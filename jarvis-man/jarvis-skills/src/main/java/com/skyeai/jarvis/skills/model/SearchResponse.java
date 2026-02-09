package com.skyeai.jarvis.skills.model;

import java.util.List;

/**
 * 搜索响应模型
 */
public class SearchResponse {
    private List<SearchResult> results;  // 搜索结果列表
    private int totalCount;              // 总结果数
    private String query;                // 搜索查询
    private long executionTime;          // 执行时间（毫秒）
    private String error;                // 错误信息
    
    // Getters and Setters
    public List<SearchResult> getResults() {
        return results;
    }
    
    public void setResults(List<SearchResult> results) {
        this.results = results;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    /**
     * 搜索结果项
     */
    public static class SearchResult {
        private String title;        // 标题
        private String url;          // URL
        private String snippet;      // 摘要
        private String date;         // 日期
        private double score;        // 相关性得分
        
        // Getters and Setters
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getSnippet() {
            return snippet;
        }
        
        public void setSnippet(String snippet) {
            this.snippet = snippet;
        }
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public double getScore() {
            return score;
        }
        
        public void setScore(double score) {
            this.score = score;
        }
    }
}