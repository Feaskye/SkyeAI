package com.skyeai.jarvis.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * RAG缓存
 * 为频繁查询的问题建立缓存，提升检索性能
 */
@Slf4j
@Component
public class RagCache {
    
    /**
     * 答案缓存前缀
     */
    private static final String ANSWER_CACHE_PREFIX = "rag:answer:";
    
    /**
     * 文档缓存前缀
     */
    private static final String DOCUMENT_CACHE_PREFIX = "rag:doc:";
    
    /**
     * 缓存过期时间（分钟）
     */
    @Value("${rag.cache.expire-minutes:10}")
    private int expireMinutes;
    
    /**
     * 最大缓存条目数
     */
    @Value("${rag.cache.max-entries:1000}")
    private int maxEntries;
    
    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 本地缓存（用于高频访问）
     */
    private final ConcurrentHashMap<String, String> localCache = new ConcurrentHashMap<>();
    
    public RagCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 获取缓存的答案
     * @param query 查询内容
     * @return 缓存的答案，如果不存在返回null
     */
    public String getCachedAnswer(String query) {
        String key = ANSWER_CACHE_PREFIX + hashKey(query);
        
        // 先检查本地缓存
        String localResult = localCache.get(key);
        if (localResult != null) {
            log.debug("命中本地缓存: {}", query);
            return localResult;
        }
        
        // 检查Redis缓存
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                String result = cached.toString();
                // 写入本地缓存
                localCache.putIfAbsent(key, result);
                log.debug("命中Redis缓存: {}", query);
                return result;
            }
        } catch (Exception e) {
            log.error("获取缓存答案失败: {}", query, e);
        }
        
        return null;
    }
    
    /**
     * 缓存答案
     * @param query 查询内容
     * @param answer 答案
     */
    public void cacheAnswer(String query, String answer) {
        if (query == null || answer == null) {
            return;
        }
        
        String key = ANSWER_CACHE_PREFIX + hashKey(query);
        
        // 写入本地缓存
        localCache.put(key, answer);
        
        // 写入Redis缓存
        try {
            redisTemplate.opsForValue().set(key, answer, expireMinutes, TimeUnit.MINUTES);
            log.debug("答案已缓存: {}, 长度: {}", query, answer.length());
        } catch (Exception e) {
            log.error("缓存答案失败: {}", query, e);
        }
    }
    
    /**
     * 获取缓存的文档
     * @param query 查询内容
     * @return 缓存的文档列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getCachedDocuments(String query) {
        String key = DOCUMENT_CACHE_PREFIX + hashKey(query);
        
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("命中文档缓存: {}", query);
                return (List<String>) cached;
            }
        } catch (Exception e) {
            log.error("获取缓存文档失败: {}", query, e);
        }
        
        return null;
    }
    
    /**
     * 缓存文档
     * @param query 查询内容
     * @param documents 文档列表
     */
    public void cacheDocuments(String query, List<String> documents) {
        if (query == null || documents == null) {
            return;
        }
        
        String key = DOCUMENT_CACHE_PREFIX + hashKey(query);
        
        try {
            redisTemplate.opsForValue().set(key, documents, expireMinutes, TimeUnit.MINUTES);
            log.debug("文档已缓存: {}, 数量: {}", query, documents.size());
        } catch (Exception e) {
            log.error("缓存文档失败: {}", query, e);
        }
    }
    
    /**
     * 清除所有缓存
     */
    public void clearAll() {
        localCache.clear();
        
        try {
            // 清除Redis中的RAG缓存
            var answerKeys = redisTemplate.keys(ANSWER_CACHE_PREFIX + "*");
            var docKeys = redisTemplate.keys(DOCUMENT_CACHE_PREFIX + "*");
            
            if (answerKeys != null && !answerKeys.isEmpty()) {
                redisTemplate.delete(answerKeys);
            }
            if (docKeys != null && !docKeys.isEmpty()) {
                redisTemplate.delete(docKeys);
            }
            
            log.info("RAG缓存已清除");
        } catch (Exception e) {
            log.error("清除RAG缓存失败", e);
        }
    }
    
    /**
     * 获取缓存统计信息
     * @return 统计信息
     */
    public CacheStats getStats() {
        CacheStats stats = new CacheStats();
        stats.setLocalCacheSize(localCache.size());
        
        try {
            var answerKeys = redisTemplate.keys(ANSWER_CACHE_PREFIX + "*");
            var docKeys = redisTemplate.keys(DOCUMENT_CACHE_PREFIX + "*");
            
            stats.setRedisAnswerCacheSize(answerKeys != null ? answerKeys.size() : 0);
            stats.setRedisDocumentCacheSize(docKeys != null ? docKeys.size() : 0);
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
        }
        
        return stats;
    }
    
    /**
     * 生成缓存键的哈希值
     */
    private String hashKey(String key) {
        // 简单的哈希实现，实际应用中可使用更好的哈希算法
        return Integer.toHexString(key.hashCode());
    }
    
    /**
     * 缓存统计信息
     */
    @lombok.Data
    public static class CacheStats {
        private int localCacheSize;
        private int redisAnswerCacheSize;
        private int redisDocumentCacheSize;
        
        public int getTotalSize() {
            return localCacheSize + redisAnswerCacheSize + redisDocumentCacheSize;
        }
    }
}