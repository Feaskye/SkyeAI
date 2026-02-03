package com.skyeai.jarvis.model;

import java.time.LocalDateTime;

/**
 * 健康数据模型
 */
public class HealthData {
    
    /**
     * 数据类型枚举
     */
    public enum DataType {
        HEART_RATE,       // 心率
        SLEEP_QUALITY,    // 睡眠质量
        STEPS,            // 步数
        CALORIES,         // 卡路里
        BLOOD_PRESSURE,   // 血压
        BLOOD_GLUCOSE,    // 血糖
        WEIGHT,           // 体重
        OXYGEN_LEVEL      // 血氧水平
    }
    
    private String id;
    private DataType type;
    private double value;
    private String unit;
    private LocalDateTime timestamp;
    private String source; // 数据来源，如Apple Health、小米手环等
    private String userId;
    
    // Constructors
    public HealthData() {
    }
    
    public HealthData(DataType type, double value, String unit, LocalDateTime timestamp, String source, String userId) {
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
        this.source = source;
        this.userId = userId;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public DataType getType() {
        return type;
    }
    
    public void setType(DataType type) {
        this.type = type;
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
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return "HealthData{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}