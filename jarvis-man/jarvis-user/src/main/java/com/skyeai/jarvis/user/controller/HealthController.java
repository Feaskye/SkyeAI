package com.skyeai.jarvis.user.controller;

import com.skyeai.jarvis.user.model.HealthData;
import com.skyeai.jarvis.user.service.HealthDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康数据控制器，用于处理健康数据相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/api/user/health")
public class HealthController {

    @Autowired
    private HealthDataService healthDataService;

    /**
     * 接收健康数据
     */
    @PostMapping("/receive")
    public ResponseEntity<Map<String, Object>> receiveHealthData(@RequestBody Map<String, Object> request) {
        try {
            log.info("接收健康数据: {}", request);
            
            // 创建健康数据对象
            HealthData healthData = new HealthData();
            
            // 设置健康数据属性
            if (request.containsKey("heartRate")) {
                healthData.setHeartRate(((Number) request.get("heartRate")).intValue());
            }
            if (request.containsKey("systolicBloodPressure")) {
                healthData.setSystolicBloodPressure(((Number) request.get("systolicBloodPressure")).intValue());
            }
            if (request.containsKey("diastolicBloodPressure")) {
                healthData.setDiastolicBloodPressure(((Number) request.get("diastolicBloodPressure")).intValue());
            }
            if (request.containsKey("bloodGlucose")) {
                healthData.setBloodGlucose(((Number) request.get("bloodGlucose")).doubleValue());
            }
            if (request.containsKey("bodyTemperature")) {
                healthData.setBodyTemperature(((Number) request.get("bodyTemperature")).doubleValue());
            }
            if (request.containsKey("oxygenSaturation")) {
                healthData.setOxygenSaturation(((Number) request.get("oxygenSaturation")).intValue());
            }
            if (request.containsKey("steps")) {
                healthData.setSteps(((Number) request.get("steps")).intValue());
            }
            if (request.containsKey("caloriesBurned")) {
                healthData.setCaloriesBurned(((Number) request.get("caloriesBurned")).intValue());
            }
            if (request.containsKey("sleepDuration")) {
                healthData.setSleepDuration(((Number) request.get("sleepDuration")).intValue());
            }
            if (request.containsKey("sleepQuality")) {
                healthData.setSleepQuality((String) request.get("sleepQuality"));
            }
            if (request.containsKey("activityLevel")) {
                healthData.setActivityLevel((String) request.get("activityLevel"));
            }
            if (request.containsKey("mood")) {
                healthData.setMood((String) request.get("mood"));
            }
            if (request.containsKey("notes")) {
                healthData.setNotes((String) request.get("notes"));
            }
            if (request.containsKey("timestamp")) {
                healthData.setTimestamp((LocalDateTime) request.get("timestamp"));
            }
            
            // 保存健康数据
            HealthData savedData = healthDataService.createHealthData(healthData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "健康数据接收成功");
            response.put("data", savedData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("接收健康数据失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "接收健康数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取健康数据统计
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getHealthStats(@PathVariable String userId) {
        try {
            log.info("获取用户健康数据统计: {}", userId);
            Map<String, Object> response = new HashMap<>();
            // 这里可以添加健康数据统计逻辑
            response.put("success", true);
            response.put("message", "获取健康数据统计成功");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取健康数据统计失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "获取健康数据统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取健康数据趋势
     */
    @GetMapping("/trend/{userId}")
    public ResponseEntity<Map<String, Object>> getHealthTrend(@PathVariable String userId) {
        try {
            log.info("获取用户健康数据趋势: {}", userId);
            Map<String, Object> response = new HashMap<>();
            // 这里可以添加健康数据趋势分析逻辑
            response.put("success", true);
            response.put("message", "获取健康数据趋势成功");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取健康数据趋势失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "获取健康数据趋势失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
