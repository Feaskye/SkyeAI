package com.skyeai.jarvis.user.controller;

import com.skyeai.jarvis.user.model.HealthData;
import com.skyeai.jarvis.user.service.HealthDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-data")
public class HealthDataController {

    @Autowired
    private HealthDataService healthDataService;

    /**
     * 创建健康数据记录
     */
    @PostMapping
    public HealthData createHealthData(@RequestBody HealthData healthData) {
        return healthDataService.createHealthData(healthData);
    }

    /**
     * 根据ID获取健康数据记录
     */
    @GetMapping("/{id}")
    public HealthData getHealthDataById(@PathVariable Long id) {
        return healthDataService.getHealthDataById(id);
    }

    /**
     * 根据用户ID获取健康数据记录
     */
    @GetMapping("/user/{userId}")
    public List<HealthData> getHealthDataByUserId(@PathVariable Long userId) {
        return healthDataService.getHealthDataByUserId(userId);
    }

    /**
     * 获取用户的最近健康数据记录
     */
    @GetMapping("/user/{userId}/recent")
    public List<HealthData> getRecentHealthDataByUserId(@PathVariable Long userId, @RequestParam(defaultValue = "50") int limit) {
        return healthDataService.getRecentHealthDataByUserId(userId, limit);
    }

    /**
     * 根据时间范围获取用户的健康数据记录
     */
    @GetMapping("/user/{userId}/time-range")
    public List<HealthData> getHealthDataByUserIdAndTimeRange(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        return healthDataService.getHealthDataByUserIdAndTimeRange(userId, startTime, endTime);
    }

    /**
     * 更新健康数据记录
     */
    @PutMapping("/{id}")
    public HealthData updateHealthData(@PathVariable Long id, @RequestBody HealthData healthData) {
        return healthDataService.updateHealthData(id, healthData);
    }

    /**
     * 删除健康数据记录
     */
    @DeleteMapping("/{id}")
    public Map<String, String> deleteHealthData(@PathVariable Long id) {
        healthDataService.deleteHealthData(id);
        return Map.of("message", "Health data deleted successfully");
    }

    /**
     * 删除用户的所有健康数据记录
     */
    @DeleteMapping("/user/{userId}")
    public Map<String, String> deleteHealthDataByUserId(@PathVariable Long userId) {
        healthDataService.deleteHealthDataByUserId(userId);
        return Map.of("message", "All health data deleted successfully for user: " + userId);
    }

    /**
     * 获取用户在指定时间范围内的平均心率
     */
    @GetMapping("/user/{userId}/average-heart-rate")
    public Map<String, Double> getAverageHeartRateByUserIdAndTimeRange(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        Double averageHeartRate = healthDataService.getAverageHeartRateByUserIdAndTimeRange(userId, startTime, endTime);
        return Map.of("averageHeartRate", averageHeartRate);
    }

    /**
     * 获取用户在指定时间范围内的平均收缩压
     */
    @GetMapping("/user/{userId}/average-systolic-bp")
    public Map<String, Double> getAverageSystolicBloodPressureByUserIdAndTimeRange(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        Double averageSystolicBloodPressure = healthDataService.getAverageSystolicBloodPressureByUserIdAndTimeRange(userId, startTime, endTime);
        return Map.of("averageSystolicBloodPressure", averageSystolicBloodPressure);
    }

    /**
     * 获取用户在指定时间范围内的平均舒张压
     */
    @GetMapping("/user/{userId}/average-diastolic-bp")
    public Map<String, Double> getAverageDiastolicBloodPressureByUserIdAndTimeRange(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        Double averageDiastolicBloodPressure = healthDataService.getAverageDiastolicBloodPressureByUserIdAndTimeRange(userId, startTime, endTime);
        return Map.of("averageDiastolicBloodPressure", averageDiastolicBloodPressure);
    }

    /**
     * 获取用户在指定时间范围内的平均睡眠时长
     */
    @GetMapping("/user/{userId}/average-sleep-duration")
    public Map<String, Double> getAverageSleepDurationByUserIdAndTimeRange(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        Double averageSleepDuration = healthDataService.getAverageSleepDurationByUserIdAndTimeRange(userId, startTime, endTime);
        return Map.of("averageSleepDuration", averageSleepDuration);
    }

    /**
     * 获取用户在指定时间范围内的总步数
     */
    @GetMapping("/user/{userId}/total-steps")
    public Map<String, Integer> getTotalStepsByUserIdAndTimeRange(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        Integer totalSteps = healthDataService.getTotalStepsByUserIdAndTimeRange(userId, startTime, endTime);
        return Map.of("totalSteps", totalSteps);
    }

    /**
     * 获取用户在指定时间范围内的总消耗卡路里
     */
    @GetMapping("/user/{userId}/total-calories")
    public Map<String, Integer> getTotalCaloriesBurnedByUserIdAndTimeRange(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        Integer totalCaloriesBurned = healthDataService.getTotalCaloriesBurnedByUserIdAndTimeRange(userId, startTime, endTime);
        return Map.of("totalCaloriesBurned", totalCaloriesBurned);
    }
}
