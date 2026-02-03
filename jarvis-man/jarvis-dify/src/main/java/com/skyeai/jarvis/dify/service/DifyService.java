package com.skyeai.jarvis.dify.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;

@Service
public class DifyService {

    @Value("${dify.base-url}")
    private String difyBaseUrl;

    @Value("${dify.api-key}")
    private String difyApiKey;

    @Value("${dify.timeout}")
    private int difyTimeout;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(difyBaseUrl)
                .defaultHeader("Authorization", "Bearer " + difyApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        System.out.println("DifyService initialized successfully");
    }

    /**
     * 调用Dify聊天API
     * @param query 用户查询
     * @param conversationId 对话ID
     * @param inputs 输入参数
     * @return 聊天响应
     */
    public Map<String, Object> chat(String query, String conversationId, Map<String, Object> inputs) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "query", query,
                    "conversation_id", conversationId,
                    "inputs", inputs
            );

            return webClient.post()
                    .uri("/api/v1/chat/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(difyTimeout))
                    .block();
        } catch (Exception e) {
            System.err.println("Failed to call Dify chat API: " + e.getMessage());
            throw new RuntimeException("Failed to call Dify chat API", e);
        }
    }

    /**
     * 创建Dify应用
     * @param name 应用名称
     * @param description 应用描述
     * @param model 模型配置
     * @return 应用信息
     */
    public Map<String, Object> createApplication(String name, String description, Map<String, Object> model) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "name", name,
                    "description", description,
                    "model", model
            );

            return webClient.post()
                    .uri("/api/v1/applications")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(difyTimeout))
                    .block();
        } catch (Exception e) {
            System.err.println("Failed to create Dify application: " + e.getMessage());
            throw new RuntimeException("Failed to create Dify application", e);
        }
    }

    /**
     * 获取应用列表
     * @return 应用列表
     */
    public Map<String, Object> getApplications() {
        try {
            return webClient.get()
                    .uri("/api/v1/applications")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(difyTimeout))
                    .block();
        } catch (Exception e) {
            System.err.println("Failed to get Dify applications: " + e.getMessage());
            throw new RuntimeException("Failed to get Dify applications", e);
        }
    }

    /**
     * 扩展文档上下文
     * @param content 文档内容
     * @param maxLength 最大长度
     * @return 扩展后的上下文
     */
    public String extendDocumentContext(String content, int maxLength) {
        // 实际应用中应该实现更复杂的上下文扩展逻辑
        if (content.length() <= maxLength) {
            return content;
        }
        
        // 截断并添加省略号
        return content.substring(0, maxLength - 3) + "...";
    }

    /**
     * 健康检查
     * @return 健康状态
     */
    public boolean healthCheck() {
        try {
            var response = webClient.get()
                    .uri("/api/v1/health")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(5000))
                    .block();
            return response != null && "ok".equals(response.get("status"));
        } catch (Exception e) {
            System.err.println("Dify health check failed: " + e.getMessage());
            return false;
        }
    }
}
