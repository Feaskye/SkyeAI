package com.skyeai.jarvis.knowledge.service;

import com.skyeai.jarvis.knowledge.model.UserPreference;
import com.skyeai.jarvis.knowledge.model.ChatHistory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 长期记忆服务，用于管理用户偏好和历史交互
 */
public interface LongTermMemoryService {

    /**
     * 保存用户偏好
     */
    UserPreference saveUserPreference(String userId, String preferenceKey, String value, String preferenceType);

    /**
     * 获取用户偏好
     */
    UserPreference getUserPreference(String userId, String preferenceKey);

    /**
     * 获取用户所有偏好
     */
    List<UserPreference> getAllUserPreferences(String userId);

    /**
     * 获取用户指定类型的偏好
     */
    List<UserPreference> getUserPreferencesByType(String userId, String preferenceType);

    /**
     * 更新用户偏好
     */
    UserPreference updateUserPreference(String userId, String preferenceKey, String value);

    /**
     * 删除用户偏好
     */
    void deleteUserPreference(String userId, String preferenceKey);

    /**
     * 保存聊天历史
     */
    void saveChatHistory(String userId, String content, String role, String sessionId);

    /**
     * 获取用户聊天历史
     */
    List<ChatHistory> getChatHistory(String userId, int limit);

    /**
     * 获取会话聊天历史
     */
    List<ChatHistory> getSessionChatHistory(String sessionId);

    /**
     * 清理过期聊天历史
     */
    void cleanExpiredChatHistory(int days);

    /**
     * 分析用户偏好
     */
    Map<String, Object> analyzeUserPreferences(String userId);

    /**
     * 提取用户兴趣点
     */
    List<String> extractUserInterests(String userId, int limit);

    /**
     * 获取用户最近的交互
     */
    List<ChatHistory> getRecentInteractions(String userId, int hours);

    /**
     * 批量保存用户偏好
     */
    void saveUserPreferencesBatch(String userId, Map<String, String> preferences, String preferenceType);

    /**
     * 导出用户记忆
     */
    Map<String, Object> exportUserMemory(String userId);

    /**
     * 导入用户记忆
     */
    void importUserMemory(String userId, Map<String, Object> memoryData);

    /**
     * 重置用户记忆
     */
    void resetUserMemory(String userId);
    
    /**
     * 获取用户画像
     */
    Map<String, Object> getUserProfile(String userId);
    
    /**
     * 更新用户画像
     */
    void updateUserProfile(String userId, Map<String, Object> profileData);
    
    /**
     * 基于用户历史交互更新用户画像
     */
    void updateUserProfileFromInteractions(String userId);
    
    /**
     * 保存带有优先级的用户偏好
     */
    UserPreference saveUserPreferenceWithPriority(String userId, String preferenceKey, String value, String preferenceType, int priority);
    
    /**
     * 根据优先级获取用户偏好
     */
    List<UserPreference> getUserPreferencesByPriority(String userId, int minPriority);
}
