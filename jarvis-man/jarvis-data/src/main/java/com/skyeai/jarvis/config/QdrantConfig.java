package com.skyeai.jarvis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Qdrant向量数据库配置类
 * 注意：当前使用内存存储作为Qdrant向量数据库的占位符实现
 */
@Configuration
public class QdrantConfig {

    @Bean
    public VectorService vectorService() {
        return new VectorService();
    }
}
