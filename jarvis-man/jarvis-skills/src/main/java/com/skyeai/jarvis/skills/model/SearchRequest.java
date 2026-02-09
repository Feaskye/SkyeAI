package com.skyeai.jarvis.skills.model;

/**
 * 搜索请求模型
 */
public class SearchRequest {
    private String query;           // 搜索查询
    private int count;              // 结果数量
    private String language;        // 语言
    private String region;          // 地区
    private String freshness;       // 新鲜度（day, week, month）
    private String responseFilter;  // 响应过滤器
    
    // Getters and Setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getFreshness() {
        return freshness;
    }
    
    public void setFreshness(String freshness) {
        this.freshness = freshness;
    }
    
    public String getResponseFilter() {
        return responseFilter;
    }
    
    public void setResponseFilter(String responseFilter) {
        this.responseFilter = responseFilter;
    }
}