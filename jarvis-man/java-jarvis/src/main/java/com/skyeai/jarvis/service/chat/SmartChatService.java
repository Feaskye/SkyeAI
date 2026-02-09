package com.skyeai.jarvis.service.chat;

import com.skyeai.jarvis.service.nlp.ConversationResult;

import java.util.Map;

/**
 * 智能对话服务接口
 * 集成函数调用和实时搜索，提供基于最新数据的智能回答
 */
public interface SmartChatService {
    
    /**
     * 智能对话处理
     * @param message 用户消息
     * @param sessionId 会话ID
     * @param useRealtimeData 是否使用实时数据
     * @return 对话结果
     */
    ConversationResult chat(String message, String sessionId, boolean useRealtimeData);
    
    /**
     * 智能对话处理（带参数）
     * @param message 用户消息
     * @param sessionId 会话ID
     * @param useRealtimeData 是否使用实时数据
     * @param parameters 额外参数
     * @return 对话结果
     */
    ConversationResult chat(String message, String sessionId, boolean useRealtimeData, Map<String, Object> parameters);
    
    /**
     * 获取会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    Map<String, Object> getSessions(String userId);
    
    /**
     * 获取会话详情
     * @param sessionId 会话ID
     * @return 会话详情
     */
    Map<String, Object> getSessionDetail(String sessionId);
    
    /**
     * 结束会话
     * @param sessionId 会话ID
     * @return 操作结果
     */
    Map<String, Object> endSession(String sessionId);
}