package com.skyeai.jarvis.user.repository;

import com.skyeai.jarvis.user.model.HealthData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HealthDataRepository extends JpaRepository<HealthData, Long> {

    List<HealthData> findByUserId(Long userId);

    @Query("SELECT hd FROM HealthData hd WHERE hd.userId = ?1 ORDER BY hd.timestamp DESC LIMIT ?2")
    List<HealthData> findRecentByUserId(Long userId, int limit);

    @Query("SELECT hd FROM HealthData hd WHERE hd.userId = ?1 AND hd.timestamp BETWEEN ?2 AND ?3 ORDER BY hd.timestamp DESC")
    List<HealthData> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(hd.heartRate) FROM HealthData hd WHERE hd.userId = ?1 AND hd.heartRate IS NOT NULL AND hd.timestamp BETWEEN ?2 AND ?3")
    Double findAverageHeartRateByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(hd.systolicBloodPressure) FROM HealthData hd WHERE hd.userId = ?1 AND hd.systolicBloodPressure IS NOT NULL AND hd.timestamp BETWEEN ?2 AND ?3")
    Double findAverageSystolicBloodPressureByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(hd.diastolicBloodPressure) FROM HealthData hd WHERE hd.userId = ?1 AND hd.diastolicBloodPressure IS NOT NULL AND hd.timestamp BETWEEN ?2 AND ?3")
    Double findAverageDiastolicBloodPressureByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(hd.sleepDuration) FROM HealthData hd WHERE hd.userId = ?1 AND hd.sleepDuration IS NOT NULL AND hd.timestamp BETWEEN ?2 AND ?3")
    Double findAverageSleepDurationByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(hd.steps) FROM HealthData hd WHERE hd.userId = ?1 AND hd.steps IS NOT NULL AND hd.timestamp BETWEEN ?2 AND ?3")
    Integer findTotalStepsByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(hd.caloriesBurned) FROM HealthData hd WHERE hd.userId = ?1 AND hd.caloriesBurned IS NOT NULL AND hd.timestamp BETWEEN ?2 AND ?3")
    Integer findTotalCaloriesBurnedByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    void deleteByUserId(Long userId);
}
