package com.skyeai.jarvis.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册器
 * 负责管理所有可插拔工具的注册、注销和查询
 * 支持运行时动态注册和注销工具
 */
@Slf4j
@Component
public class ToolRegistry {
    
    /**
     * 工具缓存，key为工具名称
     */
    private final Map<String, ToolCallback> toolCache = new ConcurrentHashMap<>();
    
    /**
     * 工具类型索引
     */
    private final Map<String, List<ToolCallback>> typeIndex = new ConcurrentHashMap<>();
    
    /**
     * 所有实现了InnerTool接口的Bean
     * Spring会自动注入
     */
    @Autowired(required = false)
    private List<InnerTool> innerTools;
    
    /**
     * 初始化方法
     * 自动扫描并注册所有InnerTool实现
     */
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
     * 注册InnerTool实现
     * @param innerTool InnerTool实现
     */
    public void registerInnerTool(InnerTool innerTool) {
        if (!innerTool.isEnabled()) {
            log.debug("工具 {} 已禁用，跳过注册", innerTool.getToolName());
            return;
        }
        
        List<ToolCallback> callbacks = innerTool.loadToolCallbacks();
        for (ToolCallback callback : callbacks) {
            registerTool(callback);
            log.debug("注册工具: {} - {}", callback.getName(), innerTool.getToolName());
        }
    }
    
    /**
     * 注册单个工具回调
     * @param callback 工具回调
     */
    public void registerTool(ToolCallback callback) {
        toolCache.put(callback.getName(), callback);
        
        // 更新类型索引
        String type = callback.getType() != null ? callback.getType() : "general";
        typeIndex.computeIfAbsent(type, k -> new ArrayList<>()).add(callback);
        
        log.debug("注册工具回调: {}", callback.getName());
    }
    
    /**
     * 注销工具
     * @param toolName 工具名称
     * @return 是否注销成功
     */
    public boolean unregisterTool(String toolName) {
        ToolCallback removed = toolCache.remove(toolName);
        if (removed != null) {
            // 从类型索引中移除
            String type = removed.getType() != null ? removed.getType() : "general";
            List<ToolCallback> typeList = typeIndex.get(type);
            if (typeList != null) {
                typeList.removeIf(t -> t.getName().equals(toolName));
            }
            
            log.debug("注销工具: {}", toolName);
            return true;
        }
        return false;
    }
    
    /**
     * 获取工具回调
     * @param toolName 工具名称
     * @return 工具回调，如果不存在返回null
     */
    public ToolCallback getTool(String toolName) {
        return toolCache.get(toolName);
    }
    
    /**
     * 检查工具是否存在
     * @param toolName 工具名称
     * @return 是否存在
     */
    public boolean hasTool(String toolName) {
        return toolCache.containsKey(toolName);
    }
    
    /**
     * 获取所有工具回调
     * @return 工具回调列表
     */
    public List<ToolCallback> getAllTools() {
        return new ArrayList<>(toolCache.values());
    }
    
    /**
     * 获取指定类型的工具
     * @param type 工具类型
     * @return 工具回调列表
     */
    public List<ToolCallback> getToolsByType(String type) {
        return typeIndex.getOrDefault(type, new ArrayList<>());
    }
    
    /**
     * 获取所有工具名称
     * @return 工具名称列表
     */
    public List<String> getAllToolNames() {
        return new ArrayList<>(toolCache.keySet());
    }
    
    /**
     * 检查是否有可用工具
     * @return 是否有工具
     */
    public boolean isEmpty() {
        return toolCache.isEmpty();
    }
    
    /**
     * 获取工具数量
     * @return 工具数量
     */
    public int getToolCount() {
        return toolCache.size();
    }
    
    /**
     * 获取所有工具类型
     * @return 类型列表
     */
    public List<String> getAllTypes() {
        return new ArrayList<>(typeIndex.keySet());
    }
}