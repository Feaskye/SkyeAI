package com.skyeai.jarvis.user.service;

import com.skyeai.jarvis.user.model.ChatHistory;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatHistoryService {

    /**
     * 创建聊天历史记录
     */
    ChatHistory createChatHistory(ChatHistory chatHistory);

    /**
     * 根据ID获取聊天历史记录
     */
    ChatHistory getChatHistoryById(Long id);

    /**
     * 根据用户ID获取聊天历史记录
     */
    List<ChatHistory> getChatHistoriesByUserId(Long userId);

    /**
     * 根据会话ID获取聊天历史记录
     */
    List<ChatHistory> getChatHistoriesBySessionId(String sessionId);

    /**
     * 根据用户ID和会话ID获取聊天历史记录
     */
    List<ChatHistory> getChatHistoriesByUserIdAndSessionId(Long userId, String sessionId);

    /**
     * 获取用户的最近聊天历史记录
     */
    List<ChatHistory> getRecentChatHistoriesByUserId(Long userId, int limit);

    /**
     * 根据时间范围获取用户的聊天历史记录
     */
    List<ChatHistory> getChatHistoriesByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 根据模型类型获取用户的聊天历史记录
     */
    List<ChatHistory> getChatHistoriesByUserIdAndModelUsed(Long userId, String modelUsed);

    /**
     * 根据提示类型获取用户的聊天历史记录
     */
    List<ChatHistory> getChatHistoriesByUserIdAndPromptType(Long userId, String promptType);

    /**
     * 更新聊天历史记录
     */
    ChatHistory updateChatHistory(Long id, ChatHistory chatHistory);

    /**
     * 删除聊天历史记录
     */
    void deleteChatHistory(Long id);

    /**
     * 删除用户的所有聊天历史记录
     */
    void deleteChatHistoriesByUserId(Long userId);

    /**
     * 删除指定会话的所有聊天历史记录
     */
    void deleteChatHistoriesBySessionId(String sessionId);

    /**
     * 获取用户的聊天历史记录总数
     */
    long getChatHistoryCountByUserId(Long userId);
}
