package com.skyeai.jarvis.knowledge.service.impl;

import com.skyeai.jarvis.knowledge.service.ShortTermMemoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ShortTermMemoryServiceImpl implements ShortTermMemoryService {

    private static class ExpiringValue {
        private final Object value;
        private final long expirationTime;
        private final int priority;
        private final long tokenCount;

        public ExpiringValue(Object value, long expirationSeconds, int priority, long tokenCount) {
            this.value = value;
            this.expirationTime = System.currentTimeMillis() + (expirationSeconds * 1000);
            this.priority = priority;
            this.tokenCount = tokenCount;
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

        public int getPriority() {
            return priority;
        }

        public long getTokenCount() {
            return tokenCount;
        }
    }

    private static class SessionContext {
        private final Map<String, ExpiringValue> contextMap;
        private long expirationTime;
        private long maxTokenLimit;

        public SessionContext(long expirationSeconds, long maxTokenLimit) {
            this.contextMap = new ConcurrentHashMap<>();
            this.expirationTime = System.currentTimeMillis() + (expirationSeconds * 1000);
            this.maxTokenLimit = maxTokenLimit;
        }

        public SessionContext(long expirationSeconds) {
            this(expirationSeconds, 4096); // 默认token限制
        }

        public Map<String, ExpiringValue> getContextMap() {
            return contextMap;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

        public void setExpirationTime(long expirationSeconds) {
            this.expirationTime = System.currentTimeMillis() + (expirationSeconds * 1000);
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public long getMaxTokenLimit() {
            return maxTokenLimit;
        }

        public void setMaxTokenLimit(long maxTokenLimit) {
            this.maxTokenLimit = maxTokenLimit;
        }

        public long getTotalTokenCount() {
            return contextMap.values().stream()
                    .filter(value -> !value.isExpired())
                    .mapToLong(ExpiringValue::getTokenCount)
                    .sum();
        }
    }

    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();
    private final Map<String, ExpiringValue> temporaryInfo = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ShortTermMemoryServiceImpl() {
        cleanupExecutor.scheduleWithFixedDelay(this::cleanupExpiredItems, 5, 5, TimeUnit.MINUTES);
    }

    private void cleanupExpiredItems() {
        Set<String> expiredSessions = new HashSet<>();
        for (Map.Entry<String, SessionContext> entry : sessions.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredSessions.add(entry.getKey());
                // 从Redis中删除过期会话
                redisTemplate.delete("session:" + entry.getKey());
            } else {
                Map<String, ExpiringValue> contextMap = entry.getValue().getContextMap();
                Set<String> expiredKeys = new HashSet<>();
                for (Map.Entry<String, ExpiringValue> contextEntry : contextMap.entrySet()) {
                    if (contextEntry.getValue().isExpired()) {
                        expiredKeys.add(contextEntry.getKey());
                    }
                }
                for (String key : expiredKeys) {
                    contextMap.remove(key);
                    // 从Redis中删除过期键
                    redisTemplate.delete("session:" + entry.getKey() + ":" + key);
                }
                
                // 压缩上下文以确保不超过token限制
                compressSessionContext(entry.getKey(), entry.getValue());
            }
        }
        for (String sessionId : expiredSessions) {
            sessions.remove(sessionId);
        }

        Set<String> expiredTemporaryKeys = new HashSet<>();
        for (Map.Entry<String, ExpiringValue> entry : temporaryInfo.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredTemporaryKeys.add(entry.getKey());
                // 从Redis中删除过期临时信息
                redisTemplate.delete("temporary:" + entry.getKey());
            }
        }
        for (String key : expiredTemporaryKeys) {
            temporaryInfo.remove(key);
        }
    }

    /**
     * 压缩会话上下文，确保不超过token限制
     */
    private void compressSessionContext(String sessionId, SessionContext session) {
        long totalTokens = session.getTotalTokenCount();
        long maxTokens = session.getMaxTokenLimit();

        if (totalTokens > maxTokens) {
            // 按优先级和时间排序，删除低优先级的项目
            List<Map.Entry<String, ExpiringValue>> sortedEntries = new ArrayList<>(session.getContextMap().entrySet());
            sortedEntries.sort((e1, e2) -> {
                // 先按优先级排序
                int priorityCompare = Integer.compare(e2.getValue().getPriority(), e1.getValue().getPriority());
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                // 再按过期时间排序（最近的优先）
                return Long.compare(e2.getValue().expirationTime, e1.getValue().expirationTime);
            });

            long currentTokens = totalTokens;
            Iterator<Map.Entry<String, ExpiringValue>> iterator = sortedEntries.iterator();

            while (currentTokens > maxTokens && iterator.hasNext()) {
                Map.Entry<String, ExpiringValue> entry = iterator.next();
                currentTokens -= entry.getValue().getTokenCount();
                session.getContextMap().remove(entry.getKey());
                // 从Redis中删除被压缩的键
                redisTemplate.delete("session:" + sessionId + ":" + entry.getKey());
            }
            
            // 更新Redis中的会话信息
            updateSessionInRedis(sessionId, session);
        }
    }

    /**
     * 估算文本的token数量
     */
    private long estimateTokenCount(Object value) {
        if (value == null) {
            return 0;
        }
        String text = value.toString();
        // 简单估算：1 token ≈ 4 characters
        return text.length() / 4 + 1;
    }

    /**
     * 计算上下文的优先级
     */
    private int calculatePriority(String key, Object value) {
        // 基于键名和值类型计算优先级
        int priority = 5; // 默认优先级
        
        // 会话状态和重要标记优先
        if (key.equals("session_state") || key.startsWith("important_")) {
            priority = 10;
        }
        // 多模态上下文次之
        else if (key.startsWith("multimodal_")) {
            priority = 8;
        }
        // 计数器和临时信息优先级较低
        else if (key.startsWith("counter_")) {
            priority = 3;
        }
        
        return priority;
    }

    /**
     * 从Redis加载会话
     */
    private SessionContext loadSessionFromRedis(String sessionId) {
        try {
            String sessionKey = "session:" + sessionId;
            Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(sessionKey);
            if (sessionData.isEmpty()) {
                return null;
            }
            
            // 解析会话数据
            Long maxTokenLimit = (Long) sessionData.getOrDefault("maxTokenLimit", 4096L);
            Long expirationTime = (Long) sessionData.getOrDefault("expirationTime", System.currentTimeMillis() + 3600000L);
            
            SessionContext session = new SessionContext((expirationTime - System.currentTimeMillis()) / 1000, maxTokenLimit);
            
            // 加载会话上下文
            Set<String> keys = redisTemplate.keys("session:" + sessionId + ":*");
            if (keys != null) {
                for (String key : keys) {
                    String contextKey = key.substring(("session:" + sessionId + ":").length());
                    Map<Object, Object> valueData = redisTemplate.opsForHash().entries(key);
                    if (!valueData.isEmpty()) {
                        Object value = valueData.get("value");
                        Object expTimeObj = valueData.get("expirationTime");
                        Object priorityObj = valueData.get("priority");
                        Object tokenCountObj = valueData.get("tokenCount");
                        
                        Long expTime = null;
                        Integer priority = null;
                        Long tokenCount = null;
                        
                        if (expTimeObj != null) {
                            if (expTimeObj instanceof String) {
                                expTime = Long.parseLong((String) expTimeObj);
                            } else if (expTimeObj instanceof Long) {
                                expTime = (Long) expTimeObj;
                            } else if (expTimeObj instanceof Integer) {
                                expTime = ((Integer) expTimeObj).longValue();
                            }
                        }
                        
                        if (priorityObj != null) {
                            if (priorityObj instanceof String) {
                                priority = Integer.parseInt((String) priorityObj);
                            } else if (priorityObj instanceof Integer) {
                                priority = (Integer) priorityObj;
                            } else if (priorityObj instanceof Long) {
                                priority = ((Long) priorityObj).intValue();
                            }
                        }
                        
                        if (tokenCountObj != null) {
                            if (tokenCountObj instanceof String) {
                                tokenCount = Long.parseLong((String) tokenCountObj);
                            } else if (tokenCountObj instanceof Long) {
                                tokenCount = (Long) tokenCountObj;
                            } else if (tokenCountObj instanceof Integer) {
                                tokenCount = ((Integer) tokenCountObj).longValue();
                            }
                        }
                        
                        if (value != null && expTime != null && priority != null && tokenCount != null) {
                            // 创建ExpiringValue对象
                            ExpiringValue expiringValue = new ExpiringValue(
                                    value,
                                    (expTime - System.currentTimeMillis()) / 1000,
                                    priority,
                                    tokenCount
                            );
                            session.getContextMap().put(contextKey, expiringValue);
                        }
                    }
                }
            }
            
            return session;
        } catch (Exception e) {
            System.err.println("Failed to load session from Redis: " + e.getMessage());
            return null;
        }
    }

    /**
     * 更新Redis中的会话信息
     */
    private void updateSessionInRedis(String sessionId, SessionContext session) {
        try {
            String sessionKey = "session:" + sessionId;
            redisTemplate.opsForHash().put(sessionKey, "maxTokenLimit", session.getMaxTokenLimit());
            redisTemplate.opsForHash().put(sessionKey, "expirationTime", session.getExpirationTime());
            redisTemplate.expireAt(sessionKey, new Date(session.getExpirationTime()));
        } catch (Exception e) {
            System.err.println("Failed to update session in Redis: " + e.getMessage());
        }
    }

    /**
     * 保存上下文到Redis
     */
    private void saveContextToRedis(String sessionId, String key, ExpiringValue value, long expirationSeconds) {
        try {
            String contextKey = "session:" + sessionId + ":" + key;
            redisTemplate.opsForHash().put(contextKey, "value", value.getValue());
            redisTemplate.opsForHash().put(contextKey, "expirationTime", value.expirationTime);
            redisTemplate.opsForHash().put(contextKey, "priority", value.getPriority());
            redisTemplate.opsForHash().put(contextKey, "tokenCount", value.getTokenCount());
            redisTemplate.expire(contextKey, expirationSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Failed to save context to Redis: " + e.getMessage());
        }
    }

    @Override
    public void saveSessionContext(String sessionId, String key, Object value, long expirationSeconds) {
        // 尝试从Redis加载会话，如果不存在则创建新会话
        SessionContext session = sessions.computeIfAbsent(sessionId, k -> {
            SessionContext redisSession = loadSessionFromRedis(sessionId);
            return redisSession != null ? redisSession : new SessionContext(expirationSeconds);
        });
        
        int priority = calculatePriority(key, value);
        long tokenCount = estimateTokenCount(value);
        ExpiringValue expiringValue = new ExpiringValue(value, expirationSeconds, priority, tokenCount);
        session.getContextMap().put(key, expiringValue);
        session.setExpirationTime(expirationSeconds);
        
        // 保存到Redis
        saveContextToRedis(sessionId, key, expiringValue, expirationSeconds);
        updateSessionInRedis(sessionId, session);
        
        // 压缩上下文以确保不超过token限制
        compressSessionContext(sessionId, session);
    }

    @Override
    public Object getSessionContext(String sessionId, String key) {
        // 尝试从本地缓存获取
        SessionContext session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            ExpiringValue value = session.getContextMap().get(key);
            if (value != null && !value.isExpired()) {
                return value.getValue();
            } else if (value != null && value.isExpired()) {
                session.getContextMap().remove(key);
                // 从Redis中删除过期键
                redisTemplate.delete("session:" + sessionId + ":" + key);
            }
        } else if (session != null && session.isExpired()) {
            sessions.remove(sessionId);
            // 从Redis中删除过期会话
            redisTemplate.delete("session:" + sessionId);
        }
        
        // 尝试从Redis获取
        try {
            String contextKey = "session:" + sessionId + ":" + key;
            Map<Object, Object> valueData = redisTemplate.opsForHash().entries(contextKey);
            if (!valueData.isEmpty()) {
                Object value = valueData.get("value");
                Long expTime = (Long) valueData.get("expirationTime");
                
                if (value != null && expTime != null && expTime > System.currentTimeMillis()) {
                    // 更新本地缓存
                    if (session == null || session.isExpired()) {
                        session = new SessionContext((expTime - System.currentTimeMillis()) / 1000);
                        sessions.put(sessionId, session);
                    }
                    
                    Integer priority = (Integer) valueData.get("priority");
                    Long tokenCount = (Long) valueData.get("tokenCount");
                    ExpiringValue expiringValue = new ExpiringValue(
                            value,
                            (expTime - System.currentTimeMillis()) / 1000,
                            priority != null ? priority : 5,
                            tokenCount != null ? tokenCount : estimateTokenCount(value)
                    );
                    session.getContextMap().put(key, expiringValue);
                    
                    return value;
                } else if (expTime != null && expTime <= System.currentTimeMillis()) {
                    // 从Redis中删除过期键
                    redisTemplate.delete(contextKey);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get context from Redis: " + e.getMessage());
        }
        
        return null;
    }

    @Override
    public Map<String, Object> getSessionAllContext(String sessionId) {
        // 尝试从本地缓存获取
        SessionContext session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            Map<String, Object> result = new HashMap<>();
            Set<String> expiredKeys = new HashSet<>();
            for (Map.Entry<String, ExpiringValue> entry : session.getContextMap().entrySet()) {
                if (!entry.getValue().isExpired()) {
                    result.put(entry.getKey(), entry.getValue().getValue());
                } else {
                    expiredKeys.add(entry.getKey());
                }
            }
            for (String key : expiredKeys) {
                session.getContextMap().remove(key);
                // 从Redis中删除过期键
                redisTemplate.delete("session:" + sessionId + ":" + key);
            }
            return result;
        } else if (session != null && session.isExpired()) {
            sessions.remove(sessionId);
            // 从Redis中删除过期会话
            redisTemplate.delete("session:" + sessionId);
        }
        
        // 尝试从Redis获取
        try {
            Map<String, Object> result = new HashMap<>();
            Set<String> keys = redisTemplate.keys("session:" + sessionId + ":*");
            if (keys != null) {
                for (String key : keys) {
                    String contextKey = key.substring(("session:" + sessionId + ":").length());
                    Map<Object, Object> valueData = redisTemplate.opsForHash().entries(key);
                    if (!valueData.isEmpty()) {
                        Object value = valueData.get("value");
                        Object expTimeObj = valueData.get("expirationTime");
                        Long expTime = null;
                        
                        if (expTimeObj != null) {
                            if (expTimeObj instanceof String) {
                                expTime = Long.parseLong((String) expTimeObj);
                            } else if (expTimeObj instanceof Long) {
                                expTime = (Long) expTimeObj;
                            } else if (expTimeObj instanceof Integer) {
                                expTime = ((Integer) expTimeObj).longValue();
                            }
                        }
                        
                        if (value != null && expTime != null && expTime > System.currentTimeMillis()) {
                            result.put(contextKey, value);
                        } else if (expTime != null && expTime <= System.currentTimeMillis()) {
                            // 从Redis中删除过期键
                            redisTemplate.delete(key);
                        }
                    }
                }
            }
            
            // 更新本地缓存
            if (!result.isEmpty()) {
                SessionContext newSession = new SessionContext(3600);
                sessions.put(sessionId, newSession);
                
                for (Map.Entry<String, Object> entry : result.entrySet()) {
                    int priority = calculatePriority(entry.getKey(), entry.getValue());
                    long tokenCount = estimateTokenCount(entry.getValue());
                    newSession.getContextMap().put(entry.getKey(), new ExpiringValue(entry.getValue(), 3600, priority, tokenCount));
                }
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("Failed to get all context from Redis: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Override
    public void deleteSessionContext(String sessionId, String key) {
        // 从本地缓存删除
        SessionContext session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            session.getContextMap().remove(key);
        } else if (session != null && session.isExpired()) {
            sessions.remove(sessionId);
        }
        
        // 从Redis删除
        redisTemplate.delete("session:" + sessionId + ":" + key);
    }

    @Override
    public void clearSessionContext(String sessionId) {
        // 从本地缓存删除
        sessions.remove(sessionId);
        
        // 从Redis删除
        try {
            Set<String> keys = redisTemplate.keys("session:" + sessionId + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            System.err.println("Failed to clear session from Redis: " + e.getMessage());
        }
    }

    @Override
    public void saveTemporaryInfo(String key, Object value, long expirationSeconds) {
        int priority = 2; // 临时信息优先级较低
        long tokenCount = estimateTokenCount(value);
        ExpiringValue expiringValue = new ExpiringValue(value, expirationSeconds, priority, tokenCount);
        temporaryInfo.put(key, expiringValue);
        
        // 保存到Redis
        try {
            String redisKey = "temporary:" + key;
            redisTemplate.opsForHash().put(redisKey, "value", value);
            redisTemplate.opsForHash().put(redisKey, "expirationTime", expiringValue.expirationTime);
            redisTemplate.opsForHash().put(redisKey, "priority", priority);
            redisTemplate.opsForHash().put(redisKey, "tokenCount", tokenCount);
            redisTemplate.expire(redisKey, expirationSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Failed to save temporary info to Redis: " + e.getMessage());
        }
    }

    @Override
    public Object getTemporaryInfo(String key) {
        // 从本地缓存获取
        ExpiringValue value = temporaryInfo.get(key);
        if (value != null && !value.isExpired()) {
            return value.getValue();
        } else if (value != null && value.isExpired()) {
            temporaryInfo.remove(key);
            // 从Redis中删除过期键
            redisTemplate.delete("temporary:" + key);
        }
        
        // 从Redis获取
        try {
            String redisKey = "temporary:" + key;
            Map<Object, Object> valueData = redisTemplate.opsForHash().entries(redisKey);
            if (!valueData.isEmpty()) {
                Object tempValue = valueData.get("value");
                Long expTime = (Long) valueData.get("expirationTime");
                
                if (tempValue != null && expTime != null && expTime > System.currentTimeMillis()) {
                    // 更新本地缓存
                    Integer priority = (Integer) valueData.get("priority");
                    Long tokenCount = (Long) valueData.get("tokenCount");
                    ExpiringValue expiringValue = new ExpiringValue(
                            tempValue,
                            (expTime - System.currentTimeMillis()) / 1000,
                            priority != null ? priority : 2,
                            tokenCount != null ? tokenCount : estimateTokenCount(tempValue)
                    );
                    temporaryInfo.put(key, expiringValue);
                    
                    return tempValue;
                } else if (expTime != null && expTime <= System.currentTimeMillis()) {
                    // 从Redis中删除过期键
                    redisTemplate.delete(redisKey);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get temporary info from Redis: " + e.getMessage());
        }
        
        return null;
    }

    @Override
    public void deleteTemporaryInfo(String key) {
        // 从本地缓存删除
        temporaryInfo.remove(key);
        
        // 从Redis删除
        redisTemplate.delete("temporary:" + key);
    }

    @Override
    public void setSessionExpiration(String sessionId, long expirationSeconds) {
        SessionContext session = sessions.get(sessionId);
        if (session != null) {
            session.setExpirationTime(expirationSeconds);
            // 更新Redis中的会话过期时间
            updateSessionInRedis(sessionId, session);
            
            // 更新Redis中各个上下文键的过期时间
            try {
                Set<String> keys = redisTemplate.keys("session:" + sessionId + ":*");
                if (keys != null) {
                    for (String key : keys) {
                        redisTemplate.expire(key, expirationSeconds, TimeUnit.SECONDS);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to update expiration in Redis: " + e.getMessage());
            }
        }
    }

    @Override
    public long getSessionExpiration(String sessionId) {
        SessionContext session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            return (session.getExpirationTime() - System.currentTimeMillis()) / 1000;
        } else if (session != null && session.isExpired()) {
            sessions.remove(sessionId);
            // 从Redis中删除过期会话
            redisTemplate.delete("session:" + sessionId);
        }
        
        // 从Redis获取
        try {
            String sessionKey = "session:" + sessionId;
            Long expTime = (Long) redisTemplate.opsForHash().get(sessionKey, "expirationTime");
            if (expTime != null && expTime > System.currentTimeMillis()) {
                return (expTime - System.currentTimeMillis()) / 1000;
            } else if (expTime != null && expTime <= System.currentTimeMillis()) {
                // 从Redis中删除过期会话
                redisTemplate.delete(sessionKey);
            }
        } catch (Exception e) {
            System.err.println("Failed to get session expiration from Redis: " + e.getMessage());
        }
        
        return 0;
    }

    @Override
    public boolean isSessionExists(String sessionId) {
        // 检查本地缓存
        SessionContext session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            return true;
        } else if (session != null && session.isExpired()) {
            sessions.remove(sessionId);
            // 从Redis中删除过期会话
            redisTemplate.delete("session:" + sessionId);
        }
        
        // 检查Redis
        try {
            String sessionKey = "session:" + sessionId;
            return redisTemplate.hasKey(sessionKey);
        } catch (Exception e) {
            System.err.println("Failed to check session existence in Redis: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Set<String> getActiveSessions() {
        Set<String> activeSessions = new HashSet<>();
        Set<String> expiredSessions = new HashSet<>();
        
        // 检查本地缓存
        for (Map.Entry<String, SessionContext> entry : sessions.entrySet()) {
            if (!entry.getValue().isExpired()) {
                activeSessions.add(entry.getKey());
            } else {
                expiredSessions.add(entry.getKey());
            }
        }
        for (String sessionId : expiredSessions) {
            sessions.remove(sessionId);
            // 从Redis中删除过期会话
            redisTemplate.delete("session:" + sessionId);
        }
        
        // 从Redis获取
        try {
            Set<String> redisKeys = redisTemplate.keys("session:*");
            if (redisKeys != null) {
                for (String key : redisKeys) {
                    if (key.startsWith("session:") && !key.contains(":")) {
                        String sessionId = key.substring("session:".length());
                        activeSessions.add(sessionId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get active sessions from Redis: " + e.getMessage());
        }
        
        return activeSessions;
    }

    @Override
    public void saveMultimodalContext(String sessionId, String modality, Object context, long expirationSeconds) {
        saveSessionContext(sessionId, "multimodal_" + modality, context, expirationSeconds);
    }

    @Override
    public Object getMultimodalContext(String sessionId, String modality) {
        return getSessionContext(sessionId, "multimodal_" + modality);
    }

    @Override
    public void saveSessionContextBatch(String sessionId, Map<String, Object> contextMap, long expirationSeconds) {
        // 尝试从Redis加载会话，如果不存在则创建新会话
        SessionContext session = sessions.computeIfAbsent(sessionId, k -> {
            SessionContext redisSession = loadSessionFromRedis(sessionId);
            return redisSession != null ? redisSession : new SessionContext(expirationSeconds);
        });
        
        for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
            int priority = calculatePriority(entry.getKey(), entry.getValue());
            long tokenCount = estimateTokenCount(entry.getValue());
            ExpiringValue expiringValue = new ExpiringValue(entry.getValue(), expirationSeconds, priority, tokenCount);
            session.getContextMap().put(entry.getKey(), expiringValue);
            
            // 保存到Redis
            saveContextToRedis(sessionId, entry.getKey(), expiringValue, expirationSeconds);
        }
        
        session.setExpirationTime(expirationSeconds);
        updateSessionInRedis(sessionId, session);
        
        // 压缩上下文以确保不超过token限制
        compressSessionContext(sessionId, session);
    }

    @Override
    public long incrementSessionCounter(String sessionId, String counterName) {
        String counterKey = "counter_" + counterName;
        Long currentValue = (Long) getSessionContext(sessionId, counterKey);
        long newValue = (currentValue == null) ? 1 : currentValue + 1;
        saveSessionContext(sessionId, counterKey, newValue, 3600);
        return newValue;
    }

    @Override
    public long getSessionCounter(String sessionId, String counterName) {
        String counterKey = "counter_" + counterName;
        Long value = (Long) getSessionContext(sessionId, counterKey);
        return (value == null) ? 0 : value;
    }

    @Override
    public void resetSessionCounter(String sessionId, String counterName) {
        String counterKey = "counter_" + counterName;
        deleteSessionContext(sessionId, counterKey);
    }

    @Override
    public void saveSessionState(String sessionId, String state, long expirationSeconds) {
        saveSessionContext(sessionId, "session_state", state, expirationSeconds);
    }

    @Override
    public String getSessionState(String sessionId) {
        Object state = getSessionContext(sessionId, "session_state");
        return (state == null) ? null : state.toString();
    }

    @Override
    public void cleanExpiredSessions() {
        cleanupExpiredItems();
    }

    @Override
    public long getSessionSize(String sessionId) {
        SessionContext session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            return session.getContextMap().size();
        } else if (session != null && session.isExpired()) {
            sessions.remove(sessionId);
            // 从Redis中删除过期会话
            redisTemplate.delete("session:" + sessionId);
        }
        
        // 从Redis获取
        try {
            Set<String> keys = redisTemplate.keys("session:" + sessionId + ":*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            System.err.println("Failed to get session size from Redis: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public void copySessionContext(String sourceSessionId, String targetSessionId, long expirationSeconds) {
        Map<String, Object> sourceContext = getSessionAllContext(sourceSessionId);
        if (!sourceContext.isEmpty()) {
            saveSessionContextBatch(targetSessionId, sourceContext, expirationSeconds);
        }
    }

    @Override
    public void mergeSessionContext(String targetSessionId, String sourceSessionId) {
        Map<String, Object> sourceContext = getSessionAllContext(sourceSessionId);
        if (!sourceContext.isEmpty()) {
            SessionContext targetSession = sessions.computeIfAbsent(targetSessionId, k -> new SessionContext(3600));
            for (Map.Entry<String, Object> entry : sourceContext.entrySet()) {
                int priority = calculatePriority(entry.getKey(), entry.getValue());
                long tokenCount = estimateTokenCount(entry.getValue());
                ExpiringValue expiringValue = new ExpiringValue(entry.getValue(), 3600, priority, tokenCount);
                targetSession.getContextMap().put(entry.getKey(), expiringValue);
                
                // 保存到Redis
                saveContextToRedis(targetSessionId, entry.getKey(), expiringValue, 3600);
            }
            
            targetSession.setExpirationTime(3600);
            updateSessionInRedis(targetSessionId, targetSession);
            
            // 压缩上下文以确保不超过token限制
            compressSessionContext(targetSessionId, targetSession);
        }
    }

    /**
     * 设置会话的最大token限制
     */
    public void setSessionMaxTokenLimit(String sessionId, long maxTokenLimit) {
        SessionContext session = sessions.computeIfAbsent(sessionId, k -> new SessionContext(3600, maxTokenLimit));
        session.setMaxTokenLimit(maxTokenLimit);
        updateSessionInRedis(sessionId, session);
        
        // 压缩上下文以确保不超过新的token限制
        compressSessionContext(sessionId, session);
    }

    /**
     * 获取会话的当前token使用情况
     */
    public Map<String, Long> getSessionTokenUsage(String sessionId) {
        SessionContext session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            Map<String, Long> usage = new HashMap<>();
            usage.put("total", session.getTotalTokenCount());
            usage.put("limit", session.getMaxTokenLimit());
            usage.put("remaining", session.getMaxTokenLimit() - session.getTotalTokenCount());
            return usage;
        } else if (session != null && session.isExpired()) {
            sessions.remove(sessionId);
            // 从Redis中删除过期会话
            redisTemplate.delete("session:" + sessionId);
        }
        
        // 从Redis获取
        try {
            String sessionKey = "session:" + sessionId;
            Long maxTokenLimit = (Long) redisTemplate.opsForHash().get(sessionKey, "maxTokenLimit");
            if (maxTokenLimit != null) {
                // 计算总token数
                long totalTokens = 0;
                Set<String> keys = redisTemplate.keys("session:" + sessionId + ":*");
                if (keys != null) {
                    for (String key : keys) {
                        Long tokenCount = (Long) redisTemplate.opsForHash().get(key, "tokenCount");
                        if (tokenCount != null) {
                            totalTokens += tokenCount;
                        }
                    }
                }
                
                Map<String, Long> usage = new HashMap<>();
                usage.put("total", totalTokens);
                usage.put("limit", maxTokenLimit);
                usage.put("remaining", maxTokenLimit - totalTokens);
                return usage;
            }
        } catch (Exception e) {
            System.err.println("Failed to get session token usage from Redis: " + e.getMessage());
        }
        
        return Collections.emptyMap();
    }
}
