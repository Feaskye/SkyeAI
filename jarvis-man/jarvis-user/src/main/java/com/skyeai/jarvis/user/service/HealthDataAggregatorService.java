package com.skyeai.jarvis.user.service;

import com.skyeai.jarvis.user.model.HealthData;
import com.skyeai.jarvis.user.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 健康数据聚合服务
 * 负责从不同来源获取健康数据，进行清洗、存储和分析
 */
@Service
public interface HealthDataAggregatorService {
    
    /**
     * 接收健康数据
     */
    HealthData receiveHealthData(HealthData healthData);
    
    /**
     * 批量接收健康数据
     */
    List<HealthData> receiveHealthDataBatch(List<HealthData> healthDataList);
    
    /**
     * 根据用户ID和时间范围查询健康数据
     */
    List<HealthData> queryHealthData(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据用户ID和数据类型查询健康数据
     */
    List<HealthData> queryHealthDataByType(Long userId, String dataType, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取用户最新的健康数据
     */
    HealthData getLatestHealthData(Long userId, String dataType);
    
    /**
     * 获取用户健康数据统计信息
     */
    Map<String, Object> getHealthDataStats(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 模拟从外部API获取健康数据
     */
    List<HealthData> fetchHealthDataFromExternalAPI(Long userId, String source);
}
