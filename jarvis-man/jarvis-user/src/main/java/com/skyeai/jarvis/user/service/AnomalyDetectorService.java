package com.skyeai.jarvis.user.service;

import com.skyeai.jarvis.user.model.AnomalyDetectionResult;
import com.skyeai.jarvis.user.model.HealthData;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 异常检测服务
 * 基于规则引擎检测健康数据中的异常情况
 */
@Service
public interface AnomalyDetectorService {
    
    /**
     * 检测单条健康数据中的异常
     */
    List<AnomalyDetectionResult> detectAnomalies(HealthData healthData);
    
    /**
     * 批量检测健康数据中的异常
     */
    List<AnomalyDetectionResult> detectAnomaliesBatch(List<HealthData> healthDataList);
    
    /**
     * 检测用户最近的健康数据异常
     */
    List<AnomalyDetectionResult> detectRecentAnomalies(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据用户ID查询异常检测结果
     */
    List<AnomalyDetectionResult> queryAnomalyResults(Long userId, LocalDateTime startTime, LocalDateTime endTime);
}
