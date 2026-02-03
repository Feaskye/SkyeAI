package com.skyeai.jarvis.config;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 向量服务类，用于处理向量数据库相关操作
 * 注意：当前使用内存存储作为Qdrant向量数据库的占位符实现
 */
@Service
public class VectorService {

    private static final String CHAT_HISTORY_COLLECTION = "chat_history";
    private static final String USER_PREFERENCE_COLLECTION = "user_preference";
    
    // 内存存储作为占位符
    private final Map<String, List<Map<String, Object>>> collections;

    public VectorService() {
        this.collections = new HashMap<>();
        initCollections();
    }

    /**
     * 初始化向量集合
     */
    private void initCollections() {
        // 初始化聊天历史集合
        collections.put(CHAT_HISTORY_COLLECTION, new ArrayList<>());
        
        // 初始化用户偏好集合
        collections.put(USER_PREFERENCE_COLLECTION, new ArrayList<>());
    }

    /**
     * 添加聊天历史向量
     */
    public void addChatHistoryVector(String documentId, List<Double> vector, Map<String, Object> payload) {
        try {
            // 创建点结构
            Map<String, Object> point = new HashMap<>();
            point.put("id", UUID.randomUUID().toString());
            point.put("vector", vector);
            point.put("payload", payload);
            
            // 添加到内存集合
            collections.get(CHAT_HISTORY_COLLECTION).add(point);
            
            // 打印日志，模拟向量存储操作
            System.out.println("Added chat history vector to in-memory storage");
        } catch (Exception e) {
            throw new RuntimeException("Failed to add chat history vector", e);
        }
    }

    /**
     * 添加用户偏好向量
     */
    public void addUserPreferenceVector(String userId, String preferenceKey, List<Double> vector, Map<String, Object> payload) {
        try {
            // 创建点结构
            Map<String, Object> point = new HashMap<>();
            point.put("id", UUID.randomUUID().toString());
            point.put("vector", vector);
            point.put("payload", payload);
            
            // 添加到内存集合
            collections.get(USER_PREFERENCE_COLLECTION).add(point);
            
            // 打印日志，模拟向量存储操作
            System.out.println("Added user preference vector to in-memory storage");
        } catch (Exception e) {
            throw new RuntimeException("Failed to add user preference vector", e);
        }
    }

    /**
     * 搜索相似的聊天历史
     */
    public List<Map<String, Object>> searchSimilarChatHistory(List<Double> queryVector, int limit, Map<String, Object> filter) {
        try {
            // 从内存集合中获取数据
            List<Map<String, Object>> points = collections.get(CHAT_HISTORY_COLLECTION);
            List<Map<String, Object>> results = new ArrayList<>();
            
            // 简单过滤和限制结果数量
            int count = 0;
            for (Map<String, Object> point : points) {
                if (count >= limit) break;
                
                // 简单过滤逻辑
                if (filter != null) {
                    Map<String, Object> payload = (Map<String, Object>) point.get("payload");
                    boolean match = true;
                    for (Map.Entry<String, Object> entry : filter.entrySet()) {
                        if (!payload.containsKey(entry.getKey()) || !payload.get(entry.getKey()).equals(entry.getValue())) {
                            match = false;
                            break;
                        }
                    }
                    if (!match) continue;
                }
                
                // 添加到结果中
                results.add(point);
                count++;
            }
            
            // 打印日志，模拟向量搜索操作
            System.out.println("Searched similar chat history in in-memory storage");
            
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search similar chat history", e);
        }
    }

    /**
     * 搜索相似的用户偏好
     */
    public List<Map<String, Object>> searchSimilarUserPreference(List<Double> queryVector, int limit, Map<String, Object> filter) {
        try {
            // 从内存集合中获取数据
            List<Map<String, Object>> points = collections.get(USER_PREFERENCE_COLLECTION);
            List<Map<String, Object>> results = new ArrayList<>();
            
            // 简单过滤和限制结果数量
            int count = 0;
            for (Map<String, Object> point : points) {
                if (count >= limit) break;
                
                // 简单过滤逻辑
                if (filter != null) {
                    Map<String, Object> payload = (Map<String, Object>) point.get("payload");
                    boolean match = true;
                    for (Map.Entry<String, Object> entry : filter.entrySet()) {
                        if (!payload.containsKey(entry.getKey()) || !payload.get(entry.getKey()).equals(entry.getValue())) {
                            match = false;
                            break;
                        }
                    }
                    if (!match) continue;
                }
                
                // 添加到结果中
                results.add(point);
                count++;
            }
            
            // 打印日志，模拟向量搜索操作
            System.out.println("Searched similar user preference in in-memory storage");
            
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search similar user preference", e);
        }
    }

    /**
     * 删除聊天历史向量
     */
    public void deleteChatHistoryVector(String pointId) {
        try {
            // 从内存集合中删除数据
            List<Map<String, Object>> points = collections.get(CHAT_HISTORY_COLLECTION);
            points.removeIf(point -> point.get("id").equals(pointId));
            
            // 打印日志，模拟向量删除操作
            System.out.println("Deleted chat history vector from in-memory storage");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete chat history vector", e);
        }
    }

    /**
     * 删除用户偏好向量
     */
    public void deleteUserPreferenceVector(String pointId) {
        try {
            // 从内存集合中删除数据
            List<Map<String, Object>> points = collections.get(USER_PREFERENCE_COLLECTION);
            points.removeIf(point -> point.get("id").equals(pointId));
            
            // 打印日志，模拟向量删除操作
            System.out.println("Deleted user preference vector from in-memory storage");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user preference vector", e);
        }
    }

    /**
     * 获取集合统计信息
     */
    public Map<String, Object> getCollectionInfo(String collectionName) {
        try {
            // 从内存集合中获取统计信息
            List<Map<String, Object>> points = collections.get(collectionName);
            Map<String, Object> info = new HashMap<>();
            info.put("collection_name", collectionName);
            info.put("point_count", points != null ? points.size() : 0);
            
            // 打印日志，模拟获取集合信息操作
            System.out.println("Got collection info from in-memory storage");
            
            return info;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get collection info", e);
        }
    }
}
