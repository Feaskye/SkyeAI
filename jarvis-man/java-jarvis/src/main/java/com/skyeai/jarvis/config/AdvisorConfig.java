package com.skyeai.jarvis.config;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Advisor 链配置
 * v10 新增：
 * - RetrievalAugmentationAdvisor (RAG 上下文注入，替代旧 QuestionAnswerAdvisor)
 * - MessageChatMemoryAdvisor (对话记忆注入，基于 Spring AI ChatMemory 接口)
 *
 * 开关：
 * - jarvis.advisor.enable.rag=true 装配 RAG Advisor
 * - jarvis.advisor.enable.memory=true 装配记忆 Advisor
 */
@Configuration
public class AdvisorConfig {

    /**
     * RAG 问答 Advisor
     * Spring AI 2.0：RetrievalAugmentationAdvisor 替代旧 QuestionAnswerAdvisor
     * 基于 VectorStoreDocumentRetriever 检索 TopK 相关文档，注入 Prompt 上下文
     */
    @Bean
    @ConditionalOnProperty(name = "jarvis.advisor.enable.rag", havingValue = "true")
    public RetrievalAugmentationAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .topK(5)
                        .build())
                .build();
    }

    /**
     * 对话记忆 Advisor
     * Spring AI 2.0：自动从 ChatMemory 按 conversationId 读取历史消息并注入 Prompt
     * 底层实现为 SpringAiChatMemory（三级存储：SessionState ↔ Redis ↔ Milvus）
     */
    @Bean
    @ConditionalOnProperty(name = "jarvis.advisor.enable.memory", havingValue = "true", matchIfMissing = true)
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }
}
