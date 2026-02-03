package com.skyeai.jarvis.user.controller;

import com.skyeai.jarvis.user.model.AnomalyDetectionResult;
import com.skyeai.jarvis.user.service.AnomalyDetectionResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anomaly-detection-results")
public class AnomalyDetectionResultController {

    @Autowired
    private AnomalyDetectionResultService anomalyDetectionResultService;

    /**
     * 创建异常检测结果
     */
    @PostMapping
    public AnomalyDetectionResult createAnomalyDetectionResult(@RequestBody AnomalyDetectionResult result) {
        return anomalyDetectionResultService.createAnomalyDetectionResult(result);
    }

    /**
     * 根据ID获取异常检测结果
     */
    @GetMapping("/{id}")
    public AnomalyDetectionResult getAnomalyDetectionResultById(@PathVariable Long id) {
        return anomalyDetectionResultService.getAnomalyDetectionResultById(id);
    }

    /**
     * 根据用户ID获取异常检测结果
     */
    @GetMapping("/user/{userId}")
    public List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserId(@PathVariable Long userId) {
        return anomalyDetectionResultService.getAnomalyDetectionResultsByUserId(userId);
    }

    /**
     * 根据用户ID和异常类型获取异常检测结果
     */
    @GetMapping("/user/{userId}/type/{anomalyType}")
    public List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndType(@PathVariable Long userId, @PathVariable String anomalyType) {
        return anomalyDetectionResultService.getAnomalyDetectionResultsByUserIdAndType(userId, anomalyType);
    }

    /**
     * 根据用户ID和严重程度获取异常检测结果
     */
    @GetMapping("/user/{userId}/severity/{severity}")
    public List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndSeverity(@PathVariable Long userId, @PathVariable String severity) {
        return anomalyDetectionResultService.getAnomalyDetectionResultsByUserIdAndSeverity(userId, severity);
    }

    /**
     * 根据用户ID和状态获取异常检测结果
     */
    @GetMapping("/user/{userId}/status/{status}")
    public List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndStatus(@PathVariable Long userId, @PathVariable String status) {
        return anomalyDetectionResultService.getAnomalyDetectionResultsByUserIdAndStatus(userId, status);
    }

    /**
     * 获取用户的最近异常检测结果
     */
    @GetMapping("/user/{userId}/recent")
    public List<AnomalyDetectionResult> getRecentAnomalyDetectionResultsByUserId(@PathVariable Long userId, @RequestParam(defaultValue = "50") int limit) {
        return anomalyDetectionResultService.getRecentAnomalyDetectionResultsByUserId(userId, limit);
    }

    /**
     * 根据时间范围获取用户的异常检测结果
     */
    @GetMapping("/user/{userId}/time-range")
    public List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndTimeRange(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        return anomalyDetectionResultService.getAnomalyDetectionResultsByUserIdAndTimeRange(userId, startTime, endTime);
    }

    /**
     * 获取用户的活跃异常检测结果
     */
    @GetMapping("/user/{userId}/active")
    public List<AnomalyDetectionResult> getActiveAnomalyDetectionResultsByUserId(@PathVariable Long userId) {
        return anomalyDetectionResultService.getActiveAnomalyDetectionResultsByUserId(userId);
    }

    /**
     * 更新异常检测结果
     */
    @PutMapping("/{id}")
    public AnomalyDetectionResult updateAnomalyDetectionResult(@PathVariable Long id, @RequestBody AnomalyDetectionResult result) {
        return anomalyDetectionResultService.updateAnomalyDetectionResult(id, result);
    }

    /**
     * 更新异常检测结果状态
     */
    @PutMapping("/{id}/status")
    public AnomalyDetectionResult updateAnomalyDetectionResultStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        return anomalyDetectionResultService.updateAnomalyDetectionResultStatus(id, status);
    }

    /**
     * 解决异常检测结果
     */
    @PutMapping("/{id}/resolve")
    public AnomalyDetectionResult resolveAnomalyDetectionResult(@PathVariable Long id) {
        return anomalyDetectionResultService.resolveAnomalyDetectionResult(id);
    }

    /**
     * 删除异常检测结果
     */
    @DeleteMapping("/{id}")
    public Map<String, String> deleteAnomalyDetectionResult(@PathVariable Long id) {
        anomalyDetectionResultService.deleteAnomalyDetectionResult(id);
        return Map.of("message", "Anomaly detection result deleted successfully");
    }

    /**
     * 删除用户的所有异常检测结果
     */
    @DeleteMapping("/user/{userId}")
    public Map<String, String> deleteAnomalyDetectionResultsByUserId(@PathVariable Long userId) {
        anomalyDetectionResultService.deleteAnomalyDetectionResultsByUserId(userId);
        return Map.of("message", "All anomaly detection results deleted successfully for user: " + userId);
    }

    /**
     * 获取用户的活跃异常检测结果数量
     */
    @GetMapping("/user/{userId}/active/count")
    public Map<String, Long> getActiveAnomalyDetectionResultCountByUserId(@PathVariable Long userId) {
        long count = anomalyDetectionResultService.getActiveAnomalyDetectionResultCountByUserId(userId);
        return Map.of("count", count);
    }

    /**
     * 获取用户在指定时间范围内的异常检测结果数量
     */
    @GetMapping("/user/{userId}/count")
    public Map<String, Long> getAnomalyDetectionResultCountByUserIdAndTypeAndTimeRange(
            @PathVariable Long userId,
            @RequestParam String anomalyType,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        long count = anomalyDetectionResultService.getAnomalyDetectionResultCountByUserIdAndTypeAndTimeRange(userId, anomalyType, startTime, endTime);
        return Map.of("count", count);
    }
}
