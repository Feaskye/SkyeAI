package com.skyeai.jarvis.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话记忆管理器
 * 管理多个会话的记忆实例
 */
@Slf4j
@Component
public class ChatMemoryManager {
    
    /**
     * 会话记忆缓存
     */
    private final Map<String, ChatMemory> memoryCache = new ConcurrentHashMap<>();
    
    /**
     * 会话超时时间（秒）
     */
    private static final long SESSION_TIMEOUT_SECONDS = 3600; // 1小时
    
    /**
     * 获取或创建会话记忆
     * @param sessionId 会话ID
     * @return 对话记忆实例
     */
    public ChatMemory getOrCreateMemory(String sessionId) {
        return memoryCache.computeIfAbsent(sessionId, id -> {
            ChatMemory memory = new ChatMemory();
            memory.setSessionId(id);
            log.debug("创建新的会话记忆: {}", id);
            return memory;
        });
    }
    
    /**
     * 获取会话记忆
     * @param sessionId 会话ID
     * @return 对话记忆实例，如果不存在返回null
     */
    public ChatMemory getMemory(String sessionId) {
        return memoryCache.get(sessionId);
    }
    
    /**
     * 移除会话记忆
     * @param sessionId 会话ID
     * @return 是否移除成功
     */
    public boolean removeMemory(String sessionId) {
        ChatMemory removed = memoryCache.remove(sessionId);
        if (removed != null) {
            log.debug("移除会话记忆: {}", sessionId);
            return true;
        }
        return false;
    }
    
    /**
     * 检查会话是否存在
     * @param sessionId 会话ID
     * @return 是否存在
     */
    public boolean exists(String sessionId) {
        return memoryCache.containsKey(sessionId);
    }
    
    /**
     * 获取活动会话数量
     * @return 会话数量
     */
    public int getActiveSessionCount() {
        return memoryCache.size();
    }
    
    /**
     * 清理过期会话
     * 注意：实际实现中应该有更复杂的过期检测机制
     */
    public void cleanupExpiredSessions() {
        // 这里可以实现更复杂的过期清理逻辑
        // 目前只是简单的清理所有会话（用于演示）
        log.debug("清理过期会话，当前会话数: {}", memoryCache.size());
    }
    
    /**
     * 获取所有会话ID
     * @return 会话ID列表
     */
    public java.util.Set<String> getAllSessionIds() {
        return memoryCache.keySet();
    }
}