package com.skyeai.jarvis.config;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * VectorStore 多 Bean 装配
 * v10 新增：支持 Milvus + Qdrant 双库共存，按用途路由
 *
 * Spring AI 的 MilvusVectorStore / QdrantVectorStore 由各自 starter 自动装配：
 * - Milvus 自动装配注册 Bean 名为 milvusVectorStore
 * - Qdrant 自动装配注册 Bean 名为 qdrantVectorStore
 * 本配置按用途二次命名，业务侧通过 @Qualifier("knowledgeVectorStore") 等注入。
 *
 * v10 修正：每个方法添加 @ConditionalOnBean，确保本地无 Milvus/Qdrant 服务时
 * 不会因底层 VectorStore 缺失而导致启动失败。下游注入者用 @Autowired(required=false) 降级。
 *
 * 路由开关（application.properties）：
 * - jarvis.vector-store.routing.knowledge     = milvus | qdrant
 * - jarvis.vector-store.routing.chat-memory   = milvus | qdrant
 * - jarvis.vector-store.routing.user-profile  = milvus | qdrant
 * - jarvis.vector-store.routing.skills-meta   = milvus | qdrant
 */
@Configuration
public class VectorStoreConfig {

    @Bean("knowledgeVectorStore")
    @ConditionalOnProperty(name = "jarvis.vector-store.routing.knowledge", havingValue = "milvus", matchIfMissing = true)
    @ConditionalOnBean(name = "milvusVectorStore")
    public VectorStore knowledgeVectorStoreByMilvus(
            @Qualifier("milvusVectorStore") VectorStore milvusVectorStore) {
        return milvusVectorStore;
    }

    @Bean("knowledgeVectorStore")
    @ConditionalOnProperty(name = "jarvis.vector-store.routing.knowledge", havingValue = "qdrant")
    @ConditionalOnBean(name = "qdrantVectorStore")
    public VectorStore knowledgeVectorStoreByQdrant(
            @Qualifier("qdrantVectorStore") VectorStore qdrantVectorStore) {
        return qdrantVectorStore;
    }

    @Bean("chatMemoryVectorStore")
    @ConditionalOnProperty(name = "jarvis.vector-store.routing.chat-memory", havingValue = "milvus", matchIfMissing = true)
    @ConditionalOnBean(name = "milvusVectorStore")
    public VectorStore chatMemoryVectorStoreByMilvus(
            @Qualifier("milvusVectorStore") VectorStore milvusVectorStore) {
        return milvusVectorStore;
    }

    @Bean("chatMemoryVectorStore")
    @ConditionalOnProperty(name = "jarvis.vector-store.routing.chat-memory", havingValue = "qdrant")
    @ConditionalOnBean(name = "qdrantVectorStore")
    public VectorStore chatMemoryVectorStoreByQdrant(
            @Qualifier("qdrantVectorStore") VectorStore qdrantVectorStore) {
        return qdrantVectorStore;
    }

    @Bean("userProfileVectorStore")
    @ConditionalOnProperty(name = "jarvis.vector-store.routing.user-profile", havingValue = "milvus", matchIfMissing = true)
    @ConditionalOnBean(name = "milvusVectorStore")
    public VectorStore userProfileVectorStoreByMilvus(
            @Qualifier("milvusVectorStore") VectorStore milvusVectorStore) {
        return milvusVectorStore;
    }

    @Bean("userProfileVectorStore")
    @ConditionalOnProperty(name = "jarvis.vector-store.routing.user-profile", havingValue = "qdrant")
    @ConditionalOnBean(name = "qdrantVectorStore")
    public VectorStore userProfileVectorStoreByQdrant(
            @Qualifier("qdrantVectorStore") VectorStore qdrantVectorStore) {
        return qdrantVectorStore;
    }

    @Bean("skillsMetaVectorStore")
    @ConditionalOnProperty(name = "jarvis.vector-store.routing.skills-meta", havingValue = "qdrant", matchIfMissing = true)
    @ConditionalOnBean(name = "qdrantVectorStore")
    public VectorStore skillsMetaVectorStoreByQdrant(
            @Qualifier("qdrantVectorStore") VectorStore qdrantVectorStore) {
        return qdrantVectorStore;
    }

    @Bean("skillsMetaVectorStore")
    @ConditionalOnProperty(name = "jarvis.vector-store.routing.skills-meta", havingValue = "milvus")
    @ConditionalOnBean(name = "milvusVectorStore")
    public VectorStore skillsMetaVectorStoreByMilvus(
            @Qualifier("milvusVectorStore") VectorStore milvusVectorStore) {
        return milvusVectorStore;
    }
}
