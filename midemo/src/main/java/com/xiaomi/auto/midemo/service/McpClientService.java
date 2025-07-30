package com.xiaomi.auto.midemo.service;

import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.mcp.client.McpClientProperties;
import org.springframework.ai.mcp.client.WebClientMcpClient;
import org.springframework.ai.mcp.common.McpRequest;
import org.springframework.ai.mcp.common.McpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class McpClientService {
    private final McpClient mcpClient;

    @Autowired
    public McpClientService(McpClientProperties properties) {
        this.mcpClient = new WebClientMcpClient(properties);
    }

    /**
     * 发送请求到MCP服务器获取模型响应
     */
    public String getModelResponse(String prompt, String modelName) {
        McpRequest request = McpRequest.builder()
                .prompt(prompt)
                .model(modelName)
                .temperature(0.7f)
                .maxTokens(500)
                .build();

        Mono<McpResponse> responseMono = mcpClient.generate(request);
        McpResponse response = responseMono.block();

        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getText();
        }
        return "获取模型响应失败";
    }

    /**
     * 使用MCP进行情感分析
     */
    public String analyzeSentimentWithMcp(String text) {
        String prompt = String.format("分析以下文本的情感倾向，返回'正面'、'负面'或'中性':\n%s", text);
        return getModelResponse(prompt, "sentiment-analysis-model");
    }
}