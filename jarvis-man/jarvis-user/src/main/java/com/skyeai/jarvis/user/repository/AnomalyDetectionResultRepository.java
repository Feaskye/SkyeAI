package com.skyeai.jarvis.user.repository;

import com.skyeai.jarvis.user.model.AnomalyDetectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnomalyDetectionResultRepository extends JpaRepository<AnomalyDetectionResult, Long> {

    List<AnomalyDetectionResult> findByUserId(Long userId);

    List<AnomalyDetectionResult> findByUserIdAndAnomalyType(Long userId, String anomalyType);

    List<AnomalyDetectionResult> findByUserIdAndSeverity(Long userId, String severity);

    List<AnomalyDetectionResult> findByUserIdAndStatus(Long userId, String status);

    @Query("SELECT adr FROM AnomalyDetectionResult adr WHERE adr.userId = ?1 ORDER BY adr.detectedAt DESC LIMIT ?2")
    List<AnomalyDetectionResult> findRecentByUserId(Long userId, int limit);

    @Query("SELECT adr FROM AnomalyDetectionResult adr WHERE adr.userId = ?1 AND adr.detectedAt BETWEEN ?2 AND ?3 ORDER BY adr.detectedAt DESC")
    List<AnomalyDetectionResult> findByUserIdAndDetectedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT adr FROM AnomalyDetectionResult adr WHERE adr.userId = ?1 AND adr.severity = ?2 ORDER BY adr.detectedAt DESC")
    List<AnomalyDetectionResult> findByUserIdAndSeverityOrderByDetectedAtDesc(Long userId, String severity);

    @Query("SELECT adr FROM AnomalyDetectionResult adr WHERE adr.userId = ?1 AND adr.status = 'detected' ORDER BY adr.severity DESC, adr.detectedAt DESC")
    List<AnomalyDetectionResult> findActiveAnomaliesByUserId(Long userId);

    @Query("SELECT COUNT(adr) FROM AnomalyDetectionResult adr WHERE adr.userId = ?1 AND adr.status = 'detected'")
    long countActiveAnomaliesByUserId(Long userId);

    @Query("SELECT COUNT(adr) FROM AnomalyDetectionResult adr WHERE adr.userId = ?1 AND adr.anomalyType = ?2 AND adr.detectedAt BETWEEN ?3 AND ?4")
    long countByUserIdAndAnomalyTypeAndDetectedAtBetween(Long userId, String anomalyType, LocalDateTime start, LocalDateTime end);

    void deleteByUserId(Long userId);
}
