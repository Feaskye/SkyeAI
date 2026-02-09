package com.skyeai.jarvis.model;

import java.util.Map;

/**
 * 聊天响应模型
 */
public class ChatResponse {
    private String response;           // AI响应消息
    private boolean usedRealTimeData;  // 是否使用了实时数据
    private Map<String, Object> toolResults; // 工具执行结果
    private String sessionId;          // 会话ID
    private long executionTime;        // 执行时间（毫秒）
    private String error;              // 错误信息

    // Getters and Setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isUsedRealTimeData() {
        return usedRealTimeData;
    }

    public void setUsedRealTimeData(boolean usedRealTimeData) {
        this.usedRealTimeData = usedRealTimeData;
    }

    public Map<String, Object> getToolResults() {
        return toolResults;
    }

    public void setToolResults(Map<String, Object> toolResults) {
        this.toolResults = toolResults;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
