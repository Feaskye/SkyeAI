package com.skyeai.jarvis.user.service.impl;

import com.skyeai.jarvis.user.model.HealthData;
import com.skyeai.jarvis.user.model.User;
import com.skyeai.jarvis.user.repository.HealthDataRepository;
import com.skyeai.jarvis.user.service.HealthDataAggregatorService;
import com.skyeai.jarvis.user.service.HealthDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康数据聚合服务实现类
 */
@Service
public class HealthDataAggregatorServiceImpl implements HealthDataAggregatorService {
    
    @Autowired
    private HealthDataService healthDataService;

    /**
     * 接收健康数据
     */
    @Override
    public HealthData receiveHealthData(HealthData healthData) {
        // 数据清洗和验证
        HealthData cleanedData = cleanAndValidateData(healthData);
        
        // 存储数据
        return healthDataService.createHealthData(cleanedData);
    }
    
    /**
     * 批量接收健康数据
     */
    @Override
    public List<HealthData> receiveHealthDataBatch(List<HealthData> healthDataList) {
        return healthDataList.stream()
                .map(this::receiveHealthData)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据用户ID和时间范围查询健康数据
     */
    @Override
    public List<HealthData> queryHealthData(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return healthDataService.getHealthDataByUserIdAndTimeRange(userId, startTime, endTime);
    }
    
    /**
     * 根据用户ID和数据类型查询健康数据
     */
    @Override
    public List<HealthData> queryHealthDataByType(Long userId, String dataType, LocalDateTime startTime, LocalDateTime endTime) {
        // 这里需要根据实际的数据类型进行过滤
        // 暂时返回所有数据
        return healthDataService.getHealthDataByUserIdAndTimeRange(userId, startTime, endTime);
    }
    
    /**
     * 获取用户最新的健康数据
     */
    @Override
    public HealthData getLatestHealthData(Long userId, String dataType) {
        List<HealthData> recentData = healthDataService.getRecentHealthDataByUserId(userId, 10);
        if (recentData.isEmpty()) {
            return null;
        }
        return recentData.get(0);
    }
    
    /**
     * 获取用户健康数据统计信息
     */
    @Override
    public Map<String, Object> getHealthDataStats(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        List<HealthData> healthDataList = healthDataService.getHealthDataByUserIdAndTimeRange(userId, startTime, endTime);
        
        // 计算统计信息
        if (!healthDataList.isEmpty()) {
            // 心率统计
            List<Integer> heartRates = healthDataList.stream()
                    .filter(data -> data.getHeartRate() != null)
                    .map(HealthData::getHeartRate)
                    .collect(Collectors.toList());
            if (!heartRates.isEmpty()) {
                stats.put("avgHeartRate", heartRates.stream().mapToInt(Integer::intValue).average().orElse(0));
                stats.put("maxHeartRate", heartRates.stream().mapToInt(Integer::intValue).max().orElse(0));
                stats.put("minHeartRate", heartRates.stream().mapToInt(Integer::intValue).min().orElse(0));
            }
            
            // 步数统计
            List<Integer> steps = healthDataList.stream()
                    .filter(data -> data.getSteps() != null)
                    .map(HealthData::getSteps)
                    .collect(Collectors.toList());
            if (!steps.isEmpty()) {
                stats.put("totalSteps", steps.stream().mapToInt(Integer::intValue).sum());
                stats.put("avgSteps", steps.stream().mapToInt(Integer::intValue).average().orElse(0));
            }
            
            // 睡眠统计
            List<Integer> sleepDurations = healthDataList.stream()
                    .filter(data -> data.getSleepDuration() != null)
                    .map(HealthData::getSleepDuration)
                    .collect(Collectors.toList());
            if (!sleepDurations.isEmpty()) {
                stats.put("avgSleepDuration", sleepDurations.stream().mapToInt(Integer::intValue).average().orElse(0));
            }
            
            // 血氧统计
            List<Integer> oxygenSaturations = healthDataList.stream()
                    .filter(data -> data.getOxygenSaturation() != null)
                    .map(HealthData::getOxygenSaturation)
                    .collect(Collectors.toList());
            if (!oxygenSaturations.isEmpty()) {
                stats.put("avgOxygenSaturation", oxygenSaturations.stream().mapToInt(Integer::intValue).average().orElse(0));
            }
        }
        
        stats.put("dataCount", healthDataList.size());
        stats.put("startTime", startTime);
        stats.put("endTime", endTime);
        
        return stats;
    }
    
    /**
     * 模拟从外部API获取健康数据
     */
    @Override
    public List<HealthData> fetchHealthDataFromExternalAPI(Long userId, String source) {
        // 模拟数据，实际项目中应调用真实的外部API
        List<HealthData> mockData = new ArrayList<>();
        
        // 模拟心率数据
        HealthData heartRateData = new HealthData();
        // 创建用户对象（实际项目中应从数据库获取）
        User user = new User();
        user.setId(userId);
        heartRateData.setUser(user);
        heartRateData.setHeartRate(72);
        heartRateData.setTimestamp(LocalDateTime.now().minusMinutes(5));
        mockData.add(heartRateData);
        
        // 模拟睡眠质量数据
        HealthData sleepData = new HealthData();
        sleepData.setUser(user);
        sleepData.setSleepDuration(480); // 8小时
        sleepData.setSleepQuality("good");
        sleepData.setTimestamp(LocalDateTime.now().minusHours(8));
        mockData.add(sleepData);
        
        // 模拟步数数据
        HealthData stepsData = new HealthData();
        stepsData.setUser(user);
        stepsData.setSteps(3500);
        stepsData.setTimestamp(LocalDateTime.now().minusHours(2));
        mockData.add(stepsData);
        
        // 模拟血氧数据
        HealthData oxygenData = new HealthData();
        oxygenData.setUser(user);
        oxygenData.setOxygenSaturation(98);
        oxygenData.setTimestamp(LocalDateTime.now().minusMinutes(10));
        mockData.add(oxygenData);
        
        return mockData;
    }
    
    /**
     * 数据清洗和验证
     */
    private HealthData cleanAndValidateData(HealthData data) {
        // 如果没有时间戳，设置当前时间
        if (data.getTimestamp() == null) {
            data.setTimestamp(LocalDateTime.now());
        }
        
        // 根据数据类型进行验证和清洗
        if (data.getHeartRate() != null) {
            // 心率范围：30-200
            if (data.getHeartRate() < 30 || data.getHeartRate() > 200) {
                System.out.println("警告：心率数据异常，值为 " + data.getHeartRate());
                // 可以选择丢弃或标记异常
            }
        }
        
        if (data.getOxygenSaturation() != null) {
            // 血氧水平范围：70-100
            if (data.getOxygenSaturation() < 70 || data.getOxygenSaturation() > 100) {
                System.out.println("警告：血氧水平数据异常，值为 " + data.getOxygenSaturation());
                // 可以选择丢弃或标记异常
            }
        }
        
        if (data.getSteps() != null && data.getSteps() < 0) {
            // 步数不能为负数
            System.out.println("警告：步数数据异常，值为 " + data.getSteps());
            data.setSteps(0);
        }
        
        return data;
    }
}
