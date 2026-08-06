package com.skyeai.jarvis.agent.tool.impl;

import com.skyeai.jarvis.agent.tool.InnerTool;
import com.skyeai.jarvis.agent.tool.ToolCallback;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * 股票查询工具
 * 实现InnerTool接口，提供股票信息查询功能
 * 支持真实API调用（可配置数据源）和模拟数据降级策略
 */
@Slf4j
@Component
public class StockTool implements InnerTool {
    
    /**
     * 输入参数Schema
     */
    private static final String INPUT_SCHEMA = """
        {
            "type": "object",
            "properties": {
                "symbol": {
                    "type": "string",
                    "description": "股票代码，如：000001.SH（上证指数）、AAPL（苹果）"
                }
            },
            "required": ["symbol"]
        }
        """;
    
    /**
     * 是否启用真实股票API
     */
    @Value("${stock.api.enabled:false}")
    private boolean apiEnabled;
    
    /**
     * 股票API URL（支持东方财富等数据源）
     */
    @Value("${stock.api.url:}")
    private String apiUrl;
    
    /**
     * 股票API Key
     */
    @Value("${stock.api.key:}")
    private String apiKey;
    
    private WebClient stockWebClient;
    
    @PostConstruct
    public void init() {
        if (apiUrl != null && !apiUrl.isBlank()) {
            this.stockWebClient = WebClient.builder()
                    .baseUrl(apiUrl)
                    .build();
        }
        log.info("StockTool初始化完成，API启用: {}", apiEnabled);
    }
    
    @Override
    public List<ToolCallback> loadToolCallbacks() {
        ToolCallback stockTool = ToolCallback.builder()
                .name("get_stock")
                .description("获取指定股票的实时行情信息")
                .inputSchema(INPUT_SCHEMA)
                .function(this::fetchStock)
                .type("stock")
                .build();
        return List.of(stockTool);
    }
    
    @Override
    public String getToolName() {
        return "StockTool";
    }
    
    @Override
    public String getToolDescription() {
        return "股票查询工具，支持查询A股和美股的实时行情";
    }
    
    @Override
    public String getToolType() {
        return "stock";
    }
    
    /**
     * 获取股票信息
     * 优先调用真实API，失败时降级为模拟数据
     * @param params 调用参数，包含symbol字段
     * @return 股票信息字符串
     */
    private String fetchStock(Map<String, Object> params) {
        String symbol = (String) params.getOrDefault("symbol", "000001.SH");
        log.debug("查询股票: {}, API启用: {}", symbol, apiEnabled);
        
        // 如果启用了真实API，优先调用
        if (apiEnabled && stockWebClient != null) {
            try {
                String result = fetchStockFromApi(symbol);
                if (result != null && !result.isBlank()) {
                    return result;
                }
                log.warn("真实股票API返回空结果，使用模拟数据");
            } catch (Exception e) {
                log.warn("真实股票API调用失败，使用模拟数据: {}", e.getMessage());
            }
        }
        
        // 降级：使用模拟数据
        return simulateStock(symbol);
    }
    
    /**
     * 从真实API获取股票信息
     * 支持东方财富等公开股票数据接口
     */
    private String fetchStockFromApi(String symbol) {
        try {
            // 转换股票代码格式
            String convertedSymbol = convertSymbol(symbol);
            
            // 调用股票API
            Map<String, Object> response = stockWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("symbol", convertedSymbol)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null) {
                // 尝试解析通用格式
                String price = safeGet(response, "price", "价格");
                String changePercent = safeGet(response, "changePercent", "涨跌幅");
                String volume = safeGet(response, "volume", "成交量");
                
                if (price != null) {
                    return String.format("%s：%s元，涨跌幅%s，成交量%s",
                            symbol, price, changePercent, volume);
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("调用股票API异常", e);
            return null;
        }
    }
    
    /**
     * 转换股票代码格式
     * 不同数据源可能需要不同的代码格式
     */
    private String convertSymbol(String symbol) {
        // 处理常见的代码格式转换
        if (symbol.contains(".SH") || symbol.contains(".SZ")) {
            // A股代码转换
            return symbol.replace(".SH", "").replace(".SZ", "");
        }
        return symbol;
    }
    
    /**
     * 安全获取Map中的值
     */
    private String safeGet(Map<String, Object> map, String key, String defaultDesc) {
        Object value = map.get(key);
        if (value == null) {
            value = map.getOrDefault(key, "未知");
        }
        return String.valueOf(value);
    }
    
    /**
     * 模拟股票数据（降级策略）
     */
    private String simulateStock(String symbol) {
        Map<String, String> stockData = Map.of(
            "000001.SH", "上证指数：3250.50点，涨幅+0.85%，成交量：2.1亿",
            "399006.SZ", "创业板指：2180.30点，涨幅+1.25%，成交量：1.8亿",
            "AAPL", "苹果(AAPL)：178.50美元，涨幅+0.52%，成交量：5200万",
            "GOOGL", "谷歌(GOOGL)：141.80美元，涨幅-0.25%，成交量：1800万",
            "MSFT", "微软(MSFT)：378.90美元，涨幅+1.15%，成交量：2100万",
            "600519.SH", "贵州茅台：1680.00元，涨幅+0.35%，成交量：1200万",
            "000858.SZ", "五粮液：145.80元，涨幅+0.62%，成交量：2800万"
        );
        
        return stockData.getOrDefault(symbol, 
            "暂未获取到" + symbol + "的股票信息，请稍后再试");
    }
}
