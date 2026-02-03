package com.skyeai.jarvis.knowledge.service;

import java.util.Map;
import java.util.Set;

/**
 * 短期记忆服务，用于管理会话上下文和临时信息
 */
public interface ShortTermMemoryService {

    /**
     * 保存会话上下文
     */
    void saveSessionContext(String sessionId, String key, Object value, long expirationSeconds);

    /**
     * 获取会话上下文
     */
    Object getSessionContext(String sessionId, String key);

    /**
     * 获取会话所有上下文
     */
    Map<String, Object> getSessionAllContext(String sessionId);

    /**
     * 删除会话上下文
     */
    void deleteSessionContext(String sessionId, String key);

    /**
     * 清除会话所有上下文
     */
    void clearSessionContext(String sessionId);

    /**
     * 保存临时信息
     */
    void saveTemporaryInfo(String key, Object value, long expirationSeconds);

    /**
     * 获取临时信息
     */
    Object getTemporaryInfo(String key);

    /**
     * 删除临时信息
     */
    void deleteTemporaryInfo(String key);

    /**
     * 设置会话过期时间
     */
    void setSessionExpiration(String sessionId, long expirationSeconds);

    /**
     * 获取会话过期时间
     */
    long getSessionExpiration(String sessionId);

    /**
     * 检查会话是否存在
     */
    boolean isSessionExists(String sessionId);

    /**
     * 获取所有活跃会话
     */
    Set<String> getActiveSessions();

    /**
     * 保存多模态上下文
     */
    void saveMultimodalContext(String sessionId, String modality, Object context, long expirationSeconds);

    /**
     * 获取多模态上下文
     */
    Object getMultimodalContext(String sessionId, String modality);

    /**
     * 批量保存会话上下文
     */
    void saveSessionContextBatch(String sessionId, Map<String, Object> contextMap, long expirationSeconds);

    /**
     * 增加会话计数器
     */
    long incrementSessionCounter(String sessionId, String counterName);

    /**
     * 获取会话计数器
     */
    long getSessionCounter(String sessionId, String counterName);

    /**
     * 重置会话计数器
     */
    void resetSessionCounter(String sessionId, String counterName);

    /**
     * 保存会话状态
     */
    void saveSessionState(String sessionId, String state, long expirationSeconds);

    /**
     * 获取会话状态
     */
    String getSessionState(String sessionId);

    /**
     * 清除过期会话
     */
    void cleanExpiredSessions();

    /**
     * 获取会话大小（键值对数量）
     */
    long getSessionSize(String sessionId);

    /**
     * 复制会话上下文
     */
    void copySessionContext(String sourceSessionId, String targetSessionId, long expirationSeconds);

    /**
     * 合并会话上下文
     */
    void mergeSessionContext(String targetSessionId, String sourceSessionId);
}
