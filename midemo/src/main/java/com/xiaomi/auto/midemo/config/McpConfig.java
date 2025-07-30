package com.xiaomi.auto.midemo.config;

import org.springframework.ai.mcp.client.McpClientProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Value("${spring.ai.mcp.client.server-url}")
    private String serverUrl;

    @Value("${spring.ai.mcp.client.api-key}")
    private String apiKey;

    @Value("${spring.ai.mcp.client.timeout:30000}")
    private int timeout;

    @Bean
    public McpClientProperties mcpClientProperties() {
        McpClientProperties properties = new McpClientProperties();
        properties.setServerUrl(serverUrl);
        properties.setApiKey(apiKey);
        properties.setTimeout(timeout);
        return properties;
    }
}