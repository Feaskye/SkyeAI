package com.skyeai.jarvis.user.service.impl;

import com.skyeai.jarvis.user.model.AnomalyDetectionResult;
import com.skyeai.jarvis.user.model.HealthData;
import com.skyeai.jarvis.user.service.AnomalyDetectionResultService;
import com.skyeai.jarvis.user.service.AnomalyDetectorService;
import com.skyeai.jarvis.user.service.HealthDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 异常检测服务实现类
 */
@Service
public class AnomalyDetectorServiceImpl implements AnomalyDetectorService {
    
    @Autowired
    private HealthDataService healthDataService;
    
    @Autowired
    private AnomalyDetectionResultService anomalyDetectionResultService;
    
    // 异常检测规则配置
    private final ConcurrentHashMap<String, DetectionRule> detectionRules = new ConcurrentHashMap<>();
    
    /**
     * 检测规则内部类
     */
    private static class DetectionRule {
        String id;
        String dataType;
        double minThreshold;
        double maxThreshold;
        String severity;
        String description;
        String recommendedAction;
        
        DetectionRule(String id, String dataType, double minThreshold, double maxThreshold, 
                     String severity, String description, String recommendedAction) {
            this.id = id;
            this.dataType = dataType;
            this.minThreshold = minThreshold;
            this.maxThreshold = maxThreshold;
            this.severity = severity;
            this.description = description;
            this.recommendedAction = recommendedAction;
        }
    }
    
    /**
     * 初始化检测规则
     */
    public AnomalyDetectorServiceImpl() {
        initializeDetectionRules();
    }
    
    /**
     * 初始化检测规则
     */
    private void initializeDetectionRules() {
        // 心率异常规则
        detectionRules.put("rule_heart_rate_high", new DetectionRule(
                "rule_heart_rate_high",
                "heartRate",
                0,
                100,
                "HIGH",
                "心率偏高",
                "建议休息，避免剧烈运动"
        ));
        
        detectionRules.put("rule_heart_rate_critical", new DetectionRule(
                "rule_heart_rate_critical",
                "heartRate",
                0,
                120,
                "CRITICAL",
                "心率过高",
                "建议立即休息，必要时寻求医疗帮助"
        ));
        
        // 血氧水平异常规则
        detectionRules.put("rule_oxygen_level_low", new DetectionRule(
                "rule_oxygen_level_low",
                "oxygenSaturation",
                95,
                100,
                "MEDIUM",
                "血氧水平偏低",
                "建议呼吸新鲜空气，适当休息"
        ));
        
        detectionRules.put("rule_oxygen_level_critical", new DetectionRule(
                "rule_oxygen_level_critical",
                "oxygenSaturation",
                90,
                100,
                "HIGH",
                "血氧水平严重偏低",
                "建议立即就医"
        ));
        
        // 睡眠质量异常规则
        detectionRules.put("rule_sleep_quality_poor", new DetectionRule(
                "rule_sleep_quality_poor",
                "sleepDuration",
                480, // 8小时
                1440, // 24小时
                "LOW",
                "睡眠时长不足",
                "建议调整作息时间，改善睡眠环境"
        ));
        
        System.out.println("异常检测规则已初始化: " + detectionRules.size() + " 条规则");
    }
    
    /**
     * 检测单条健康数据中的异常
     */
    @Override
    public List<AnomalyDetectionResult> detectAnomalies(HealthData healthData) {
        List<AnomalyDetectionResult> anomalies = new ArrayList<>();
        
        // 检查心率异常
        if (healthData.getHeartRate() != null) {
            checkHeartRateAnomaly(healthData, anomalies);
        }
        
        // 检查血氧异常
        if (healthData.getOxygenSaturation() != null) {
            checkOxygenSaturationAnomaly(healthData, anomalies);
        }
        
        // 检查睡眠异常
        if (healthData.getSleepDuration() != null) {
            checkSleepDurationAnomaly(healthData, anomalies);
        }
        
        return anomalies;
    }
    
    /**
     * 批量检测健康数据中的异常
     */
    @Override
    public List<AnomalyDetectionResult> detectAnomaliesBatch(List<HealthData> healthDataList) {
        return healthDataList.stream()
                .flatMap(data -> detectAnomalies(data).stream())
                .collect(Collectors.toList());
    }
    
