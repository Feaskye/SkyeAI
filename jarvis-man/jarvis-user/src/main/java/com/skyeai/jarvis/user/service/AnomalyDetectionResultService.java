package com.skyeai.jarvis.user.service;

import com.skyeai.jarvis.user.model.AnomalyDetectionResult;

import java.time.LocalDateTime;
import java.util.List;

public interface AnomalyDetectionResultService {

    /**
     * 创建异常检测结果
     */
    AnomalyDetectionResult createAnomalyDetectionResult(AnomalyDetectionResult result);

    /**
     * 根据ID获取异常检测结果
     */
    AnomalyDetectionResult getAnomalyDetectionResultById(Long id);

    /**
     * 根据用户ID获取异常检测结果
     */
    List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserId(Long userId);

    /**
     * 根据用户ID和异常类型获取异常检测结果
     */
    List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndType(Long userId, String anomalyType);

    /**
     * 根据用户ID和严重程度获取异常检测结果
     */
    List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndSeverity(Long userId, String severity);

    /**
     * 根据用户ID和状态获取异常检测结果
     */
    List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndStatus(Long userId, String status);

    /**
     * 获取用户的最近异常检测结果
     */
    List<AnomalyDetectionResult> getRecentAnomalyDetectionResultsByUserId(Long userId, int limit);

    /**
     * 根据时间范围获取用户的异常检测结果
     */
    List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 获取用户的活跃异常检测结果
     */
    List<AnomalyDetectionResult> getActiveAnomalyDetectionResultsByUserId(Long userId);

    /**
     * 更新异常检测结果
     */
    AnomalyDetectionResult updateAnomalyDetectionResult(Long id, AnomalyDetectionResult result);

    /**
     * 更新异常检测结果状态
     */
    AnomalyDetectionResult updateAnomalyDetectionResultStatus(Long id, String status);

    /**
     * 解决异常检测结果
     */
    AnomalyDetectionResult resolveAnomalyDetectionResult(Long id);

    /**
     * 删除异常检测结果
     */
    void deleteAnomalyDetectionResult(Long id);

    /**
     * 删除用户的所有异常检测结果
     */
    void deleteAnomalyDetectionResultsByUserId(Long userId);

    /**
     * 获取用户的活跃异常检测结果数量
     */
    long getActiveAnomalyDetectionResultCountByUserId(Long userId);

    /**
     * 获取用户在指定时间范围内的异常检测结果数量
     */
    long getAnomalyDetectionResultCountByUserIdAndTypeAndTimeRange(Long userId, String anomalyType, LocalDateTime start, LocalDateTime end);
}
