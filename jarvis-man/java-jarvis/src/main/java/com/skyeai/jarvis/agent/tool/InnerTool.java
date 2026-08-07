package com.skyeai.jarvis.agent.tool;

import java.util.List;
import org.springframework.ai.tool.ToolCallback;

/**
 * 内部工具接口
 * 定义可插拔工具的标准接口，实现开闭原则
 * 所有自定义工具都需要实现此接口
 * v10 改造：loadToolCallbacks() 返回 Spring AI 2.0 ToolCallback（删除自研 POJO）
 */
public interface InnerTool {

    /**
     * 加载工具回调列表（返回 Spring AI 2.0 ToolCallback）
     * @return 工具回调列表
     */
    List<ToolCallback> loadToolCallbacks();
    
    /**
     * 获取工具名称
     * @return 工具名称（用于标识和显示）
     */
    String getToolName();
    
    /**
     * 获取工具描述
     * @return 工具描述（用于向用户说明工具用途）
     */
    String getToolDescription();
    
    /**
     * 获取工具类型分类
     * @return 工具类型，如：weather, stock, search等
     */
    default String getToolType() {
        return "general";
    }
    
    /**
     * 是否启用该工具
     * @return 是否启用
     */
    default boolean isEnabled() {
        return true;
    }
}