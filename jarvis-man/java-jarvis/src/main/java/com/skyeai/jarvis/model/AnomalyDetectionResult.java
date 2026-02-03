package com.skyeai.jarvis.model;

import java.time.LocalDateTime;

/**
 * 异常检测结果模型
 */
public class AnomalyDetectionResult {
    
    /**
     * 异常级别枚举
     */
    public enum Severity {
        LOW,     // 低风险
        MEDIUM,  // 中风险
        HIGH     // 高风险
    }
    
    private String id;
    private String userId;
    private HealthData.DataType dataType;
    private double value;
    private String unit;
    private LocalDateTime timestamp;
    private Severity severity;
    private String description;
    private String recommendedAction;
    private String ruleId;
    
    // Constructors
    public AnomalyDetectionResult() {
    }
    
    public AnomalyDetectionResult(String userId, HealthData.DataType dataType, double value, String unit, LocalDateTime timestamp, Severity severity, String description, String recommendedAction, String ruleId) {
        this.userId = userId;
        this.dataType = dataType;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
        this.severity = severity;
        this.description = description;
        this.recommendedAction = recommendedAction;
        this.ruleId = ruleId;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public HealthData.DataType getDataType() {
        return dataType;
    }
    
    public void setDataType(HealthData.DataType dataType) {
        this.dataType = dataType;
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getRecommendedAction() {
        return recommendedAction;
    }
    
    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    @Override
    public String toString() {
        return "AnomalyDetectionResult{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", dataType=" + dataType +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", timestamp=" + timestamp +
                ", severity=" + severity +
                ", description='" + description + '\'' +
                ", recommendedAction='" + recommendedAction + '\'' +
                ", ruleId='" + ruleId + '\'' +
                '}';
    }
}