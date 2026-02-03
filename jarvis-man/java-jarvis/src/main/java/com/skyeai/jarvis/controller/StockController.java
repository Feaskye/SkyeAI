package com.skyeai.jarvis.controller;

import com.skyeai.jarvis.model.PriceChangeInfo;
import com.skyeai.jarvis.model.ScheduleEvent;
import com.skyeai.jarvis.service.ScheduleService;
import com.skyeai.jarvis.service.ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 股票监测控制器
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ServiceClient serviceClient;

    /**
     * 创建股票监测提醒
     * @param title 提醒标题
     * @param description 提醒描述
     * @param stockSymbols 股票代码列表，逗号分隔
     * @param priceChangeThreshold 价格变动阈值（百分比）
     * @return 创建的日程事件
     */
    @PostMapping("/create-alert")
    public ScheduleEvent createStockAlert(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String stockSymbols,
            @RequestParam(defaultValue = "3.0") double priceChangeThreshold) {

        // 创建每日重复的股票监测事件
        return scheduleService.createRepeatEvent(
                title,
                description,
                "daily",
                stockSymbols,
                priceChangeThreshold
        );
    }

    /**
     * 获取股票当前价格
     * @param stockSymbol 股票代码或名称
     * @return 股票当前价格
     */
    @GetMapping("/price")
    public double getStockPrice(@RequestParam String stockSymbol) {
        Map<String, Object> request = Map.of("stockSymbol", stockSymbol);
        Map<String, Object> response = serviceClient.callFinanceService("/stock/price", request);
        return ((Number) response.get("price")).doubleValue();
    }

    /**
     * 检测股票价格变动
     * @param stockSymbol 股票代码或名称
     * @param threshold 价格变动阈值（百分比）
     * @return 价格变动信息
     */
    @GetMapping("/detect-change")
    public PriceChangeInfo detectPriceChange(
            @RequestParam String stockSymbol,
            @RequestParam(defaultValue = "3.0") double threshold) {
        Map<String, Object> request = Map.of(
            "stockSymbol", stockSymbol,
            "threshold", threshold
        );
        Map<String, Object> response = serviceClient.callFinanceService("/stock/detect-change", request);
        
        // 转换响应为PriceChangeInfo
        PriceChangeInfo changeInfo = new PriceChangeInfo();
        changeInfo.setStockSymbol((String) response.get("stockSymbol"));
        changeInfo.setCurrentPrice(((Number) response.get("currentPrice")).doubleValue());
        changeInfo.setPreviousPrice(((Number) response.get("previousPrice")).doubleValue());
        changeInfo.setChangePercent(((Number) response.get("changePercent")).doubleValue());
        changeInfo.setChangeDirection((String) response.get("changeDirection"));
        changeInfo.setSignificantChange((Boolean) response.get("significantChange"));
        changeInfo.setRecommendation((String) response.get("recommendation"));
        
        return changeInfo;
    }
}
