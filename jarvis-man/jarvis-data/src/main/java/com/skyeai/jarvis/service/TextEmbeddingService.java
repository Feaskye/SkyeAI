package com.skyeai.jarvis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本向量化服务，用于将文本转换为向量表示
 * 基于 Spring AI EmbeddingModel 实现
 */
@Service
public class TextEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(TextEmbeddingService.class);

    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 将文本转换为向量
     */
    public List<Double> embedText(String text) {
        try {
            // 委托 Spring AI EmbeddingModel 生成向量
            float[] vector = embeddingModel.embed(text);
            List<Double> result = new ArrayList<>(vector.length);
            for (float v : vector) {
                result.add((double) v);
            }
            log.info("文本向量化成功，向量维度：{}", vector.length);
            return result;
        } catch (Exception e) {
            log.error("文本向量化失败：{}", e.getMessage(), e);
            throw new RuntimeException("文本向量化失败", e);
        }
    }

    /**
     * 批量将文本转换为向量
     */
    public List<List<Double>> embedTexts(List<String> texts) {
        List<List<Double>> vectors = new ArrayList<>(texts.size());
        for (String text : texts) {
            vectors.add(embedText(text));
        }
        return vectors;
    }

    /**
     * 计算两个向量的余弦相似度
     */
    public double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vectors must have the same size");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += Math.pow(vector1.get(i), 2);
            norm2 += Math.pow(vector2.get(i), 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
