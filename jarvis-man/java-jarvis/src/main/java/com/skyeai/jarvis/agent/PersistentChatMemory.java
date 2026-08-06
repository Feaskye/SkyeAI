package com.skyeai.jarvis.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 持久化对话记忆
 * 使用Redis存储对话历史和摘要，实现会话恢复和历史查询
 */
@Slf4j
@Component
public class PersistentChatMemory {
    
    /**
     * 记忆存储前缀
     */
    private static final String MEMORY_PREFIX = "chat:memory:";
    
    /**
     * 摘要存储前缀
     */
    private static final String SUMMARY_PREFIX = "chat:summary:";
    
    /**
     * 过期时间（小时）
     */
    @Value("${chat.memory.expire-hours:24}")
    private long expireHours;
    
    /**
     * Redis模板
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 内存中的记忆
     */
    private ChatMemory inMemoryMemory;
    
    /**
     * 添加消息到记忆
     * @param sessionId 会话ID
     * @param message 消息
     */
    public void addMessage(String sessionId, Message message) {
        if (inMemoryMemory != null) {
            inMemoryMemory.addMessage(message);
        }
        
        // 异步持久化到Redis
        CompletableFuture.runAsync(() -> {
            try {
                String key = MEMORY_PREFIX + sessionId;
                redisTemplate.opsForList().rightPush(key, message);
                redisTemplate.expire(key, expireHours, TimeUnit.HOURS);
                log.debug("消息已持久化到Redis - sessionId: {}", sessionId);
            } catch (Exception e) {
                log.error("消息持久化失败 - sessionId: {}", sessionId, e);
            }
        });
    }
    
    /**
     * 获取消息列表
     * @param sessionId 会话ID
     * @return 消息列表
     */
    public List<Message> getMessages(String sessionId) {
        // 先从内存获取
        if (inMemoryMemory != null && !inMemoryMemory.getMessages().isEmpty()) {
            return inMemoryMemory.getMessages();
        }
        
        // 从Redis加载
        try {
            String key = MEMORY_PREFIX + sessionId;
            List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
            if (messages != null && !messages.isEmpty()) {
                List<Message> result = new ArrayList<>();
                for (Object obj : messages) {
                    if (obj instanceof Message) {
                        result.add((Message) obj);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.error("从Redis加载消息失败 - sessionId: {}", sessionId, e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 保存摘要
     * @param sessionId 会话ID
     * @param summary 摘要文本
     */
    public void saveSummary(String sessionId, String summary) {
        if (inMemoryMemory != null) {
            inMemoryMemory.setSummary(summary);
        }
        
        // 保存到Redis
        try {
            String key = SUMMARY_PREFIX + sessionId;
            redisTemplate.opsForValue().set(key, summary, expireHours, TimeUnit.HOURS);
            log.debug("摘要已保存到Redis - sessionId: {}, 长度: {}", sessionId, summary.length());
        } catch (Exception e) {
            log.error("摘要保存失败 - sessionId: {}", sessionId, e);
        }
    }
    
    /**
     * 加载摘要
     * @param sessionId 会话ID
     * @return 摘要文本
     */
    public String loadSummary(String sessionId) {
        // 先检查内存
        if (inMemoryMemory != null && inMemoryMemory.getSummary() != null) {
            return inMemoryMemory.getSummary();
        }
        
        // 从Redis加载
        try {
            String key = SUMMARY_PREFIX + sessionId;
            String summary = (String) redisTemplate.opsForValue().get(key);
            if (summary != null && inMemoryMemory != null) {
                inMemoryMemory.setSummary(summary);
            }
            return summary;
        } catch (Exception e) {
            log.error("从Redis加载摘要失败 - sessionId: {}", sessionId, e);
            return null;
        }
    }
    
    /**
     * 删除会话记忆
     * @param sessionId 会话ID
     */
    public void deleteMemory(String sessionId) {
        try {
            String memoryKey = MEMORY_PREFIX + sessionId;
            String summaryKey = SUMMARY_PREFIX + sessionId;
            redisTemplate.delete(memoryKey);
            redisTemplate.delete(summaryKey);
            log.debug("会话记忆已删除 - sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("删除会话记忆失败 - sessionId: {}", sessionId, e);
        }
    }
    
    /**
     * 检查会话是否存在
     * @param sessionId 会话ID
     * @return 是否存在
     */
    public boolean exists(String sessionId) {
        try {
            String key = MEMORY_PREFIX + sessionId;
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("检查会话存在性失败 - sessionId: {}", sessionId, e);
            return false;
        }
    }
    
    /**
     * 设置内存中的记忆实例
     * @param memory 记忆实例
     */
    public void setInMemoryMemory(ChatMemory memory) {
        this.inMemoryMemory = memory;
    }
    
    /**
     * 获取过期时间
     * @return 过期时间（小时）
     */
    public long getExpireHours() {
        return expireHours;
    }
}