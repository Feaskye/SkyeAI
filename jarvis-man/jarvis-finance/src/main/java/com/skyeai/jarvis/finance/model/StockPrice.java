package com.skyeai.jarvis.finance.model;

import java.time.LocalDateTime;

/**
 * 股票价格模型类
 */
public class StockPrice {
    private String id;
    private String stockSymbol;
    private double price;
    private double previousPrice;
    private LocalDateTime timestamp;
    private String source;

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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(double previousPrice) {
        this.previousPrice = previousPrice;
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

    @Override
    public String toString() {
        return "StockPrice{" +
                "id='" + id + '\'' +
                ", stockSymbol='" + stockSymbol + '\'' +
                ", price=" + price +
                ", previousPrice=" + previousPrice +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                '}';
    }
}
