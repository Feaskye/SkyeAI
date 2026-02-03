package com.skyeai.jarvis.repository;

import com.skyeai.jarvis.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户偏好Repository
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    /**
     * 获取指定用户的所有偏好
     * @param userId 用户ID
     * @return 用户偏好列表
     */
    List<UserPreference> findByUserId(String userId);

    /**
     * 获取指定用户的指定偏好
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     * @return 用户偏好列表
     */
    List<UserPreference> findByUserIdAndPreferenceKey(String userId, String preferenceKey);

    /**
     * 根据用户ID和偏好键删除偏好
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     */
    void deleteByUserIdAndPreferenceKey(String userId, String preferenceKey);

    /**
     * 获取指定用户的指定类型偏好
     * @param userId 用户ID
     * @param preferenceType 偏好类型
     * @return 用户偏好列表
     */
    List<UserPreference> findByUserIdAndPreferenceType(String userId, String preferenceType);

    /**
     * 删除指定用户的所有偏好
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);
}
