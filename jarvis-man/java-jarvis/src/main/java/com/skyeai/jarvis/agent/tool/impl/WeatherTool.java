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
 * 天气查询工具
 * 实现InnerTool接口，提供天气查询功能
 * 支持真实API调用（和风天气）和模拟数据降级策略
 */
@Slf4j
@Component
public class WeatherTool implements InnerTool {
    
    /**
     * 输入参数Schema
     */
    private static final String INPUT_SCHEMA = """
        {
            "type": "object",
            "properties": {
                "city": {
                    "type": "string",
                    "description": "要查询天气的城市名称，如：北京、上海、广州"
                }
            },
            "required": ["city"]
        }
        """;
    
    /**
     * 是否启用真实天气API
     */
    @Value("${weather.api.enabled:false}")
    private boolean apiEnabled;
    
    /**
     * 天气API URL（和风天气）
     */
    @Value("${weather.api.url:https://devapi.qweather.com/v7/weather/now}")
    private String apiUrl;
    
    /**
     * 天气API Key
     */
    @Value("${weather.api.key:}")
    private String apiKey;
    
    /**
     * 城市ID映射API（和风天气需要城市ID）
     */
    @Value("${weather.api.location-url:https://geoapi.qweather.com/v2/city/lookup}")
    private String locationApiUrl;
    
    private WebClient weatherWebClient;
    private WebClient locationWebClient;
    
    @PostConstruct
    public void init() {
        this.weatherWebClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();
        this.locationWebClient = WebClient.builder()
                .baseUrl(locationApiUrl)
                .build();
        log.info("WeatherTool初始化完成，API启用: {}", apiEnabled);
    }
    
    @Override
    public List<ToolCallback> loadToolCallbacks() {
        ToolCallback weatherTool = ToolCallback.builder()
                .name("get_weather")
                .description("获取指定城市的当前天气信息")
                .inputSchema(INPUT_SCHEMA)
                .function(this::fetchWeather)
                .type("weather")
                .build();
        return List.of(weatherTool);
    }
    
    @Override
    public String getToolName() {
        return "WeatherTool";
    }
    
    @Override
    public String getToolDescription() {
        return "天气查询工具，支持查询全国主要城市的天气信息";
    }
    
    @Override
    public String getToolType() {
        return "weather";
    }
    
    /**
     * 获取天气信息
     * 优先调用真实API，失败时降级为模拟数据
     * @param params 调用参数，包含city字段
     * @return 天气信息字符串
     */
    private String fetchWeather(Map<String, Object> params) {
        String city = (String) params.getOrDefault("city", "北京");
        log.debug("查询天气: {}, API启用: {}", city, apiEnabled);
        
        // 如果启用了真实API，优先调用
        if (apiEnabled && apiKey != null && !apiKey.isBlank()) {
            try {
                String result = fetchWeatherFromApi(city);
                if (result != null && !result.isBlank()) {
                    return result;
                }
                log.warn("真实天气API返回空结果，使用模拟数据");
            } catch (Exception e) {
                log.warn("真实天气API调用失败，使用模拟数据: {}", e.getMessage());
            }
        }
        
        // 降级：使用模拟数据
        return simulateWeather(city);
    }
    
    /**
     * 从真实API获取天气信息（和风天气）
     */
    private String fetchWeatherFromApi(String city) {
        try {
            // 第一步：获取城市ID
            String locationId = getLocationId(city);
            if (locationId == null) {
                log.warn("无法获取城市ID: {}", city);
                return null;
            }
            
            // 第二步：查询天气
            Map<String, Object> response = weatherWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("location", locationId)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("code")) {
                String code = String.valueOf(response.get("code"));
                if ("200".equals(code)) {
                    // 解析天气数据
                    Map<String, Object> now = (Map<String, Object>) response.get("now");
                    if (now != null) {
                        String temp = String.valueOf(now.getOrDefault("temp", "未知"));
                        String text = String.valueOf(now.getOrDefault("text", "未知"));
                        String windDir = String.valueOf(now.getOrDefault("windDir", "未知"));
                        String windScale = String.valueOf(now.getOrDefault("windScale", "未知"));
                        
                        return String.format("%s今日天气：%s，气温%s°C，风力%s级，风向%s",
                                city, text, temp, windScale, windDir);
                    }
                } else {
                    log.warn("天气API返回错误代码: {}", code);
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("调用天气API异常", e);
            return null;
        }
    }
    
    /**
     * 获取城市ID（和风天气需要城市ID作为location参数）
     */
    private String getLocationId(String city) {
        try {
            Map<String, Object> response = locationWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("location", city)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("code")) {
                String code = String.valueOf(response.get("code"));
                if ("200".equals(code)) {
                    List<Map<String, Object>> locations = (List<Map<String, Object>>) response.get("location");
                    if (locations != null && !locations.isEmpty()) {
                        return String.valueOf(locations.get(0).get("id"));
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("获取城市ID异常", e);
            return null;
        }
    }
    
    /**
     * 模拟天气数据（降级策略）
     */
    private String simulateWeather(String city) {
        Map<String, String> weatherData = Map.of(
            "北京", "北京今日天气：晴转多云，气温15°C-25°C，风力3级",
            "上海", "上海今日天气：多云，气温18°C-28°C，风力2级",
            "广州", "广州今日天气：阵雨，气温23°C-32°C，风力4级",
            "深圳", "深圳今日天气：晴，气温25°C-33°C，风力2级",
            "杭州", "杭州今日天气：阴转小雨，气温16°C-24°C，风力3级",
            "成都", "成都今日天气：多云，气温14°C-23°C，风力2级",
            "西安", "西安今日天气：晴，气温12°C-26°C，风力3级"
        );
        
        return weatherData.getOrDefault(city, 
            "暂未获取到" + city + "的天气信息，请稍后再试");
    }
}
