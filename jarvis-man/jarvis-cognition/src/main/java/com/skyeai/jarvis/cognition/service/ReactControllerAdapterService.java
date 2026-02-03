package com.skyeai.jarvis.cognition.service;

/**
 * React控制器适配器服务
 * 用于处理来自java-jarvis的React相关请求
 */
public interface ReactControllerAdapterService {
    
    /**
     * 执行ReAct决策流程
     */
    String executeReact(String query);
    
    /**
     * 获取工具调用提示
     */
    String getToolCallPrompt(String query);
}
