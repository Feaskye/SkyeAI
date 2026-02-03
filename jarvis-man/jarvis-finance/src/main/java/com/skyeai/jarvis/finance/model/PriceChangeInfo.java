package com.skyeai.jarvis.finance.model;

import java.time.LocalDateTime;

/**
 * 股票价格变动信息模型类
 */
public class PriceChangeInfo {
    private String id;
    private String stockSymbol;
    private double currentPrice;
    private double previousPrice;
    private double changePercent;
    private boolean significantChange;
    private String changeDirection;
    private String recommendation;
    private LocalDateTime timestamp;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(double previousPrice) {
        this.previousPrice = previousPrice;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public boolean isSignificantChange() {
        return significantChange;
    }

    public void setSignificantChange(boolean significantChange) {
        this.significantChange = significantChange;
    }

    public String getChangeDirection() {
        return changeDirection;
    }

    public void setChangeDirection(String changeDirection) {
        this.changeDirection = changeDirection;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PriceChangeInfo{" +
                "id='" + id + '\'' +
                ", stockSymbol='" + stockSymbol + '\'' +
                ", currentPrice=" + currentPrice +
                ", previousPrice=" + previousPrice +
                ", changePercent=" + changePercent +
                ", significantChange=" + significantChange +
                ", changeDirection='" + changeDirection + '\'' +
                ", recommendation='" + recommendation + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
