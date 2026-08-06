package com.skyeai.jarvis.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 关键词存储和BM25检索
 * 基于BM25算法的关键词检索实现
 */
@Slf4j
@Component
public class KeywordStore {
    
    /**
     * 文档集合
     */
    private final Map<String, KeywordDocument> documents = new HashMap<>();
    
    /**
     * 文档频率
     */
    private final Map<String, Integer> documentFrequency = new HashMap<>();
    
    /**
     * 平均文档长度
     */
    private double averageDocLength = 0;
    
    /**
     * 文档总数
     */
    private int totalDocs = 0;
    
    /**
     * BM25参数
     */
    private static final double K1 = 1.5;
    private static final double B = 0.75;
    
    /**
     * 添加文档
     */
    public void addDocument(String id, String content, Map<String, Object> metadata) {
        KeywordDocument doc = new KeywordDocument(id, content, metadata);
        documents.put(id, doc);
        
        // 更新文档频率
        List<String> terms = extractTerms(content);
        for (String term : terms) {
            documentFrequency.merge(term, 1, Integer::sum);
        }
        
        // 更新平均文档长度
        totalDocs++;
        double totalLength = documents.values().stream()
                .mapToLong(d -> d.getTerms().size())
                .sum();
        averageDocLength = totalLength / totalDocs;
        
        log.debug("添加关键词文档: {}, 词数: {}", id, terms.size());
    }
    
    /**
     * 搜索文档
     */
    public List<KeywordSearchResult> search(String query, int limit) {
        List<String> queryTerms = extractTerms(query);
        Map<String, Double> scores = new HashMap<>();
        
        // 计算每个文档的BM25分数
        for (KeywordDocument doc : documents.values()) {
            double score = calculateBM25(doc, queryTerms);
            if (score > 0) {
                scores.put(doc.getId(), score);
            }
        }
        
        // 按分数排序
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    KeywordDocument doc = documents.get(entry.getKey());
                    return new KeywordSearchResult(doc.getId(), entry.getValue(), doc.getContent(), doc.getMetadata());
                })
                .toList();
    }
    
    /**
     * 计算BM25分数
     */
    private double calculateBM25(KeywordDocument doc, List<String> queryTerms) {
        double score = 0;
        int docLength = doc.getTerms().size();
        
        for (String term : queryTerms) {
            int tf = doc.getTermFrequency(term);
            if (tf == 0) continue;
            
            int df = documentFrequency.getOrDefault(term, 0);
            if (df == 0) df = 1;
            
            // IDF计算
            double idf = Math.log((totalDocs - df + 0.5) / (df + 0.5) + 1);
            
            // BM25公式
            double termScore = idf * (tf * (K1 + 1)) / (tf + K1 * (1 - B + B * docLength / averageDocLength));
            
            score += termScore;
        }
        
        return score;
    }
    
    /**
     * 提取词项
     */
    private List<String> extractTerms(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        
        // 简单的中文分词（实际应用中应使用专业分词库）
        return Arrays.stream(text.split("[\\s,，。、！？；：\"\''（）「」『』【】《》]+"))
                .filter(term -> term.length() >= 2)
                .toList();
    }
    
    /**
     * 获取文档数量
     */
    public int getDocumentCount() {
        return documents.size();
    }
    
    /**
     * 关键词文档
     */
    @lombok.Data
    public static class KeywordDocument {
        private String id;
        private String content;
        private Map<String, Object> metadata;
        private Map<String, Integer> termFrequency;
        
        public KeywordDocument(String id, String content, Map<String, Object> metadata) {
            this.id = id;
            this.content = content;
            this.metadata = metadata != null ? metadata : new HashMap<>();
            this.termFrequency = new HashMap<>();
            
            // 计算词频
            List<String> terms = Arrays.stream(content.split("[\\s,，。、！？；：\"\''（）「」『』【】《》]+"))
                    .filter(t -> t.length() >= 2)
                    .toList();
            
            for (String term : terms) {
                termFrequency.merge(term, 1, Integer::sum);
            }
        }
        
        public int getTermFrequency(String term) {
            return termFrequency.getOrDefault(term, 0);
        }
        
        public List<String> getTerms() {
            return new ArrayList<>(termFrequency.keySet());
        }
    }
    
    /**
     * 关键词搜索结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class KeywordSearchResult {
        private String id;
        private double score;
        private String content;
        private Map<String, Object> metadata;
    }
}