package com.skyeai.jarvis.service;

import com.skyeai.jarvis.config.VectorService;
import com.skyeai.jarvis.model.UserPreference;
import com.skyeai.jarvis.repository.UserPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户偏好服务，用于管理用户偏好设置
 */
@Service
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final VectorService vectorService;
    private final TextEmbeddingService textEmbeddingService;

    @Autowired
    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository, 
                               VectorService vectorService, 
                               TextEmbeddingService textEmbeddingService) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.vectorService = vectorService;
        this.textEmbeddingService = textEmbeddingService;
    }

    /**
     * 保存用户偏好
     * @param userPreference 用户偏好对象
     * @return 保存后的用户偏好
     */
    public UserPreference saveUserPreference(UserPreference userPreference) {
        LocalDateTime now = LocalDateTime.now();
        if (userPreference.getCreatedAt() == null) {
            userPreference.setCreatedAt(now);
        }
        userPreference.setUpdatedAt(now);
        
        // 保存到数据库
        UserPreference saved = userPreferenceRepository.save(userPreference);
        
        // 保存到向量数据库
        saveToVectorDatabase(saved);
        
        return saved;
    }

    /**
     * 保存用户偏好到向量数据库
     */
    private void saveToVectorDatabase(UserPreference preference) {
        try {
            // 生成文本向量（使用偏好键和值的组合作为文本）
            String text = preference.getPreferenceKey() + ": " + preference.getValue();
            List<Double> vector = textEmbeddingService.embedText(text);
            
            // 构建payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("user_id", preference.getUserId());
            payload.put("preference_key", preference.getPreferenceKey());
            payload.put("value", preference.getValue());
            payload.put("preference_type", preference.getPreferenceType());
            payload.put("priority", preference.getPriority());
            payload.put("created_at", preference.getCreatedAt().toString());
            payload.put("updated_at", preference.getUpdatedAt().toString());
            
            // 保存到向量数据库
            vectorService.addUserPreferenceVector(
                    preference.getUserId(), 
                    preference.getPreferenceKey(), 
                    vector, 
                    payload);
        } catch (Exception e) {
            // 向量存储失败不影响主流程，只记录日志
            System.err.println("Failed to save user preference to vector database: " + e.getMessage());
        }
    }

    /**
     * 保存用户偏好
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     * @param value 偏好值
     * @return 保存的用户偏好
     */
    public UserPreference saveUserPreference(String userId, String preferenceKey, String value) {
        UserPreference userPreference = new UserPreference();
        userPreference.setUserId(userId);
        userPreference.setPreferenceKey(preferenceKey);
        userPreference.setValue(value);
        userPreference.setCreatedAt(LocalDateTime.now());
        userPreference.setUpdatedAt(LocalDateTime.now());
        return userPreferenceRepository.save(userPreference);
    }

    /**
     * 保存用户偏好（带类型和优先级）
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     * @param value 偏好值
     * @param preferenceType 偏好类型
     * @param priority 优先级
     * @return 保存的用户偏好
     */
    public UserPreference saveUserPreference(String userId, String preferenceKey, String value, String preferenceType, Integer priority) {
        UserPreference userPreference = new UserPreference();
        userPreference.setUserId(userId);
        userPreference.setPreferenceKey(preferenceKey);
        userPreference.setValue(value);
        userPreference.setPreferenceType(preferenceType);
        userPreference.setPriority(priority);
        userPreference.setCreatedAt(LocalDateTime.now());
        userPreference.setUpdatedAt(LocalDateTime.now());
        return userPreferenceRepository.save(userPreference);
    }

    /**
     * 获取指定用户的所有偏好
     * @param userId 用户ID
     * @return 用户偏好列表
     */
    public List<UserPreference> getUserPreferences(String userId) {
        return userPreferenceRepository.findByUserId(userId);
    }

    /**
     * 获取指定用户的指定偏好
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     * @return 用户偏好列表
     */
    public List<UserPreference> getUserPreferences(String userId, String preferenceKey) {
        return userPreferenceRepository.findByUserIdAndPreferenceKey(userId, preferenceKey);
    }

    /**
     * 删除指定用户的指定偏好
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     */
    public void deleteUserPreference(String userId, String preferenceKey) {
        userPreferenceRepository.deleteByUserIdAndPreferenceKey(userId, preferenceKey);
    }

    /**
     * 删除用户偏好
     * @param id 用户偏好ID
     */
    public void deleteUserPreference(Long id) {
        userPreferenceRepository.deleteById(id);
    }

    /**
     * 获取指定用户的指定类型偏好
     * @param userId 用户ID
     * @param preferenceType 偏好类型
     * @return 用户偏好列表
     */
    public List<UserPreference> getUserPreferencesByType(String userId, String preferenceType) {
        return userPreferenceRepository.findByUserIdAndPreferenceType(userId, preferenceType);
    }

    /**
     * 删除指定用户的所有偏好
     * @param userId 用户ID
     */
    public void deleteUserPreferences(String userId) {
        userPreferenceRepository.deleteByUserId(userId);
    }
}
