package com.skyeai.jarvis.finance.service;

import com.skyeai.jarvis.finance.model.PriceChangeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 股票监测服务，用于获取股票价格和检测价格变动
 */
@Service
public class StockMonitorService {

    // 股票名称到股票代码的映射
    private final Map<String, String> stockCodeMap = new HashMap<>();
    // HTTP客户端
    private final RestTemplate restTemplate;
    // Redis模板
    private final RedisTemplate<String, Object> redisTemplate;
    // 缓存过期时间（毫秒）
    private final long cacheExpiry;
    // 新浪股票API URL
    private final String sinaApiUrl;
    
    // 内存存储，用于模拟数据库
    private final Map<String, Double> stockPrices = new HashMap<>();
    private final Map<String, Double> previousStockPrices = new HashMap<>();

    /**
     * 初始化股票代码映射
     */
    @Autowired
    public StockMonitorService(RestTemplate restTemplate, 
                              RedisTemplate<String, Object> redisTemplate,
                              @Value("${finance.stock.cache.expiry}") long cacheExpiry,
                              @Value("${finance.stock.api.sina.url}") String sinaApiUrl) {
        // 初始化股票名称到股票代码的映射
        stockCodeMap.put("海能达", "sz002583"); // 海能达，深市
        stockCodeMap.put("大位科技", "sz002214"); // 大立科技，深市（假设大位科技是大立科技）
        
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.cacheExpiry = cacheExpiry;
        this.sinaApiUrl = sinaApiUrl;
        
        // 初始化一些默认股票价格
        stockPrices.put("sz002583", 10.50);
        stockPrices.put("sz002214", 15.20);
        previousStockPrices.put("sz002583", 10.30);
        previousStockPrices.put("sz002214", 14.80);
    }

    /**
     * 获取股票当前价格
     * @param stockSymbol 股票代码或名称
     * @return 股票当前价格
     */
    public double getStockPrice(String stockSymbol) {
        try {
            // 从Redis缓存中获取股票价格
            Double cachedPrice = (Double) redisTemplate.opsForValue().get("stock:price:" + stockSymbol);
            if (cachedPrice != null) {
                return cachedPrice;
            }

            // 获取股票代码
            String stockCode = stockSymbol;
            if (stockCodeMap.containsKey(stockSymbol)) {
                stockCode = stockCodeMap.get(stockSymbol);
            }

            // 模拟获取股票价格
            double price = getSimulatedStockPrice(stockCode);

            // 保存到Redis缓存
            redisTemplate.opsForValue().set("stock:price:" + stockSymbol, price, cacheExpiry, TimeUnit.MILLISECONDS);

            // 保存到内存存储
            saveStockPriceToMemory(stockSymbol, price);

            return price;
        } catch (Exception e) {
            // 如果获取失败，返回默认价格
            System.err.println("获取股票价格失败: " + e.getMessage());
            return getDefaultStockPrice(stockSymbol);
        }
    }

    /**
     * 模拟获取股票价格
     * @param stockCode 股票代码
     * @return 模拟的股票价格
     */
    private double getSimulatedStockPrice(String stockCode) {
        // 如果有缓存的价格，返回缓存的价格
        if (stockPrices.containsKey(stockCode)) {
            // 随机波动价格
            double basePrice = stockPrices.get(stockCode);
            double randomChange = (Math.random() - 0.5) * 0.1; // -5% 到 +5% 的随机波动
            double newPrice = basePrice * (1 + randomChange);
            
            // 更新价格
            previousStockPrices.put(stockCode, basePrice);
            stockPrices.put(stockCode, newPrice);
            
            return newPrice;
        }
        
        // 否则返回默认价格
        return 10.0 + Math.random() * 20.0;
    }

    /**
     * 保存股票价格到内存存储
     * @param stockSymbol 股票代码
     * @param price 股票价格
     */
    private void saveStockPriceToMemory(String stockSymbol, double price) {
        String stockCode = stockSymbol;
        if (stockCodeMap.containsKey(stockSymbol)) {
            stockCode = stockCodeMap.get(stockSymbol);
        }
        
        if (stockPrices.containsKey(stockCode)) {
            previousStockPrices.put(stockCode, stockPrices.get(stockCode));
        } else {
            previousStockPrices.put(stockCode, price);
        }
        stockPrices.put(stockCode, price);
    }

    /**
     * 获取默认股票价格
     * @param stockSymbol 股票代码
     * @return 默认股票价格
     */
    private double getDefaultStockPrice(String stockSymbol) {
        String stockCode = stockSymbol;
        if (stockCodeMap.containsKey(stockSymbol)) {
            stockCode = stockCodeMap.get(stockSymbol);
        }
        
        return stockPrices.getOrDefault(stockCode, 10.0);
    }

    /**
     * 检测股票价格变动
     * @param stockSymbol 股票代码或名称
     * @param threshold 价格变动阈值（百分比）
     * @return 价格变动信息，包含当前价格、变动百分比、变动方向
     */
    public PriceChangeInfo detectPriceChange(String stockSymbol, double threshold) {
        double currentPrice = getStockPrice(stockSymbol);
        
        // 从内存存储获取前一时刻价格
        double previousPrice = getPreviousStockPrice(stockSymbol);
        if (previousPrice == 0.0) {
            previousPrice = currentPrice;
        }

        double changePercent = (currentPrice - previousPrice) / previousPrice * 100;
        boolean isSignificant = Math.abs(changePercent) >= threshold;

        // 创建价格变动信息
        PriceChangeInfo info = new PriceChangeInfo();
        info.setStockSymbol(stockSymbol);
        info.setCurrentPrice(currentPrice);
        info.setPreviousPrice(previousPrice);
        info.setChangePercent(changePercent);
        info.setSignificantChange(isSignificant);
        info.setTimestamp(LocalDateTime.now());
        
        if (isSignificant) {
            if (changePercent > 0) {
                info.setChangeDirection("急拉");
                info.setRecommendation("考虑适当减仓");
            } else {
                info.setChangeDirection("急跌");
                info.setRecommendation("观察后考虑加仓");
            }
        }

        return info;
    }
    
    /**
     * 获取前一时刻的股票价格
     * @param stockSymbol 股票代码
     * @return 前一时刻的股票价格
     */
    private double getPreviousStockPrice(String stockSymbol) {
        String stockCode = stockSymbol;
        if (stockCodeMap.containsKey(stockSymbol)) {
            stockCode = stockCodeMap.get(stockSymbol);
        }
        
        return previousStockPrices.getOrDefault(stockCode, getDefaultStockPrice(stockSymbol));
    }
}
