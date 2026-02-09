package com.skyeai.jarvis.skills.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyeai.jarvis.skills.model.SearchRequest;
import com.skyeai.jarvis.skills.model.SearchResponse;
import com.skyeai.jarvis.skills.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bing搜索服务实现
 * 集成Bing Search API，提供实时搜索功能
 */
@Slf4j
@Service
public class BingSearchServiceImpl implements SearchService {
    
    @Value("${search.bing.api-key}")
    private String bingApiKey;
    
    @Value("${search.bing.endpoint:https://api.bing.microsoft.com/v7.0/search}")
    private String bingEndpoint;
    
    @Value("${search.bing.default-count:5}")
    private int defaultCount;
    
    @Value("${search.bing.max-count:10}")
    private int maxCount;
    
    @Value("${search.cache.enabled:true}")
    private boolean cacheEnabled;
    
    @Value("${search.cache.expiration:3600}")
    private long cacheExpiration;
    
    @Value("${search.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;
    
    @Value("${search.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, CachedSearchResponse> searchCache;
    private final Map<String, Integer> rateLimitCounter;
    private long lastRateLimitReset;
    
    public BingSearchServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.searchCache = new ConcurrentHashMap<>();
        this.rateLimitCounter = new ConcurrentHashMap<>();
        this.lastRateLimitReset = System.currentTimeMillis();
    }
    
    /**
     * 执行搜索
     */
    @Override
    public SearchResponse search(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 检查速率限制
            if (rateLimitEnabled && !checkRateLimit()) {
                SearchResponse response = new SearchResponse();
                response.setError("Rate limit exceeded");
                response.setExecutionTime(System.currentTimeMillis() - startTime);
                return response;
            }
            
            // 生成缓存键
            String cacheKey = generateCacheKey(request);
            
            // 检查缓存
            if (cacheEnabled) {
                CachedSearchResponse cachedResponse = searchCache.get(cacheKey);
                if (cachedResponse != null && !isCacheExpired(cachedResponse)) {
                    log.info("Using cached search result for query: {}", request.getQuery());
                    SearchResponse response = cachedResponse.getResponse();
                    response.setExecutionTime(System.currentTimeMillis() - startTime);
                    return response;
                }
            }
            
            // 构建请求URL
            String url = buildSearchUrl(request);
            
            // 发送请求
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Ocp-Apim-Subscription-Key", bingApiKey)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            // 处理响应
            SearchResponse response = processResponse(httpResponse.body(), request);
            response.setExecutionTime(System.currentTimeMillis() - startTime);
            
            // 缓存结果
            if (cacheEnabled && response.getError() == null) {
                searchCache.put(cacheKey, new CachedSearchResponse(response, System.currentTimeMillis()));
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error executing search: {}", e.getMessage(), e);
            SearchResponse response = new SearchResponse();
            response.setError("Search failed: " + e.getMessage());
            response.setExecutionTime(System.currentTimeMillis() - startTime);
            return response;
        }
    }
    
    /**
     * 执行关键词搜索
     */
    @Override
    public SearchResponse search(String query, int count) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setCount(Math.min(count, maxCount));
        request.setLanguage("zh-CN");
        return search(request);
    }
    
