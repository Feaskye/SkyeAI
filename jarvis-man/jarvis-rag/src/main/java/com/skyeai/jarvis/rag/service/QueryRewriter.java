package com.skyeai.jarvis.rag.service;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 查询改写器
 * 对原始查询进行扩展和改写，生成多个相关查询
 */
@Component
public class QueryRewriter {
    
    /**
     * 同义词映射
     */
    private static final Map<String, List<String>> SYNONYMS = new HashMap<>();
    
    static {
        SYNONYMS.put("北京", Arrays.asList("帝都", "京城", "首都"));
        SYNONYMS.put("股票", Arrays.asList("股市", "证券", "股份"));
        SYNONYMS.put("天气", Arrays.asList("气候", "气象", "气温"));
        SYNONYMS.put("中国", Arrays.asList("中华", "华夏", "China"));
        SYNONYMS.put("美国", Arrays.asList("美国", "USA", "美利坚"));
    }
    
    /**
     * 改写查询
     * @param originalQuery 原始查询
     * @return 改写后的查询列表
     */
    public List<String> rewrite(String originalQuery) {
        List<String> queries = new ArrayList<>();
        queries.add(originalQuery);
        
        // 1. 添加原始查询
        queries.add(originalQuery);
        
        // 2. 提取关键词并扩展
        List<String> keywords = extractKeywords(originalQuery);
        for (String keyword : keywords) {
            // 添加同义词扩展
            List<String> synonyms = SYNONYMS.get(keyword);
            if (synonyms != null) {
                for (String synonym : synonyms) {
                    String expandedQuery = originalQuery.replace(keyword, synonym);
                    if (!expandedQuery.equals(originalQuery)) {
                        queries.add(expandedQuery);
                    }
                }
            }
        }
        
        // 3. 添加泛化查询
        String generalizedQuery = generalizeQuery(originalQuery);
        if (!generalizedQuery.equals(originalQuery)) {
            queries.add(generalizedQuery);
        }
        
        // 4. 添加具体化查询
        String specifiedQuery = specifyQuery(originalQuery);
        if (!specifiedQuery.equals(originalQuery)) {
            queries.add(specifiedQuery);
        }
        
        // 去重
        return new ArrayList<>(new LinkedHashSet<>(queries));
    }
    
    /**
     * 提取关键词
     */
    private List<String> extractKeywords(String query) {
        List<String> keywords = new ArrayList<>();
        
        // 简单的关键词提取（实际应用中应使用NLP）
        String[] words = query.split("[\\s,，。、]+");
        for (String word : words) {
            if (word.length() >= 2) {
                keywords.add(word);
            }
        }
        
        return keywords;
    }
    
    /**
     * 泛化查询
     */
    private String generalizeQuery(String query) {
        // 简单的泛化：将具体词汇替换为通用词汇
        String generalized = query
            .replaceAll("\\d+岁", "年龄")
            .replaceAll("\\d{4}年", "某年")
            .replaceAll("\\d+元", "金额")
            .replaceAll("第\\d+", "某");
        
        return generalized;
    }
    
    /**
     * 具体化查询
     */
    private String specifyQuery(String query) {
        // 简单的时间具体化
        String specified = query
            .replace("现在", "最近几天")
            .replace("最近", "最近一周内")
            .replace("当前", "此时此刻");
        
        return specified;
    }
    
    /**
     * 获取查询建议
     */
    public List<String> getQuerySuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        
        // 添加完整匹配建议
        suggestions.add(query);
        
        // 添加前缀匹配建议
        if (query.length() > 2) {
            suggestions.add(query.substring(0, query.length() - 1) + "*");
        }
        
        return suggestions;
    }
}