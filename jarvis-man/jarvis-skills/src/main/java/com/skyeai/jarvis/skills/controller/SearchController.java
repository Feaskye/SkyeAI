package com.skyeai.jarvis.skills.controller;

import com.skyeai.jarvis.skills.model.SearchRequest;
import com.skyeai.jarvis.skills.model.SearchResponse;
import com.skyeai.jarvis.skills.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索控制器
 * 提供搜索相关的HTTP接口
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
public class SearchController {
    
    @Autowired
    private SearchService searchService;
    
    // 搜索历史（暂时存储在内存中）
    private final List<SearchHistory> searchHistory = new ArrayList<>();
    private int historyIdCounter = 1;
    
    /**
     * 执行搜索
     * @param request 搜索请求
     * @return 搜索响应
     */
    @PostMapping
    public Map<String, Object> search(@RequestBody SearchRequest request) {
        log.info("Received search request: {}", request.getQuery());
        SearchResponse response = searchService.search(request);
        
        // 保存搜索历史
        saveSearchHistory(request, response);
        
        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("results", response.getResults());
        result.put("totalCount", response.getTotalCount());
        result.put("query", response.getQuery());
        result.put("executionTime", response.getExecutionTime());
        
        if (response.getError() != null) {
            result.put("error", response.getError());
        }
        
        return result;
    }
    
    /**
     * 执行简单搜索
     * @param query 搜索关键词
     * @param count 结果数量
     * @return 搜索响应
     */
    @GetMapping
    public Map<String, Object> search(@RequestParam String query, 
                                 @RequestParam(required = false, defaultValue = "5") int count) {
        log.info("Received simple search request: {}, count: {}", query, count);
        SearchResponse response = searchService.search(query, count);
        
        // 保存搜索历史
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setCount(count);
        saveSearchHistory(request, response);
        
        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("results", response.getResults());
        result.put("totalCount", response.getTotalCount());
        result.put("query", response.getQuery());
        result.put("executionTime", response.getExecutionTime());
        
        if (response.getError() != null) {
            result.put("error", response.getError());
        }
        
        return result;
    }
    
    /**
     * 获取搜索历史
     * @return 搜索历史列表
     */
    @GetMapping("/history")
    public Map<String, Object> getSearchHistory() {
        List<Map<String, Object>> historyList = new ArrayList<>();
        for (SearchHistory history : searchHistory) {
            Map<String, Object> historyItem = new HashMap<>();
            historyItem.put("id", history.getId());
            historyItem.put("query", history.getQuery());
            historyItem.put("timestamp", history.getTimestamp());
            historyItem.put("resultsCount", history.getResultsCount());
            historyList.add(historyItem);
        }
        return Map.of("history", historyList);
    }
    
    /**
     * 获取搜索历史详情
     * @param id 历史ID
     * @return 历史详情
     */
    @GetMapping("/history/{id}")
    public Map<String, Object> getSearchHistoryDetail(@PathVariable int id) {
        for (SearchHistory history : searchHistory) {
            if (history.getId() == id) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", history.getId());
                result.put("query", history.getQuery());
                result.put("timestamp", history.getTimestamp());
                result.put("results", history.getResults());
                return result;
            }
        }
        return Map.of("error", "History not found");
    }
    
    /**
     * 清理搜索缓存
     * @return 清理结果
     */
    @DeleteMapping("/cache")
    public Map<String, String> clearCache() {
        searchService.clearCache();
        return Map.of("status", "success", "message", "Cache cleared");
    }
    
    /**
     * 获取缓存状态
     * @return 缓存状态
     */
    @GetMapping("/cache/status")
    public Map<String, String> getCacheStatus() {
        String status = searchService.getCacheStatus();
        return Map.of("status", "success", "message", status);
    }
    
    /**
     * 保存搜索历史
     */
    private void saveSearchHistory(SearchRequest request, SearchResponse response) {
        SearchHistory history = new SearchHistory();
        history.setId(historyIdCounter++);
        history.setQuery(request.getQuery());
        history.setTimestamp(System.currentTimeMillis());
        history.setResultsCount(response.getResults() != null ? response.getResults().size() : 0);
        history.setResults(response.getResults());
        searchHistory.add(history);
        
        // 限制历史记录数量
        if (searchHistory.size() > 100) {
            searchHistory.remove(0);
        }
    }
    
    /**
     * 搜索历史记录
     */
    private static class SearchHistory {
        private int id;
        private String query;
        private long timestamp;
        private int resultsCount;
        private List<SearchResponse.SearchResult> results;
        
        // Getters and Setters
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getQuery() {
            return query;
        }
        
        public void setQuery(String query) {
            this.query = query;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
        
        public int getResultsCount() {
            return resultsCount;
        }
        
        public void setResultsCount(int resultsCount) {
            this.resultsCount = resultsCount;
        }
        
        public List<SearchResponse.SearchResult> getResults() {
            return results;
        }
        
        public void setResults(List<SearchResponse.SearchResult> results) {
            this.results = results;
        }
    }
}