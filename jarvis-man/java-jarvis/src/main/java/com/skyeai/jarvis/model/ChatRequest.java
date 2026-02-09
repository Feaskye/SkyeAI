package com.skyeai.jarvis.model;

/**
 * 聊天请求模型
 */
public class ChatRequest {
    private String message;      // 用户消息
    private String sessionId;    // 会话ID
    private String userId;       // 用户ID
    private String language;     // 语言
    private boolean useRealTimeData; // 是否使用实时数据
    private boolean useTools;    // 是否使用工具

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isUseRealTimeData() {
        return useRealTimeData;
    }

    public void setUseRealTimeData(boolean useRealTimeData) {
        this.useRealTimeData = useRealTimeData;
    }

    public boolean isUseTools() {
        return useTools;
    }

    public void setUseTools(boolean useTools) {
        this.useTools = useTools;
    }
}
