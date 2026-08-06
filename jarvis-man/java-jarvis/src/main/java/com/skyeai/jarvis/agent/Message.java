package com.skyeai.jarvis.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息模型
 * 表示对话中的一条消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        USER,          // 用户消息
        ASSISTANT,     // 助手消息
        SYSTEM,        // 系统消息
        TOOL,          // 工具调用消息
        TOOL_RESULT    // 工具结果消息
    }
    
    /**
     * 消息ID
     */
    private String id;
    
    /**
     * 消息类型
     */
    private MessageType messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 工具名称（仅工具消息使用）
     */
    private String toolName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 工具调用参数（仅工具消息使用）
     */
    private String toolParameters;
    
    /**
     * 工具调用结果（仅工具结果消息使用）
     */
    private String toolResult;
    
    /**
     * 创建用户消息
     */
    public static Message user(String content) {
        Message msg = new Message();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setMessageType(MessageType.USER);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        return msg;
    }
    
    /**
     * 创建助手消息
     */
    public static Message assistant(String content) {
        Message msg = new Message();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setMessageType(MessageType.ASSISTANT);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        return msg;
    }
    
    /**
     * 创建系统消息
     */
    public static Message system(String content) {
        Message msg = new Message();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setMessageType(MessageType.SYSTEM);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        return msg;
    }
    
    /**
     * 创建工具调用消息
     */
    public static Message tool(String toolName, String parameters) {
        Message msg = new Message();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setMessageType(MessageType.TOOL);
        msg.setToolName(toolName);
        msg.setToolParameters(parameters);
        msg.setCreatedAt(LocalDateTime.now());
        return msg;
    }
    
    /**
     * 创建工具结果消息
     */
    public static Message toolResult(String toolName, String result) {
        Message msg = new Message();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setMessageType(MessageType.TOOL_RESULT);
        msg.setToolName(toolName);
        msg.setToolResult(result);
        msg.setCreatedAt(LocalDateTime.now());
        return msg;
    }
}