    /**
     * 执行自然语言搜索
     */
    @Override
    public SearchResponse search(String query, int count, String language) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setCount(Math.min(count, maxCount));
        request.setLanguage(language);
        return search(request);
    }
    
    /**
     * 清理搜索缓存
     */
    @Override
    public void clearCache() {
        searchCache.clear();
        log.info("Search cache cleared");
    }
    
    /**
     * 获取缓存状态
     */
    @Override
    public String getCacheStatus() {
        return String.format("Cache size: %d, Enabled: %b, Expiration: %d seconds", 
                searchCache.size(), cacheEnabled, cacheExpiration);
    }
    
    /**
     * 检查速率限制
     */
    private boolean checkRateLimit() {
        long now = System.currentTimeMillis();
        
        // 每分钟重置计数器
        if (now - lastRateLimitReset > 60000) {
            rateLimitCounter.clear();
            lastRateLimitReset = now;
        }
        
        String key = "global";
        int count = rateLimitCounter.computeIfAbsent(key, k -> 0);
        
        if (count >= requestsPerMinute) {
            return false;
        }
        
        rateLimitCounter.put(key, count + 1);
        return true;
    }
    
    /**
     * 生成缓存键
     */
    private String generateCacheKey(SearchRequest request) {
        StringBuilder key = new StringBuilder();
        key.append(request.getQuery());
        key.append("_");
        key.append(request.getCount());
        key.append("_");
        key.append(request.getLanguage() != null ? request.getLanguage() : "default");
        key.append("_");
        key.append(request.getFreshness() != null ? request.getFreshness() : "any");
        return key.toString();
    }
    
    /**
     * 检查缓存是否过期
     */
    private boolean isCacheExpired(CachedSearchResponse cachedResponse) {
        return System.currentTimeMillis() - cachedResponse.getTimestamp() > cacheExpiration * 1000;
    }
    
    /**
     * 构建搜索URL
     */
    private String buildSearchUrl(SearchRequest request) {
        StringBuilder url = new StringBuilder(bingEndpoint);
        url.append("?q=");
        url.append(URLEncoder.encode(request.getQuery(), StandardCharsets.UTF_8));
        
        int count = request.getCount() > 0 ? request.getCount() : defaultCount;
        url.append("&count=").append(Math.min(count, maxCount));
        
        if (request.getLanguage() != null) {
            url.append("&mkt=").append(request.getLanguage());
        }
        
        if (request.getFreshness() != null) {
            url.append("&freshness=").append(request.getFreshness());
        }
        
        if (request.getResponseFilter() != null) {
            url.append("&responseFilter=").append(request.getResponseFilter());
        }
        
        return url.toString();
    }
    
    /**
     * 处理响应
     */
    private SearchResponse processResponse(String responseBody, SearchRequest request) throws IOException {
        SearchResponse response = new SearchResponse();
        response.setQuery(request.getQuery());
        
        try {
            // 解析Bing API响应
            Map<String, Object> bingResponse = objectMapper.readValue(responseBody, Map.class);
            
            // 提取搜索结果
            List<SearchResponse.SearchResult> results = new ArrayList<>();
            
            if (bingResponse.containsKey("webPages")) {
                Map<String, Object> webPages = (Map<String, Object>) bingResponse.get("webPages");
                if (webPages.containsKey("totalEstimatedMatches")) {
                    response.setTotalCount(Integer.parseInt(webPages.get("totalEstimatedMatches").toString()));
                }
                
                if (webPages.containsKey("value")) {
                    List<Map<String, Object>> valueList = (List<Map<String, Object>>) webPages.get("value");
                    for (Map<String, Object> item : valueList) {
                        SearchResponse.SearchResult result = new SearchResponse.SearchResult();
                        if (item.containsKey("name")) {
                            result.setTitle((String) item.get("name"));
                        }
                        if (item.containsKey("url")) {
                            result.setUrl((String) item.get("url"));
                        }
                        if (item.containsKey("snippet")) {
                            result.setSnippet((String) item.get("snippet"));
                        }
                        if (item.containsKey("dateLastCrawled")) {
                            result.setDate((String) item.get("dateLastCrawled"));
                        }
                        // 简单的相关性得分计算
                        result.setScore(1.0 - (results.size() * 0.1));
                        results.add(result);
                    }
                }
            }
            
            response.setResults(results);
        } catch (Exception e) {
            log.error("Error processing search response: {}", e.getMessage(), e);
            response.setError("Failed to process search response");
        }
        
        return response;
    }
    
    /**
     * 缓存的搜索响应
     */
    private static class CachedSearchResponse {
        private final SearchResponse response;
        private final long timestamp;
        
        public CachedSearchResponse(SearchResponse response, long timestamp) {
            this.response = response;
            this.timestamp = timestamp;
        }
        
        public SearchResponse getResponse() {
            return response;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}