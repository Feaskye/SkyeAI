package com.skyeai.jarvis.knowledge.service;

import com.skyeai.jarvis.knowledge.model.UserPreference;
import java.util.List;

/**
 * 用户偏好服务，用于管理用户偏好设置
 */
public interface UserPreferenceService {

    /**
     * 保存用户偏好
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     * @param value 偏好值
     * @param preferenceType 偏好类型
     * @param priority 优先级
     * @return 保存的用户偏好
     */
    UserPreference saveUserPreference(String userId, String preferenceKey, String value, String preferenceType, int priority);

    /**
     * 获取指定用户的所有偏好
     * @param userId 用户ID
     * @return 用户偏好列表
     */
    List<UserPreference> getUserPreferences(String userId);

    /**
     * 获取指定用户的指定偏好
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     * @return 用户偏好列表
     */
    List<UserPreference> getUserPreferences(String userId, String preferenceKey);

    /**
     * 删除指定用户的指定偏好
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     * @return 是否删除成功
     */
    boolean deleteUserPreference(String userId, String preferenceKey);
}
