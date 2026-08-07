package com.skyeai.jarvis.llm.service.impl;

import com.skyeai.jarvis.llm.service.LlmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 Spring AI 2.0 的 LlmService 实现
 * v10 Phase 6：使用 Spring AI ChatModel / EmbeddingModel 替换手写 HttpClient 调用
 *
 * 装配策略：
 * - 由 spring-ai-alibaba-starter-dashscope 自动装配 DashScopeChatModel / DashScopeEmbeddingModel
 * - 注入通用接口（ChatModel / EmbeddingModel），不绑定特定实现类，便于切换模型提供商
 * - 当 jarvis.llm.impl=spring-ai 或未配置时激活（matchIfMissing=true）
 * - 缺失配置时显式抛 IllegalStateException，不静默返回空
 *
 * 注意：Spring AI 通用接口的工具调用需通过 ToolCallback 注册，当前 chat/chatWithMemory
 * 的 tools 参数仅记录日志，不直接传递 OpenAI 格式工具定义。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "jarvis.llm.impl", havingValue = "spring-ai", matchIfMissing = true)
public class SpringAiLlmServiceImpl implements LlmService {

    /** Spring AI ChatModel（自动装配为 DashScopeChatModel） */
    @Autowired(required = false)
    private ChatModel chatModel;

    /** Spring AI EmbeddingModel（自动装配为 DashScopeEmbeddingModel） */
    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    public SpringAiLlmServiceImpl() {
        log.info("SpringAiLlmServiceImpl 初始化（jarvis.llm.impl=spring-ai）");
    }

    // ==================== 基础对话方法 ====================

    @Override
    public String generateText(String prompt) {
        return generateText("You are a helpful assistant", prompt);
    }

