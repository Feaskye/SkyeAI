package com.skyeai.jarvis.finance.controller;

import com.skyeai.jarvis.finance.model.PriceChangeInfo;
import com.skyeai.jarvis.finance.service.StockMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 股票控制器，用于处理股票相关的HTTP请求
 */
@RestController
@RequestMapping("/api/finance")
public class StockController {

    @Autowired
    private StockMonitorService stockMonitorService;

    /**
     * 获取股票当前价格
     * @param stockSymbol 股票代码或名称
     * @return 股票价格信息
     */
    @PostMapping("/stock/price")
    public ResponseEntity<Map<String, Object>> getStockPrice(@RequestBody Map<String, Object> request) {
        try {
            String stockSymbol = (String) request.get("stockSymbol");
            double price = stockMonitorService.getStockPrice(stockSymbol);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "stockSymbol", stockSymbol,
                    "price", price,
                    "message", "获取股票价格成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取股票价格失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检测股票价格变动
     * @param request 请求参数，包含股票代码和阈值
     * @return 价格变动信息
     */
    @PostMapping("/stock/detect-change")
    public ResponseEntity<Map<String, Object>> detectPriceChange(@RequestBody Map<String, Object> request) {
        try {
            String stockSymbol = (String) request.get("stockSymbol");
            double threshold = ((Number) request.get("threshold")).doubleValue();
            PriceChangeInfo changeInfo = stockMonitorService.detectPriceChange(stockSymbol, threshold);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "stockSymbol", changeInfo.getStockSymbol(),
                    "currentPrice", changeInfo.getCurrentPrice(),
                    "previousPrice", changeInfo.getPreviousPrice(),
                    "changePercent", changeInfo.getChangePercent(),
                    "changeDirection", changeInfo.getChangeDirection(),
                    "significantChange", changeInfo.isSignificantChange(),
                    "recommendation", changeInfo.getRecommendation(),
                    "message", "检测股票价格变动成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "检测股票价格变动失败: " + e.getMessage()
            ));
        }
    }
}
