package com.skyeai.jarvis.agent.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyeai.jarvis.agent.tool.ToolRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP服务器
 * 对外暴露Jarvis能力，支持外部服务调用
 */
@Slf4j
@Component
public class McpServer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 服务端口
     */
    @Value("${mcp.server.port:3001}")
    private int port;

    /**
     * 是否启用
     */
    @Value("${mcp.server.enabled:false}")
    private boolean enabled;

    /**
     * 工具注册器
     */
    @Autowired
    private ToolRegistry toolRegistry;

    /**
     * 启动MCP服务器
     */
    @PostConstruct
    public void start() {
        if (!enabled) {
            log.info("MCP服务器已禁用");
            return;
        }

        // 实际实现应启动真正的MCP服务器
        log.info("MCP服务器启动中，端口: {}", port);
    }

    /**
     * 获取可用的工具列表
     * @return MCP工具列表
     */
    public List<McpTool> getAvailableTools() {
        return toolRegistry.getAllTools().stream()
                .map(tool -> new McpTool(
                        tool.getToolDefinition().name(),
                        tool.getToolDefinition().description(),
                        tool.getToolDefinition().inputSchema()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 调用工具
     * @param toolName 工具名称
     * @param params 参数
     * @return 调用结果
     */
    public String callTool(String toolName, java.util.Map<String, Object> params) {
        var tool = toolRegistry.getTool(toolName);
        if (tool == null) {
            return "工具不存在: " + toolName;
        }

        try {
            String jsonParams = OBJECT_MAPPER.writeValueAsString(params);
            return tool.call(jsonParams);
        } catch (Exception e) {
            log.error("工具调用失败: {}", toolName, e);
            return "工具调用失败: " + e.getMessage();
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
