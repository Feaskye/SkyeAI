package com.skyeai.jarvis.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 重排器
 * 使用交叉编码器对检索结果进行重排，提升相关性
 */
@Slf4j
@Component
public class Reranker {
    
    /**
     * 重排文档
     * @param query 查询文本
     * @param documents 待重排的文档列表
     * @return 重排后的文档列表
     */
    public List<RerankedDocument> rerank(String query, List<RerankedDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }
        
        if (documents.size() == 1) {
            return documents;
        }
        
        // 计算每个文档与查询的相关性分数
        List<Map.Entry<RerankedDocument, Double>> scores = new ArrayList<>();
        
        for (RerankedDocument doc : documents) {
            double relevanceScore = calculateRelevance(query, doc);
            scores.add(new AbstractMap.SimpleEntry<>(doc, relevanceScore));
        }
        
        // 按相关性分数排序
        scores.sort(Map.Entry.<RerankedDocument, Double>comparingByValue().reversed());
        
        // 构建结果，设置新排名
        List<RerankedDocument> result = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            RerankedDocument doc = scores.get(i).getKey();
            doc.setNewRank(i + 1);
            doc.setRelevanceScore(scores.get(i).getValue());
            result.add(doc);
        }
        
        log.debug("重排完成，文档数: {}", documents.size());
        return result;
    }
    
    /**
     * 计算相关性分数（简化版）
     * 实际应用中应使用交叉编码器模型
     */
    private double calculateRelevance(String query, RerankedDocument doc) {
        double score = 0;
        
        // 原始分数权重
        score += doc.getOriginalScore() * 0.3;
        
        // 词项匹配权重
        Set<String> queryTerms = new HashSet<>(Arrays.asList(query.toLowerCase().split("\\s+")));
        String content = doc.getContent().toLowerCase();
        
        int matchCount = 0;
        for (String term : queryTerms) {
            if (content.contains(term)) {
                matchCount++;
            }
        }
        
        if (!queryTerms.isEmpty()) {
            score += (double) matchCount / queryTerms.size() * 0.5;
        }
        
        // 位置权重（靠前的文档略微加分）
        double positionBonus = 1.0 / (doc.getOriginalRank() + 1) * 0.1;
        score += positionBonus;
        
        // 长度惩罚（太短或太长的文档略微减分）
        double lengthPenalty = calculateLengthPenalty(doc.getContent().length());
        score += lengthPenalty * 0.1;
        
        return score;
    }
    
    /**
     * 计算长度惩罚
     */
    private double calculateLengthPenalty(int length) {
        // 理想长度在100-500字之间
        if (length < 100) {
            return length / 100.0;
        } else if (length <= 500) {
            return 1.0;
        } else {
            return Math.max(0.5, 1.0 - (length - 500) / 1000.0);
        }
    }
    
    /**
     * 重排文档
     */
    @lombok.Data
    public static class RerankedDocument {
        private String id;
        private String content;
        private double originalScore;
        private int originalRank;
        private double relevanceScore;
        private int newRank;
        private Map<String, Object> metadata;
        
        public RerankedDocument() {
        }
        
        public RerankedDocument(String id, String content, double originalScore, int originalRank) {
            this.id = id;
            this.content = content;
            this.originalScore = originalScore;
            this.originalRank = originalRank;
        }
    }
}