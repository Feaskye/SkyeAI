package com.skyeai.jarvis.agent.mcp;

import com.skyeai.jarvis.agent.tool.ToolCallback;
import com.skyeai.jarvis.agent.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP客户端管理器
 * 管理MCP客户端连接，支持连接外部工具服务
 */
@Slf4j
@Component
public class McpClientManager {
    
    /**
     * MCP客户端缓存
     */
    private final Map<String, McpClient> clients = new ConcurrentHashMap<>();
    
    /**
     * 工具注册器
     */
    @Autowired
    private ToolRegistry toolRegistry;
    
    /**
     * 连接到MCP服务器
     * @param serverId 服务器ID
     * @param endpoint 服务器端点
     */
    public void connectToServer(String serverId, String endpoint) {
        if (clients.containsKey(serverId)) {
            log.warn("MCP服务器已连接: {}", serverId);
            return;
        }
        
        try {
            McpClient client = new McpClient(serverId, endpoint);
            client.connect();
            clients.put(serverId, client);
            
            // 自动注册工具
            List<McpTool> tools = client.discoverTools();
            registerMcpTools(tools, serverId);
            
            log.info("成功连接到MCP服务器: {} - {}", serverId, endpoint);
        } catch (Exception e) {
            log.error("连接MCP服务器失败: {} - {}", serverId, endpoint, e);
        }
    }
    
    /**
     * 断开MCP服务器连接
     * @param serverId 服务器ID
     */
    public void disconnectFromServer(String serverId) {
        McpClient removed = clients.remove(serverId);
        if (removed != null) {
            removed.disconnect();
            log.info("已断开MCP服务器: {}", serverId);
        }
    }
    
    /**
     * 注册MCP工具
     */
    private void registerMcpTools(List<McpTool> tools, String serverId) {
        for (McpTool tool : tools) {
            ToolCallback callback = ToolCallback.builder()
                    .name(tool.getName())
                    .description(tool.getDescription())
                    .inputSchema(tool.getInputSchema())
                    .function(params -> callMcpTool(serverId, tool, params))
                    .type("mcp:" + serverId)
                    .build();
            
            toolRegistry.registerTool(callback);
            log.debug("注册MCP工具: {} from {}", tool.getName(), serverId);
        }
    }
    
    /**
     * 调用MCP工具
     */
    private String callMcpTool(String serverId, McpTool tool, Map<String, Object> params) {
        McpClient client = clients.get(serverId);
        if (client == null) {
            return "MCP服务不可用: " + serverId;
        }
        
        try {
            return client.callTool(tool.getName(), params);
        } catch (Exception e) {
            log.error("MCP工具调用失败: {} - {}", serverId, tool.getName(), e);
            return "MCP工具调用失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取已连接的服务器列表
     */
    public List<String> getConnectedServers() {
        return List.copyOf(clients.keySet());
    }
    
    /**
     * MCP客户端
     */
    @Slf4j
    public static class McpClient {
        private final String serverId;
        private final String endpoint;
        private boolean connected;
        
        public McpClient(String serverId, String endpoint) {
            this.serverId = serverId;
            this.endpoint = endpoint;
        }
        
        public void connect() {
            // 实际实现应建立真正的连接
            this.connected = true;
            log.debug("连接MCP服务器: {}", serverId);
        }
        
        public void disconnect() {
            this.connected = false;
            log.debug("断开MCP服务器: {}", serverId);
        }
        
        public List<McpTool> discoverTools() {
            // 实际实现应从服务器发现工具列表
            log.debug("发现MCP工具: {}", serverId);
            return List.of();
        }
        
        public String callTool(String toolName, Map<String, Object> params) {
            // 实际实现应调用服务器的工具
            log.debug("调用MCP工具: {} - {}", serverId, toolName);
            return "MCP工具响应";
        }
    }
    
    /**
     * MCP工具
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class McpTool {
        private String name;
        private String description;
        private String inputSchema;
    }
}