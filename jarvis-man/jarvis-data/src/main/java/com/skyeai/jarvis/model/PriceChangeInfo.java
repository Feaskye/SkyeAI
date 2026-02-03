package com.skyeai.jarvis.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 股票价格变动信息实体类
 */
@Entity
@Table(name = "price_change_info")
public class PriceChangeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "stock_symbol")
    private String stockSymbol;

    @Column(nullable = false, name = "current_price")
    private double currentPrice;

    @Column(name = "previous_price")
    private double previousPrice;

    @Column(name = "change_percent")
    private double changePercent;

    @Column(name = "is_significant_change")
    private boolean significantChange;

    @Column(name = "change_direction")
    private String changeDirection;

    private String recommendation;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
}