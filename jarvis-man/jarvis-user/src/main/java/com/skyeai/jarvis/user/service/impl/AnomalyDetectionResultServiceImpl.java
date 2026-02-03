package com.skyeai.jarvis.user.service.impl;

import com.skyeai.jarvis.user.model.AnomalyDetectionResult;
import com.skyeai.jarvis.user.repository.AnomalyDetectionResultRepository;
import com.skyeai.jarvis.user.service.AnomalyDetectionResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnomalyDetectionResultServiceImpl implements AnomalyDetectionResultService {

    @Autowired
    private AnomalyDetectionResultRepository anomalyDetectionResultRepository;

    @Override
    public AnomalyDetectionResult createAnomalyDetectionResult(AnomalyDetectionResult result) {
        return anomalyDetectionResultRepository.save(result);
    }

    @Override
    public AnomalyDetectionResult getAnomalyDetectionResultById(Long id) {
        return anomalyDetectionResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anomaly detection result not found with id: " + id));
    }

    @Override
    public List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserId(Long userId) {
        return anomalyDetectionResultRepository.findByUserId(userId);
    }

    @Override
    public List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndType(Long userId, String anomalyType) {
        return anomalyDetectionResultRepository.findByUserIdAndAnomalyType(userId, anomalyType);
    }

    @Override
    public List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndSeverity(Long userId, String severity) {
        return anomalyDetectionResultRepository.findByUserIdAndSeverity(userId, severity);
    }

    @Override
    public List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndStatus(Long userId, String status) {
        return anomalyDetectionResultRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<AnomalyDetectionResult> getRecentAnomalyDetectionResultsByUserId(Long userId, int limit) {
        return anomalyDetectionResultRepository.findRecentByUserId(userId, limit);
    }

    @Override
    public List<AnomalyDetectionResult> getAnomalyDetectionResultsByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return anomalyDetectionResultRepository.findByUserIdAndDetectedAtBetween(userId, start, end);
    }

    @Override
    public List<AnomalyDetectionResult> getActiveAnomalyDetectionResultsByUserId(Long userId) {
        return anomalyDetectionResultRepository.findActiveAnomaliesByUserId(userId);
    }

    @Override
    public AnomalyDetectionResult updateAnomalyDetectionResult(Long id, AnomalyDetectionResult result) {
        AnomalyDetectionResult existingResult = anomalyDetectionResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anomaly detection result not found with id: " + id));

        // 更新异常检测结果
        if (result.getAnomalyType() != null) {
            existingResult.setAnomalyType(result.getAnomalyType());
        }
        if (result.getSeverity() != null) {
            existingResult.setSeverity(result.getSeverity());
        }
        if (result.getDescription() != null) {
            existingResult.setDescription(result.getDescription());
        }
        if (result.getDetectedBy() != null) {
            existingResult.setDetectedBy(result.getDetectedBy());
        }
        if (result.getConfidence() != null) {
            existingResult.setConfidence(result.getConfidence());
        }
        if (result.getRecommendation() != null) {
            existingResult.setRecommendation(result.getRecommendation());
        }
        if (result.getStatus() != null) {
            existingResult.setStatus(result.getStatus());
        }
        if (result.getDetectedAt() != null) {
            existingResult.setDetectedAt(result.getDetectedAt());
        }
        if (result.getResolvedAt() != null) {
            existingResult.setResolvedAt(result.getResolvedAt());
        }

        return anomalyDetectionResultRepository.save(existingResult);
    }

    @Override
    public AnomalyDetectionResult updateAnomalyDetectionResultStatus(Long id, String status) {
        AnomalyDetectionResult existingResult = anomalyDetectionResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anomaly detection result not found with id: " + id));

        existingResult.setStatus(status);
        if ("resolved".equals(status)) {
            existingResult.setResolvedAt(LocalDateTime.now());
        }

        return anomalyDetectionResultRepository.save(existingResult);
    }

    @Override
    public AnomalyDetectionResult resolveAnomalyDetectionResult(Long id) {
        return updateAnomalyDetectionResultStatus(id, "resolved");
    }

    @Override
    public void deleteAnomalyDetectionResult(Long id) {
        anomalyDetectionResultRepository.deleteById(id);
    }

    @Override
    public void deleteAnomalyDetectionResultsByUserId(Long userId) {
        anomalyDetectionResultRepository.deleteByUserId(userId);
    }

    @Override
    public long getActiveAnomalyDetectionResultCountByUserId(Long userId) {
        return anomalyDetectionResultRepository.countActiveAnomaliesByUserId(userId);
    }

    @Override
    public long getAnomalyDetectionResultCountByUserIdAndTypeAndTimeRange(Long userId, String anomalyType, LocalDateTime start, LocalDateTime end) {
        return anomalyDetectionResultRepository.countByUserIdAndAnomalyTypeAndDetectedAtBetween(userId, anomalyType, start, end);
    }
}
