package com.skyeai.jarvis.agent;

import com.skyeai.jarvis.agent.client.LlmClient;
import com.skyeai.jarvis.agent.subagent.SubAgent;
import com.skyeai.jarvis.agent.subagent.SubAgentManager;
import com.skyeai.jarvis.agent.tool.ToolCallback;
import com.skyeai.jarvis.agent.tool.ToolRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * AgentCore核心编排器
 * 统一对话流程编排，集成意图识别、RAG检索、记忆管理、模型调用和工具执行
 * 支持自动化的记忆压缩和上下文管理
 * 模型调用已下沉到jarvis-llm服务
 */
@Slf4j
@Component
public class AgentCore {
    
    /**
     * 意图识别器
     */
    @Autowired
    private IntentRecognizer intentRecognizer;
    
    /**
     * 记忆管理器
     */
    @Autowired
    private ChatMemoryManager memoryManager;
    
    /**
     * 工具注册器
     */
    @Autowired
    private ToolRegistry toolRegistry;
    
    /**
     * 子代理管理器
     */
    @Autowired
    private SubAgentManager subAgentManager;
    
    /**
     * LLM服务客户端
     */
    @Autowired
    private LlmClient llmClient;
    
    /**
     * RAG服务URL
     */
    @Value("${rag.service.url:http://localhost:8087}")
    private String ragServiceUrl;
    
    /**
     * RAG是否已加载知识库
     */
    @Value("${rag.knowledge.loaded:false}")
    private boolean ragKnowledgeLoaded;
    
    /**
     * RAG集合名称
     */
    @Value("${rag.collection.name:default}")
    private String ragCollectionName;

    /**
     * RAG返回结果数量
     */
    @Value("${rag.top.k:5}")
    private int ragTopK;

    /**
     * 系统提示词
     */
    @Value("${agent.system.prompt:You are Jarvis, a helpful AI assistant.}")
    private String systemPrompt;

    /**
     * RAG服务WebClient
     */
    private WebClient ragWebClient;

    /**
     * 初始化RAG服务客户端
     */
    @PostConstruct
    public void init() {
        this.ragWebClient = WebClient.builder()
                .baseUrl(ragServiceUrl)
                .build();
        log.info("AgentCore RAG WebClient初始化成功，服务地址: {}", ragServiceUrl);
    }
    
    /**
     * 处理对话
     * @param sessionId 会话ID
     * @param userInput 用户输入
     * @return 响应内容
     */
    public String chat(String sessionId, String userInput) {
        log.debug("AgentCore处理对话 - sessionId: {}, input: {}", sessionId, userInput);
        
        // 1. 获取或创建会话记忆
        ChatMemory memory = memoryManager.getOrCreateMemory(sessionId);
        
        // 2. 意图识别
        Intent intent = intentRecognizer.recognize(userInput);
        log.debug("识别到意图: {} - {}", intent, intentRecognizer.getIntentDescription(intent));
        
        // 3. RAG检索（如果需要）
        if (intent == Intent.RAG && ragKnowledgeLoaded) {
            String ragContext = queryRag(userInput);
            if (ragContext != null && !ragContext.isBlank()) {
                String enrichedInput = "以下是从知识库中检索到的相关参考资料，请结合这些资料回答用户的问题：\n\n" + 
                                   ragContext + "\n\n用户问题：" + userInput;
                memory.addMessage(Message.user(enrichedInput));
            } else {
                memory.addMessage(Message.user(userInput));
            }
        } else {
            memory.addMessage(Message.user(userInput));
        }
        
        // 4. 处理子代理请求
        if (intent == Intent.SUB_AGENT) {
            String subAgentResponse = handleSubAgentRequest(userInput, memory);
            memory.addMessage(Message.assistant(subAgentResponse));
            return subAgentResponse;
        }
        
        // 5. 构建消息列表
        List<Message> messages = memory.getMessages();
        
        // 6. 调用模型（记忆压缩自动处理）
        String response = callModel(messages);
        
        // 7. 添加响应到记忆
        memory.addMessage(Message.assistant(response != null ? response : ""));
        
        return response != null ? response : "";
    }
    
