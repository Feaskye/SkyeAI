package com.skyeai.jarvis.agent;

/**
 * 意图枚举
 * 定义AgentCore支持的各种意图类型
 */
public enum Intent {
    /**
     * 普通对话
     */
    CHAT,
    
    /**
     * RAG检索
     */
    RAG,
    
    /**
     * 工具调用
     */
    TOOL_CALL,
    
    /**
     * 子代理调用
     */
    SUB_AGENT,
    
    /**
     * 总结请求
     */
    SUMMARY,
    
    /**
     * 未知意图
     */
    UNKNOWN
}