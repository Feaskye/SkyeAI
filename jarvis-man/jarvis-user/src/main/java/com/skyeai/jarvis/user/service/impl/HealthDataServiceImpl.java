package com.skyeai.jarvis.user.service.impl;

import com.skyeai.jarvis.user.model.HealthData;
import com.skyeai.jarvis.user.repository.HealthDataRepository;
import com.skyeai.jarvis.user.service.HealthDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HealthDataServiceImpl implements HealthDataService {

    @Autowired
    private HealthDataRepository healthDataRepository;

    @Override
    public HealthData createHealthData(HealthData healthData) {
        return healthDataRepository.save(healthData);
    }

    @Override
    public HealthData getHealthDataById(Long id) {
        return healthDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Health data not found with id: " + id));
    }

    @Override
    public List<HealthData> getHealthDataByUserId(Long userId) {
        return healthDataRepository.findByUserId(userId);
    }

    @Override
    public List<HealthData> getRecentHealthDataByUserId(Long userId, int limit) {
        return healthDataRepository.findRecentByUserId(userId, limit);
    }

    @Override
    public List<HealthData> getHealthDataByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return healthDataRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }

    @Override
    public HealthData updateHealthData(Long id, HealthData healthData) {
        HealthData existingHealthData = healthDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Health data not found with id: " + id));

        // 更新健康数据记录
        if (healthData.getHeartRate() != null) {
            existingHealthData.setHeartRate(healthData.getHeartRate());
        }
        if (healthData.getSystolicBloodPressure() != null) {
            existingHealthData.setSystolicBloodPressure(healthData.getSystolicBloodPressure());
        }
        if (healthData.getDiastolicBloodPressure() != null) {
            existingHealthData.setDiastolicBloodPressure(healthData.getDiastolicBloodPressure());
        }
        if (healthData.getBloodGlucose() != null) {
            existingHealthData.setBloodGlucose(healthData.getBloodGlucose());
        }
        if (healthData.getBodyTemperature() != null) {
            existingHealthData.setBodyTemperature(healthData.getBodyTemperature());
        }
        if (healthData.getOxygenSaturation() != null) {
            existingHealthData.setOxygenSaturation(healthData.getOxygenSaturation());
        }
        if (healthData.getSteps() != null) {
            existingHealthData.setSteps(healthData.getSteps());
        }
        if (healthData.getCaloriesBurned() != null) {
            existingHealthData.setCaloriesBurned(healthData.getCaloriesBurned());
        }
        if (healthData.getSleepDuration() != null) {
            existingHealthData.setSleepDuration(healthData.getSleepDuration());
        }
        if (healthData.getSleepQuality() != null) {
            existingHealthData.setSleepQuality(healthData.getSleepQuality());
        }
        if (healthData.getActivityLevel() != null) {
            existingHealthData.setActivityLevel(healthData.getActivityLevel());
        }
        if (healthData.getMood() != null) {
            existingHealthData.setMood(healthData.getMood());
        }
        if (healthData.getNotes() != null) {
            existingHealthData.setNotes(healthData.getNotes());
        }
        if (healthData.getTimestamp() != null) {
            existingHealthData.setTimestamp(healthData.getTimestamp());
        }

        return healthDataRepository.save(existingHealthData);
    }

    @Override
    public void deleteHealthData(Long id) {
        healthDataRepository.deleteById(id);
    }

    @Override
    public void deleteHealthDataByUserId(Long userId) {
        healthDataRepository.deleteByUserId(userId);
    }

    @Override
    public Double getAverageHeartRateByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return healthDataRepository.findAverageHeartRateByUserIdAndTimestampBetween(userId, start, end);
    }

    @Override
    public Double getAverageSystolicBloodPressureByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return healthDataRepository.findAverageSystolicBloodPressureByUserIdAndTimestampBetween(userId, start, end);
    }

    @Override
    public Double getAverageDiastolicBloodPressureByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return healthDataRepository.findAverageDiastolicBloodPressureByUserIdAndTimestampBetween(userId, start, end);
    }

    @Override
    public Double getAverageSleepDurationByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return healthDataRepository.findAverageSleepDurationByUserIdAndTimestampBetween(userId, start, end);
    }

    @Override
    public Integer getTotalStepsByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return healthDataRepository.findTotalStepsByUserIdAndTimestampBetween(userId, start, end);
    }

    @Override
    public Integer getTotalCaloriesBurnedByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return healthDataRepository.findTotalCaloriesBurnedByUserIdAndTimestampBetween(userId, start, end);
    }
}