    /**
     * 处理工具调用
     * @param sessionId 会话ID
     * @param userInput 用户输入
     * @param toolCallbacks 工具回调列表
     * @return 响应内容
     */
    public String chatWithTools(String sessionId, String userInput, List<ToolCallback> toolCallbacks) {
        log.debug("AgentCore处理工具调用 - sessionId: {}, input: {}", sessionId, userInput);
        
        ChatMemory memory = memoryManager.getOrCreateMemory(sessionId);
        memory.addMessage(Message.user(userInput));
        
        List<Message> messages = memory.getMessages();
        
        // 调用模型（会自动触发工具回调）
        String response = callModelWithTools(messages, toolCallbacks);
        
        memory.addMessage(Message.assistant(response != null ? response : ""));
        
        return response != null ? response : "";
    }
    
    /**
     * 查询RAG服务
     * 通过HTTP调用jarvis-rag服务的多路召回接口
     * @param query 查询内容
     * @return RAG上下文
     */
    private String queryRag(String query) {
        try {
            log.debug("查询RAG服务: {}, 集合: {}, TopK: {}", query, ragCollectionName, ragTopK);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("collectionName", ragCollectionName);
            requestBody.put("limit", ragTopK);

            // 调用 jarvis-rag 服务
            String result = ragWebClient.post()
                    .uri("/api/rag/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (result != null && !result.isBlank()) {
                log.debug("RAG查询成功，结果长度: {}", result.length());
                return result;
            } else {
                log.warn("RAG服务返回空结果");
                return null;
            }
        } catch (Exception e) {
            log.error("RAG查询失败，尝试降级策略", e);
            // 降级：返回空字符串，让系统继续处理
            return null;
        }
    }
    
    /**
     * 处理子代理请求
     * @param userInput 用户输入
     * @param memory 记忆实例
     * @return 子代理响应
     */
    private String handleSubAgentRequest(String userInput, ChatMemory memory) {
        // 简单实现：创建临时子代理处理请求
        String tempAgentId = "temp-agent-" + System.currentTimeMillis();
        String agentName = "临时助手";
        String systemPrompt = "你是一个专业的助手，负责回答用户的问题。";
        
        SubAgent agent = subAgentManager.createSubAgent(tempAgentId, agentName, systemPrompt);
        String response = agent.chat(userInput);
        
        // 清理临时子代理
        subAgentManager.destroySubAgent(tempAgentId);
        
        return response;
    }
    
    /**
     * 调用模型（无工具）
     * @param messages 消息列表
     * @return 模型响应
     */
    private String callModel(List<Message> messages) {
        log.debug("调用LLM模型，消息数: {}", messages.size());
        
        // 将Message转换为LLM服务需要的格式
        List<Map<String, String>> llmMessages = convertToLlmMessages(messages);
        
        // 调用jarvis-llm服务
        LlmClient.AgentResponse response = llmClient.chat(systemPrompt, llmMessages, List.of(), false);
        
        return response != null ? response.getContent() : "";
    }
    
    /**
     * 调用模型（带工具）
     * @param messages 消息列表
     * @param toolCallbacks 工具回调列表
     * @return 模型响应
     */
    private String callModelWithTools(List<Message> messages, List<ToolCallback> toolCallbacks) {
        log.debug("调用LLM模型（带工具），消息数: {}, 工具数: {}", messages.size(), toolCallbacks.size());
        
        // 将Message转换为LLM服务需要的格式
        List<Map<String, String>> llmMessages = convertToLlmMessages(messages);
        
        // 将工具转换为LLM服务需要的格式
        List<Map<String, Object>> llmTools = convertToLlmTools(toolCallbacks);
        
        // 调用jarvis-llm服务（启用工具调用）
        LlmClient.AgentResponse response = llmClient.chat(systemPrompt, llmMessages, llmTools, true);
        
        // 如果是工具调用，执行工具并总结结果
        if (response != null && response.isToolCall()) {
            return handleToolCall(response.getToolCallData(), toolCallbacks);
        }
        
        return response != null ? response.getContent() : "";
    }
    
