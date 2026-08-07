package com.skyeai.jarvis.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册器
 * v10 改造：持有 Spring AI 2.0 ToolCallback 接口（删除自研 POJO）
 * 保留业务元数据（type/enabled）通过自维护 Map 实现
 */
@Slf4j
@Component
public class ToolRegistry {

    /** 工具缓存，key 为工具名称 */
    private final Map<String, ToolCallback> toolCache = new ConcurrentHashMap<>();

    /** 工具类型索引 */
    private final Map<String, List<ToolCallback>> typeIndex = new ConcurrentHashMap<>();

    /** 工具类型元数据（Spring AI 原生无此字段） */
    private final Map<String, String> toolTypeMap = new ConcurrentHashMap<>();

    /** 所有实现了 InnerTool 接口的 Bean */
    @Autowired(required = false)
    private List<InnerTool> innerTools;

    @PostConstruct
    public void init() {
        if (innerTools != null && !innerTools.isEmpty()) {
            for (InnerTool tool : innerTools) {
                registerInnerTool(tool);
            }
            log.info("工具注册器初始化完成，已注册 {} 个工具", toolCache.size());
        } else {
            log.info("工具注册器初始化完成，未发现InnerTool实现");
        }
    }

    /**
     * 注册 InnerTool 实现
     */
    public void registerInnerTool(InnerTool innerTool) {
        if (!innerTool.isEnabled()) {
            log.debug("工具 {} 已禁用，跳过注册", innerTool.getToolName());
            return;
        }

        List<ToolCallback> callbacks = innerTool.loadToolCallbacks();
        for (ToolCallback callback : callbacks) {
            String toolName = callback.getToolDefinition().name();
            registerTool(callback, innerTool.getToolType());
            log.debug("注册工具: {} - {}", toolName, innerTool.getToolName());
        }
    }

    /**
     * 注册单个工具回调
     */
    public void registerTool(ToolCallback callback, String type) {
        String toolName = callback.getToolDefinition().name();
        toolCache.put(toolName, callback);
        String effectiveType = type != null ? type : "general";
        toolTypeMap.put(toolName, effectiveType);
        typeIndex.computeIfAbsent(effectiveType, k -> new ArrayList<>()).add(callback);
        log.debug("注册工具回调: {}", toolName);
    }

    /**
     * 注册单个工具回调（默认类型）
     */
    public void registerTool(ToolCallback callback) {
        registerTool(callback, "general");
    }

    /**
     * 注销工具
     */
    public boolean unregisterTool(String toolName) {
        ToolCallback removed = toolCache.remove(toolName);
        if (removed != null) {
            String type = toolTypeMap.remove(toolName);
            if (type != null) {
                List<ToolCallback> typeList = typeIndex.get(type);
                if (typeList != null) {
                    typeList.removeIf(t -> t.getToolDefinition().name().equals(toolName));
                }
            }
            log.debug("注销工具: {}", toolName);
            return true;
        }
        return false;
    }

    public ToolCallback getTool(String toolName) {
        return toolCache.get(toolName);
    }

    public boolean hasTool(String toolName) {
        return toolCache.containsKey(toolName);
    }

    /**
     * 获取所有工具回调（Spring AI ToolCallback 接口）
     */
    public List<ToolCallback> getAllTools() {
        return new ArrayList<>(toolCache.values());
    }

    /**
     * 获取所有工具回调（别名方法，供 ReactAgentConfig 使用）
     */
    public List<ToolCallback> getAllToolCallbacks() {
        return getAllTools();
    }

    public List<ToolCallback> getToolsByType(String type) {
        return typeIndex.getOrDefault(type, new ArrayList<>());
    }

    public List<String> getAllToolNames() {
        return new ArrayList<>(toolCache.keySet());
    }

    public boolean isEmpty() {
        return toolCache.isEmpty();
    }

    public int getToolCount() {
        return toolCache.size();
    }

    public List<String> getAllTypes() {
        return new ArrayList<>(typeIndex.keySet());
    }
}