    @Override
    public String generateText(String systemPrompt, String userPrompt) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            throw new IllegalArgumentException("用户提示词不能为空");
        }
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        return generateText(systemPrompt, messages);
    }

    @Override
    public String generateText(String systemPrompt, List<Map<String, String>> messages) {
        ChatModel model = requireChatModel();
        try {
            List<Message> springMessages = toSpringMessages(systemPrompt, messages);
            Prompt prompt = new Prompt(springMessages);
            ChatResponse response = model.call(prompt);
            String text = extractText(response);
            log.info("Spring AI 文本生成成功，输入消息数：{}", springMessages.size());
            return text;
        } catch (Exception e) {
            log.error("Spring AI 文本生成失败：{}", e.getMessage(), e);
            throw new RuntimeException("Spring AI 文本生成失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void generateTextStream(String prompt, LlmStreamCallback callback) {
        generateTextStream("You are a helpful assistant", prompt, callback);
    }

    @Override
    public void generateTextStream(String systemPrompt, String userPrompt, LlmStreamCallback callback) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        generateTextStream(systemPrompt, messages, callback);
    }

    @Override
    public void generateTextStream(String systemPrompt, List<Map<String, String>> messages, LlmStreamCallback callback) {
        ChatModel model = requireChatModel();
        try {
            List<Message> springMessages = toSpringMessages(systemPrompt, messages);
            Prompt prompt = new Prompt(springMessages);
            // 使用 Spring AI 流式接口，转为同步 Stream 逐 token 回调
            model.stream(prompt).toStream().forEach(chatResponse -> {
                String token = extractText(chatResponse);
                if (token != null && !token.isEmpty()) {
                    callback.onToken(token);
                }
            });
            callback.onComplete();
            log.info("Spring AI 流式生成完成");
        } catch (Exception e) {
            log.error("Spring AI 流式生成失败：{}", e.getMessage(), e);
            callback.onError(e);
        }
    }

    // ==================== 嵌入方法 ====================

    @Override
    public List<Double> embedText(String text) {
        if (text == null || text.isBlank()) {
            // 缺失输入显式报错，不静默返回空
            throw new IllegalArgumentException("embedText 输入不能为空");
        }
        EmbeddingModel model = requireEmbeddingModel();
        try {
            EmbeddingResponse response = model.embedForResponse(List.of(text));
            if (response.getResults() == null || response.getResults().isEmpty()) {
                throw new IllegalStateException("嵌入模型返回空结果，请检查 DASHSCOPE_API_KEY 配置");
            }
            float[] vector = response.getResults().get(0).getOutput();
            List<Double> result = new ArrayList<>(vector.length);
            for (float v : vector) {
                result.add((double) v);
            }
            log.info("Spring AI 文本嵌入成功，向量维度：{}", vector.length);
            return result;
        } catch (Exception e) {
            log.error("Spring AI 文本嵌入失败：{}", e.getMessage(), e);
            throw new RuntimeException("Spring AI 文本嵌入失败：" + e.getMessage(), e);
        }
    }

    @Override
    public List<List<Double>> embedTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("embedTexts 输入不能为空");
        }
        EmbeddingModel model = requireEmbeddingModel();
        try {
            EmbeddingResponse response = model.embedForResponse(texts);
            List<List<Double>> results = new ArrayList<>(response.getResults().size());
            for (Embedding embedding : response.getResults()) {
                float[] vector = embedding.getOutput();
                List<Double> item = new ArrayList<>(vector.length);
                for (float v : vector) {
                    item.add((double) v);
                }
                results.add(item);
            }
            log.info("Spring AI 批量嵌入成功，数量：{}", results.size());
            return results;
        } catch (Exception e) {
            log.error("Spring AI 批量嵌入失败：{}", e.getMessage(), e);
            throw new RuntimeException("Spring AI 批量嵌入失败：" + e.getMessage(), e);
        }
    }

    // ==================== 模型信息方法 ====================

    @Override
    public Map<String, Object> getModelInfo(String modelName) {
        Map<String, Object> info = new HashMap<>();
        info.put("provider", "spring-ai-dashscope");
        info.put("requestedModel", modelName);
        info.put("chatModelAvailable", chatModel != null);
        info.put("embeddingModelAvailable", embeddingModel != null);
        return info;
    }

    @Override
    public List<String> listModels() {
        // 返回 DashScope 支持的常用模型
        List<String> models = new ArrayList<>();
        models.add("qwen-turbo");
        models.add("qwen-plus");
        models.add("qwen-max");
        models.add("qwen-long");
        models.add("text-embedding-v1");
        models.add("text-embedding-v2");
        return models;
    }

    // ==================== 多模态方法 ====================
    // Spring AI 通用 ChatModel 接口不直接支持图像/语音/视频处理，显式报错避免静默失败

    @Override
    public ImageProcessingResult processImage(InputStream imageStream, String imageType) {
        throw new UnsupportedOperationException(
                "Spring AI 通用 ChatModel 接口不直接支持图像处理，请配置 jarvis.llm.impl=legacy 或专用多模态模型");
    }

    @Override
    public SpeechProcessingResult processSpeech(InputStream audioStream, String audioType) {
        throw new UnsupportedOperationException(
                "Spring AI 通用 ChatModel 接口不直接支持语音处理，请配置 jarvis.llm.impl=legacy 或专用多模态模型");
    }

    @Override
    public VideoProcessingResult processVideo(InputStream videoStream, String videoType) {
        throw new UnsupportedOperationException(
                "Spring AI 通用 ChatModel 接口不直接支持视频处理，请配置 jarvis.llm.impl=legacy 或专用多模态模型");
    }

    @Override
    public MultimodalFusionResult fuseMultimodalInformation(Map<String, Object> multimodalData) {
        throw new UnsupportedOperationException(
                "Spring AI 通用 ChatModel 接口不直接支持多模态融合，请配置 jarvis.llm.impl=legacy");
    }

    // ==================== ReAct 决策流程 ====================

    @Override
    public String executeReact(String query) {
        // 构建完整 ReAct 执行流程
        String taskPlan = generateTaskPlan(query, "", "");
        generateThought(query, "", "", "", taskPlan, new ArrayList<>());
        decideAction(query, "", "", "", taskPlan, new ArrayList<>());
        evaluateProgress(query, "", taskPlan, "模拟执行结果");
        return generateFinalAnswer(query, "", "", "", taskPlan);
    }

    @Override
    public String generateTaskPlan(String query, String contextInfo, String userPreferences) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯，一个强大的AI助手。你需要根据用户的问题、聊天上下文和用户偏好，生成详细的任务规划。\n\n");
        prompt.append("用户问题: ").append(query).append("\n\n");
        if (contextInfo != null && !contextInfo.isEmpty()) {
            prompt.append("聊天上下文:\n").append(contextInfo).append("\n\n");
        }
        if (userPreferences != null && !userPreferences.isEmpty()) {
            prompt.append("用户偏好:\n").append(userPreferences).append("\n\n");
        }
        prompt.append("请生成一个详细的任务规划，包括：\n");
        prompt.append("1. 任务目标\n2. 所需步骤\n3. 可能需要的工具\n4. 预期结果\n\n规划: ");
        return generateText(prompt.toString());
    }

    @Override
    public String generateThought(String query, String history, String contextInfo, String userPreferences,
                                  String taskPlan, List<String> tools) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯，一个强大的AI助手。你需要根据用户的问题、历史记录、聊天上下文、用户偏好和任务规划，生成下一步的思考。\n\n");
        prompt.append("用户问题: ").append(query).append("\n\n");
        if (contextInfo != null && !contextInfo.isEmpty()) {
            prompt.append("聊天上下文:\n").append(contextInfo).append("\n\n");
        }
        if (userPreferences != null && !userPreferences.isEmpty()) {
            prompt.append("用户偏好:\n").append(userPreferences).append("\n\n");
        }
        if (taskPlan != null && !taskPlan.isEmpty()) {
            prompt.append("任务规划:\n").append(taskPlan).append("\n\n");
        }
        if (history != null && !history.isEmpty()) {
            prompt.append("历史记录:\n").append(history).append("\n\n");
        }
        if (tools != null && !tools.isEmpty()) {
            prompt.append("可用工具:\n");
            for (String tool : tools) {
                prompt.append("- ").append(tool).append("\n");
            }
        }
        prompt.append("\n请生成你的思考过程，只需要思考，不需要执行任何动作。\n思考: ");
        return generateText(prompt.toString());
    }

    @Override
    public String decideAction(String query, String history, String contextInfo, String userPreferences,
                               String taskPlan, List<String> tools) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯，一个强大的AI助手。你需要根据用户的问题、历史记录、思考过程、聊天上下文、用户偏好和任务规划，决定下一步的行动。\n\n");
        prompt.append("用户问题: ").append(query).append("\n\n");
        if (contextInfo != null && !contextInfo.isEmpty()) {
            prompt.append("聊天上下文:\n").append(contextInfo).append("\n\n");
        }
        if (userPreferences != null && !userPreferences.isEmpty()) {
            prompt.append("用户偏好:\n").append(userPreferences).append("\n\n");
        }
        if (taskPlan != null && !taskPlan.isEmpty()) {
            prompt.append("任务规划:\n").append(taskPlan).append("\n\n");
        }
        if (history != null && !history.isEmpty()) {
            prompt.append("历史记录:\n").append(history).append("\n\n");
        }
        if (tools != null && !tools.isEmpty()) {
            prompt.append("可用工具:\n");
            for (String tool : tools) {
                prompt.append("- ").append(tool).append("\n");
            }
        }
        prompt.append("\n请从可用工具中选择一个，并以JSON格式返回，包含tool(工具名称)和parameters(参数)字段。\n");
        prompt.append("如果不需要使用工具，可以返回{\"tool\": \"finish\", \"parameters\": {}}\n");
        prompt.append("输出格式示例: {\"tool\": \"browser\", \"parameters\": {\"url\": \"https://github.com\"}}\n\n输出: ");
        return generateText(prompt.toString());
    }

    @Override
    public boolean evaluateProgress(String query, String history, String taskPlan, String observation) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯，一个强大的AI助手。你需要根据用户的问题、历史执行记录、任务规划和最新的执行结果，评估任务执行进度。\n\n");
        prompt.append("用户问题: ").append(query).append("\n\n");
        if (taskPlan != null) {
            prompt.append("任务规划:\n").append(taskPlan).append("\n\n");
        }
        if (history != null) {
            prompt.append("历史执行记录:\n").append(history).append("\n\n");
        }
        prompt.append("最新执行结果:\n").append(observation).append("\n\n");
        prompt.append("请评估任务是否已经完成。如果任务已经完成，返回false；如果任务还需要继续执行，返回true。\n");
        prompt.append("只需要返回true或false，不需要其他任何解释。\n评估结果: ");
        String evaluation = generateText(prompt.toString());
        return evaluation != null && evaluation.toLowerCase().contains("true");
    }

    @Override
    public String generateFinalAnswer(String query, String history, String contextInfo, String userPreferences,
                                      String taskPlan) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯，一个强大的AI助手。请根据用户的问题、完整的历史记录、聊天上下文、用户偏好和任务规划，生成最终的回答。\n\n");
        prompt.append("用户问题: ").append(query).append("\n\n");
        if (contextInfo != null && !contextInfo.isEmpty()) {
            prompt.append("聊天上下文:\n").append(contextInfo).append("\n\n");
        }
        if (userPreferences != null && !userPreferences.isEmpty()) {
            prompt.append("用户偏好:\n").append(userPreferences).append("\n\n");
        }
        if (taskPlan != null && !taskPlan.isEmpty()) {
            prompt.append("任务规划:\n").append(taskPlan).append("\n\n");
        }
        if (history != null && !history.isEmpty()) {
            prompt.append("完整历史记录:\n").append(history).append("\n\n");
        }
        prompt.append("请总结整个过程，给出最终的回答。最终回答: ");
        return generateText(prompt.toString());
    }

    // ==================== Agent 专用接口 ====================

    @Override
    public AgentResponse chat(String systemPrompt, List<Map<String, String>> messages,
                              List<Map<String, Object>> tools, boolean toolCall) {
        ChatModel model = requireChatModel();
        AgentResponse response = new AgentResponse();
        try {
            List<Message> springMessages = toSpringMessages(systemPrompt, messages);
            Prompt prompt = new Prompt(springMessages);
            ChatResponse chatResponse = model.call(prompt);
            String content = extractText(chatResponse);
            response.setContent(content != null ? content : "");
            response.setToolCall(false);
            response.setFinishReason("stop");
            if (toolCall && tools != null && !tools.isEmpty()) {
                // Spring AI 工具调用需通过 ToolCallback 注册，此处仅记录日志，不传递 OpenAI 格式工具定义
                log.warn("Spring AI 通用 ChatModel 的工具调用需通过 ToolCallback 注册，当前忽略 tools 参数（数量={}）", tools.size());
            }
            log.info("Spring AI Agent chat 成功，消息数：{}", springMessages.size());
        } catch (Exception e) {
            log.error("Spring AI Agent chat 失败：{}", e.getMessage(), e);
            throw new RuntimeException("Spring AI Agent chat 失败：" + e.getMessage(), e);
        }
        return response;
    }

    @Override
    public AgentResponse chatWithMemory(String systemPrompt, List<Map<String, String>> messages,
                                        List<Map<String, Object>> tools, String memorySummary, int maxTokens) {
        ChatModel model = requireChatModel();
        AgentResponse response = new AgentResponse();
        try {
            // 记忆摘要注入到系统提示
            String enhancedSystemPrompt = systemPrompt;
            if (memorySummary != null && !memorySummary.isEmpty()) {
                enhancedSystemPrompt = memorySummary + "\n\n" + systemPrompt;
            }
            List<Message> springMessages = toSpringMessages(enhancedSystemPrompt, messages);
            Prompt prompt = new Prompt(springMessages);
            ChatResponse chatResponse = model.call(prompt);
            String content = extractText(chatResponse);
            response.setContent(content != null ? content : "");
            response.setToolCall(false);
            response.setFinishReason("stop");
            if (maxTokens > 0) {
                // 当前由模型默认配置决定 maxTokens，如需精确控制需通过 ChatOptions
                log.debug("chatWithMemory maxTokens={} 当前由模型默认配置决定", maxTokens);
            }
            log.info("Spring AI Agent chatWithMemory 成功，消息数：{}", springMessages.size());
        } catch (Exception e) {
            log.error("Spring AI Agent chatWithMemory 失败：{}", e.getMessage(), e);
            throw new RuntimeException("Spring AI Agent chatWithMemory 失败：" + e.getMessage(), e);
        }
        return response;
    }

    @Override
    public void chatStream(String systemPrompt, List<Map<String, String>> messages,
                           List<Map<String, Object>> tools, AgentStreamCallback callback) {
        ChatModel model = requireChatModel();
        try {
            List<Message> springMessages = toSpringMessages(systemPrompt, messages);
            Prompt prompt = new Prompt(springMessages);
            model.stream(prompt).toStream().forEach(chatResponse -> {
                String token = extractText(chatResponse);
                if (token != null && !token.isEmpty()) {
                    callback.onToken(token);
                }
            });
            callback.onComplete();
            log.info("Spring AI Agent chatStream 完成");
        } catch (Exception e) {
            log.error("Spring AI Agent chatStream 失败：{}", e.getMessage(), e);
            callback.onError(e);
        }
    }

    @Override
    public String summarizeToolResults(String query, List<Map<String, Object>> toolResults, String history) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下工具调用结果，对用户的问题进行总结回答。\n\n");
        prompt.append("用户问题: ").append(query).append("\n\n");
        prompt.append("工具调用结果:\n");
        if (toolResults != null) {
            for (int i = 0; i < toolResults.size(); i++) {
                prompt.append(i + 1).append(". ").append(toolResults.get(i)).append("\n");
            }
        }
        if (history != null && !history.isEmpty()) {
            prompt.append("\n历史记录: ").append(history).append("\n\n");
        }
        prompt.append("请总结工具调用结果，给出最终回答：");
        return generateText(prompt.toString());
    }

    @Override
    public String generateMemorySummary(List<Map<String, String>> messages, int maxLength) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("请对以下对话历史进行摘要，保留关键信息，控制在").append(maxLength).append("字以内。\n\n对话历史:\n");
        for (Map<String, String> message : messages) {
            String role = message.get("role");
            String content = message.get("content");
            prompt.append(role).append(": ").append(content).append("\n");
        }
        prompt.append("\n摘要: ");
        String summary = generateText(prompt.toString());
        if (summary != null && summary.length() > maxLength) {
            summary = summary.substring(0, maxLength) + "...";
        }
        return summary;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 校验 ChatModel 可用，缺失时显式抛 IllegalStateException
     */
    private ChatModel requireChatModel() {
        if (chatModel == null) {
            throw new IllegalStateException(
                    "Spring AI ChatModel 未装配，请检查 spring-ai-alibaba-starter-dashscope 依赖与 DASHSCOPE_API_KEY 配置");
        }
        return chatModel;
    }

    /**
     * 校验 EmbeddingModel 可用，缺失时显式抛 IllegalStateException
     */
    private EmbeddingModel requireEmbeddingModel() {
        if (embeddingModel == null) {
            throw new IllegalStateException(
                    "Spring AI EmbeddingModel 未装配，请检查 spring-ai-alibaba-starter-dashscope 依赖与 DASHSCOPE_API_KEY 配置");
        }
        return embeddingModel;
    }

    /**
     * 将项目消息列表（OpenAI 格式 Map）转换为 Spring AI Message 列表
     */
    private List<Message> toSpringMessages(String systemPrompt, List<Map<String, String>> messages) {
        List<Message> springMessages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            springMessages.add(new SystemMessage(systemPrompt));
        }
        if (messages != null) {
            for (Map<String, String> message : messages) {
                String role = message.get("role");
                String content = message.get("content");
                if (content == null) {
                    content = "";
                }
                if ("system".equalsIgnoreCase(role)) {
                    springMessages.add(new SystemMessage(content));
                } else if ("assistant".equalsIgnoreCase(role)) {
                    springMessages.add(new AssistantMessage(content));
                } else {
                    // 默认按用户消息处理（user 及未知角色）
                    springMessages.add(new UserMessage(content));
                }
            }
        }
        return springMessages;
    }

    /**
     * 从 ChatResponse 提取文本内容
     */
    private String extractText(ChatResponse response) {
        if (response == null) {
            return "";
        }
        Generation generation = response.getResult();
        if (generation == null) {
            return "";
        }
        Message output = generation.getOutput();
        if (output == null) {
            return "";
        }
        String text = output.getText();
        return text != null ? text : "";
    }
}
