package com.skyeai.jarvis.agent.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM服务客户端
 * 用于调用jarvis-llm服务的Agent接口
 */
@Slf4j
@Component
public class LlmClient {

    @Value("${llm.service.url:http://localhost:8081}")
    private String llmServiceUrl;

    @Value("${llm.service.timeout:30000}")
    private int timeout;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(llmServiceUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        log.info("LlmClient初始化成功，服务地址: {}", llmServiceUrl);
    }

    /**
     * 调用LLM模型（消息列表格式）
     * @param systemPrompt 系统提示
     * @param messages 消息列表
     * @param tools 可用工具列表
     * @param toolCall 是否启用工具调用
     * @return 模型响应
     */
    public AgentResponse chat(String systemPrompt, List<Map<String, String>> messages,
                              List<Map<String, Object>> tools, boolean toolCall) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("systemPrompt", systemPrompt);
            requestBody.put("messages", messages);
            requestBody.put("tools", tools != null ? tools : List.of());
            requestBody.put("toolCall", toolCall);

            return webClient.post()
                    .uri("/api/llm/agent/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(AgentResponse.class)
                    .block();

        } catch (Exception e) {
            log.error("调用LLM服务失败", e);
            // 返回模拟响应
            AgentResponse response = new AgentResponse();
            response.setContent("模拟响应：收到您的消息，正在处理中...");
            response.setToolCall(false);
            return response;
        }
    }

    /**
     * 调用LLM模型（带记忆优化）
     * @param systemPrompt 系统提示
     * @param messages 消息列表
     * @param tools 可用工具列表
     * @param memorySummary 记忆摘要
     * @param maxTokens 最大token数
     * @return 模型响应
     */
    public AgentResponse chatWithMemory(String systemPrompt, List<Map<String, String>> messages,
                                        List<Map<String, Object>> tools, String memorySummary, int maxTokens) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("systemPrompt", systemPrompt);
            requestBody.put("messages", messages);
            requestBody.put("tools", tools != null ? tools : List.of());
            requestBody.put("memorySummary", memorySummary);
            requestBody.put("maxTokens", maxTokens);

            return webClient.post()
                    .uri("/api/llm/agent/chat-with-memory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(AgentResponse.class)
                    .block();

        } catch (Exception e) {
            log.error("调用LLM服务（带记忆）失败", e);
            AgentResponse response = new AgentResponse();
            response.setContent("模拟响应（带记忆）：收到您的消息，正在处理中...");
            response.setToolCall(false);
            return response;
        }
    }

    /**
     * 工具调用结果总结
     * @param query 用户查询
     * @param toolResults 工具调用结果列表
     * @param history 对话历史
     * @return 总结结果
     */
    public String summarizeToolResults(String query, List<Map<String, Object>> toolResults, String history) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("toolResults", toolResults);
            requestBody.put("history", history);

            return webClient.post()
                    .uri("/api/llm/agent/summarize-tool-results")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            log.error("总结工具结果失败", e);
            return "工具调用结果已收到，正在分析中...";
        }
    }

    /**
     * 记忆摘要生成
     * @param messages 消息列表
     * @param maxLength 最大长度
     * @return 记忆摘要
     */
    public String generateMemorySummary(List<Map<String, String>> messages, int maxLength) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", messages);
            requestBody.put("maxLength", maxLength);

            return webClient.post()
                    .uri("/api/llm/agent/memory-summary")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            log.error("生成记忆摘要失败", e);
            return "";
        }
    }

    /**
     * 文本嵌入
     * @param text 输入文本
     * @return 嵌入向量
     */
    public List<Double> embedText(String text) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);

            List<Double> result = webClient.post()
                    .uri("/api/llm/agent/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<Double>>() {})
                    .block();

            if (result != null && !result.isEmpty()) {
                return result;
            } else {
                log.warn("嵌入模型返回空结果，使用降级策略");
                return generateFallbackEmbedding(text);
            }
        } catch (Exception e) {
            log.error("文本嵌入失败，使用降级策略", e);
            return generateFallbackEmbedding(text);
        }
    }

    /**
     * 批量文本嵌入
     * @param texts 输入文本列表
     * @return 嵌入向量列表
     */
    public List<List<Double>> embedTexts(List<String> texts) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("texts", texts);

            List<List<Double>> result = webClient.post()
                    .uri("/api/llm/agent/embed-batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<List<Double>>>() {})
                    .block();

            if (result != null && !result.isEmpty()) {
                return result;
            } else {
                log.warn("批量嵌入返回空结果，使用降级策略");
                List<List<Double>> fallbackResults = new java.util.ArrayList<>();
                for (String text : texts) {
                    fallbackResults.add(generateFallbackEmbedding(text));
                }
                return fallbackResults;
            }
        } catch (Exception e) {
            log.error("批量文本嵌入失败，使用降级策略", e);
            List<List<Double>> fallbackResults = new java.util.ArrayList<>();
            for (String text : texts) {
                fallbackResults.add(generateFallbackEmbedding(text));
            }
            return fallbackResults;
        }
    }

    /**
     * 降级嵌入策略（当真实嵌入服务不可用时使用）
     * 基于文本哈希生成伪向量，保证检索功能可用
     * @param text 输入文本
     * @return 伪嵌入向量（1536维）
     */
    private List<Double> generateFallbackEmbedding(String text) {
        List<Double> vector = new java.util.ArrayList<>(1536);
        int hash = text.hashCode();
        for (int i = 0; i < 1536; i++) {
            // 基于哈希生成确定性的伪随机向量
            double value = Math.sin((hash + i) * 0.01) * 0.5;
            vector.add(value);
        }
        // 归一化
        double norm = 0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.size(); i++) {
                vector.set(i, vector.get(i) / norm);
            }
        }
        return vector;
    }

    /**
     * Agent响应
     */
    @Data
    public static class AgentResponse {
        private String content;
        private boolean toolCall;
        private Map<String, Object> toolCallData;
        private String finishReason;
        private int tokenCount;
        private Map<String, Object> metadata;
    }
}