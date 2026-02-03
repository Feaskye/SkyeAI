package com.skyeai.jarvis.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日程事件实体类
 */
public class ScheduleEvent {

    // 日期时间格式化器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;

    private String title;

    private LocalDateTime dateTime;

    private String description;

    private LocalDateTime createdAt;

    private String repeatType; // daily, weekly, monthly

    private String stockSymbols; // 股票代码，逗号分隔

    private double priceChangeThreshold; // 价格变动阈值（百分比）

    private boolean active; // 是否活跃

    private String lastCheckTime; // 上次检查时间

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public String getStockSymbols() {
        return stockSymbols;
    }

    public void setStockSymbols(String stockSymbols) {
        this.stockSymbols = stockSymbols;
    }

    public double getPriceChangeThreshold() {
        return priceChangeThreshold;
    }

    public void setPriceChangeThreshold(double priceChangeThreshold) {
        this.priceChangeThreshold = priceChangeThreshold;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(String lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    @Override
    public String toString() {
        return "ScheduleEvent{" +
                "id=" + id +
                ", title='" + title + "'" +
                ", dateTime=" + dateTime.format(DATE_TIME_FORMATTER) +
                ", description='" + description + "'" +
                ", repeatType='" + repeatType + "'" +
                ", stockSymbols='" + stockSymbols + "'" +
                ", priceChangeThreshold=" + priceChangeThreshold +
                ", active=" + active +
                '}';
    }
}