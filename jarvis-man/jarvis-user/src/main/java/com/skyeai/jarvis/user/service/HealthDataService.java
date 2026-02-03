package com.skyeai.jarvis.user.service;

import com.skyeai.jarvis.user.model.HealthData;

import java.time.LocalDateTime;
import java.util.List;

public interface HealthDataService {

    /**
     * 创建健康数据记录
     */
    HealthData createHealthData(HealthData healthData);

    /**
     * 根据ID获取健康数据记录
     */
    HealthData getHealthDataById(Long id);

    /**
     * 根据用户ID获取健康数据记录
     */
    List<HealthData> getHealthDataByUserId(Long userId);

    /**
     * 获取用户的最近健康数据记录
     */
    List<HealthData> getRecentHealthDataByUserId(Long userId, int limit);

    /**
     * 根据时间范围获取用户的健康数据记录
     */
    List<HealthData> getHealthDataByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 更新健康数据记录
     */
    HealthData updateHealthData(Long id, HealthData healthData);

    /**
     * 删除健康数据记录
     */
    void deleteHealthData(Long id);

    /**
     * 删除用户的所有健康数据记录
     */
    void deleteHealthDataByUserId(Long userId);

    /**
     * 获取用户在指定时间范围内的平均心率
     */
    Double getAverageHeartRateByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 获取用户在指定时间范围内的平均收缩压
     */
    Double getAverageSystolicBloodPressureByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 获取用户在指定时间范围内的平均舒张压
     */
    Double getAverageDiastolicBloodPressureByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 获取用户在指定时间范围内的平均睡眠时长
     */
    Double getAverageSleepDurationByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 获取用户在指定时间范围内的总步数
     */
    Integer getTotalStepsByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 获取用户在指定时间范围内的总消耗卡路里
     */
    Integer getTotalCaloriesBurnedByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);
}