    /**
     * 将Message列表转换为LLM服务需要的格式
     */
    private List<Map<String, String>> convertToLlmMessages(List<Message> messages) {
        List<Map<String, String>> llmMessages = new ArrayList<>();
        
        for (Message msg : messages) {
            Map<String, String> llmMsg = new HashMap<>();
            llmMsg.put("role", msg.getMessageType().name().toLowerCase());
            llmMsg.put("content", msg.getContent());
            llmMessages.add(llmMsg);
        }
        
        return llmMessages;
    }
    
    /**
     * 将工具回调转换为LLM服务需要的格式
     * 传递ToolCallback的inputSchema作为parameters字段
     */
    private List<Map<String, Object>> convertToLlmTools(List<ToolCallback> toolCallbacks) {
        List<Map<String, Object>> llmTools = new ArrayList<>();

        for (ToolCallback tool : toolCallbacks) {
            Map<String, Object> llmTool = new HashMap<>();
            llmTool.put("type", "function");

            Map<String, Object> function = new HashMap<>();
            function.put("name", tool.getName());
            function.put("description", tool.getDescription());

            // 传递inputSchema作为parameters，如果没有则使用默认空对象
            if (tool.getInputSchema() != null && !tool.getInputSchema().isBlank()) {
                try {
                    // 尝试解析JSON Schema
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> parameters = mapper.readValue(tool.getInputSchema(), Map.class);
                    function.put("parameters", parameters);
                } catch (Exception e) {
                    log.warn("解析工具inputSchema失败: {}, 使用空对象", tool.getName(), e);
                    function.put("parameters", Map.of());
                }
            } else {
                function.put("parameters", Map.of());
            }

            llmTool.put("function", function);
            llmTools.add(llmTool);
        }

        return llmTools;
    }
    
    /**
     * 处理工具调用
     * 从toolCallData中解析工具名称和参数，只执行被调用的工具
     */
    @SuppressWarnings("unchecked")
    private String handleToolCall(Map<String, Object> toolCallData, List<ToolCallback> toolCallbacks) {
        if (toolCallData == null || toolCallbacks == null || toolCallbacks.isEmpty()) {
            log.warn("工具调用数据为空或没有可用工具");
            return "工具调用失败：无效的调用数据或没有可用工具";
        }

        // 解析工具名称和参数
        String toolName = (String) toolCallData.get("toolName");
        if (toolName == null || toolName.isBlank()) {
            // 兼容不同格式：可能叫 name 或 tool_name
            toolName = (String) toolCallData.get("name");
        }
        if (toolName == null || toolName.isBlank()) {
            log.warn("工具调用数据中未找到工具名称: {}", toolCallData);
            return "工具调用失败：未指定工具名称";
        }

        Map<String, Object> arguments = (Map<String, Object>) toolCallData.get("arguments");
        if (arguments == null) {
            arguments = (Map<String, Object>) toolCallData.get("parameters");
        }
        if (arguments == null) {
            arguments = Map.of();
        }

        log.debug("工具调用解析结果 - 工具: {}, 参数: {}", toolName, arguments);

        // 查找并执行匹配的工具
        for (ToolCallback tool : toolCallbacks) {
            if (tool.getName().equals(toolName)) {
                String result = tool.getFunction().apply(arguments);
                log.debug("工具执行完成 - {}: {}", toolName,
                    result != null ? result.substring(0, Math.min(result.length(), 100)) : "null");
                return result;
            }
        }

        // 如果未找到匹配的工具，尝试从ToolRegistry查找
        log.warn("未找到匹配的工具: {}", toolName);
        return "工具调用失败：未找到名为 \"" + toolName + "\" 的工具";
    }
    
    /**
     * 获取工具注册器
     * @return 工具注册器
     */
    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }
    
    /**
     * 获取子代理管理器
     * @return 子代理管理器
     */
    public SubAgentManager getSubAgentManager() {
        return subAgentManager;
    }
    
    /**
     * 获取记忆管理器
     * @return 记忆管理器
     */
    public ChatMemoryManager getMemoryManager() {
        return memoryManager;
    }
}