    /**
     * 检测用户最近的健康数据异常
     */
    @Override
    public List<AnomalyDetectionResult> detectRecentAnomalies(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取用户最近的健康数据
        List<HealthData> recentData = healthDataService.getHealthDataByUserIdAndTimeRange(userId, startTime, endTime);
        
        // 检测异常
        return detectAnomaliesBatch(recentData);
    }
    
    /**
     * 根据用户ID查询异常检测结果
     */
    @Override
    public List<AnomalyDetectionResult> queryAnomalyResults(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return anomalyDetectionResultService.getAnomalyDetectionResultsByUserIdAndTimeRange(userId, startTime, endTime);
    }
    
    /**
     * 检查心率异常
     */
    private void checkHeartRateAnomaly(HealthData healthData, List<AnomalyDetectionResult> anomalies) {
        int heartRate = healthData.getHeartRate();
        
        // 检查心率过高
        if (heartRate > 100) {
            AnomalyDetectionResult result = createAnomalyResult(
                    healthData,
                    "心率偏高",
                    "HIGH",
                    "心率值为 " + heartRate + "，高于正常范围",
                    "建议休息，避免剧烈运动"
            );
            anomalies.add(result);
            anomalyDetectionResultService.createAnomalyDetectionResult(result);
        }
        
        if (heartRate > 120) {
            AnomalyDetectionResult result = createAnomalyResult(
                    healthData,
                    "心率过高",
                    "CRITICAL",
                    "心率值为 " + heartRate + "，严重高于正常范围",
                    "建议立即休息，必要时寻求医疗帮助"
            );
            anomalies.add(result);
            anomalyDetectionResultService.createAnomalyDetectionResult(result);
        }
    }
    
    /**
     * 检查血氧异常
     */
    private void checkOxygenSaturationAnomaly(HealthData healthData, List<AnomalyDetectionResult> anomalies) {
        int oxygenSaturation = healthData.getOxygenSaturation();
        
        // 检查血氧偏低
        if (oxygenSaturation < 95) {
            AnomalyDetectionResult result = createAnomalyResult(
                    healthData,
                    "血氧水平偏低",
                    "MEDIUM",
                    "血氧值为 " + oxygenSaturation + "%，低于正常范围",
                    "建议呼吸新鲜空气，适当休息"
            );
            anomalies.add(result);
            anomalyDetectionResultService.createAnomalyDetectionResult(result);
        }
        
        if (oxygenSaturation < 90) {
            AnomalyDetectionResult result = createAnomalyResult(
                    healthData,
                    "血氧水平严重偏低",
                    "HIGH",
                    "血氧值为 " + oxygenSaturation + "%，严重低于正常范围",
                    "建议立即就医"
            );
            anomalies.add(result);
            anomalyDetectionResultService.createAnomalyDetectionResult(result);
        }
    }
    
    /**
     * 检查睡眠异常
     */
    private void checkSleepDurationAnomaly(HealthData healthData, List<AnomalyDetectionResult> anomalies) {
        int sleepDuration = healthData.getSleepDuration();
        
        // 检查睡眠不足
        if (sleepDuration < 480) { // 少于8小时
            AnomalyDetectionResult result = createAnomalyResult(
                    healthData,
                    "睡眠时长不足",
                    "LOW",
                    "睡眠时长为 " + sleepDuration + " 分钟，少于建议的8小时",
                    "建议调整作息时间，改善睡眠环境"
            );
            anomalies.add(result);
            anomalyDetectionResultService.createAnomalyDetectionResult(result);
        }
    }
    
    /**
     * 创建异常检测结果
     */
    private AnomalyDetectionResult createAnomalyResult(HealthData healthData, 
                                                     String anomalyType, 
                                                     String severity, 
                                                     String description, 
                                                     String recommendedAction) {
        AnomalyDetectionResult result = new AnomalyDetectionResult();
        result.setUser(healthData.getUser());
        result.setAnomalyType(anomalyType);
        result.setSeverity(severity);
        result.setDescription(description);
        result.setDetectedBy("rule_engine");
        result.setConfidence(0.9);
        result.setRecommendation(recommendedAction);
        result.setStatus("detected");
        result.setDetectedAt(LocalDateTime.now());
        
        return result;
    }
}
