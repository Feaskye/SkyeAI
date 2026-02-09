package com.skyeai.jarvis.skills.service;

import com.skyeai.jarvis.skills.model.SearchRequest;
import com.skyeai.jarvis.skills.model.SearchResponse;

/**
 * 搜索服务接口
 * 提供实时搜索功能，集成外部搜索API
 */
public interface SearchService {
    
    /**
     * 执行搜索
     * @param request 搜索请求
     * @return 搜索响应
     */
    SearchResponse search(SearchRequest request);
    
    /**
     * 执行关键词搜索
     * @param query 搜索关键词
     * @param count 结果数量
     * @return 搜索响应
     */
    SearchResponse search(String query, int count);
    
    /**
     * 执行自然语言搜索
     * @param query 自然语言查询
     * @param count 结果数量
     * @param language 语言
     * @return 搜索响应
     */
    SearchResponse search(String query, int count, String language);
    
    /**
     * 清理搜索缓存
     */
    void clearCache();
    
    /**
     * 获取缓存状态
     * @return 缓存状态
     */
    String getCacheStatus();
}