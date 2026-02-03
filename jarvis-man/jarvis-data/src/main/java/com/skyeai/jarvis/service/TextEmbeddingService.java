package com.skyeai.jarvis.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 文本向量化服务，用于将文本转换为向量表示
 * 注意：这里使用随机向量作为示例，实际应用中应使用真实的嵌入模型
 */
@Service
public class TextEmbeddingService {

    private static final int VECTOR_SIZE = 768;
    private final Random random = new Random();

    /**
     * 将文本转换为向量
     */
    public List<Double> embedText(String text) {
        // 实际应用中，这里应该使用真实的嵌入模型，如BERT、Sentence Transformers等
        // 这里使用随机向量作为示例
        List<Double> vector = new ArrayList<>(VECTOR_SIZE);
        for (int i = 0; i < VECTOR_SIZE; i++) {
            // 生成-1到1之间的随机数
            vector.add(random.nextDouble() * 2 - 1);
        }
        return vector;
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
