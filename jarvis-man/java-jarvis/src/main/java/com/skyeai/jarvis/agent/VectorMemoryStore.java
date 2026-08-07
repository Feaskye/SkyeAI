package com.skyeai.jarvis.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量库长期记忆存储
 * v10 新增：对话消息向量化落库 + 跨会话历史记忆检索
 *
 * 设计：
 * - 写入：将 UserMessage/AssistantMessage 文本向量化后存入 chatMemoryVectorStore
 * - 检索：用户问题向量化 → 相似度搜索 → 返回相关历史上下文
 * - 元数据：sessionId/role/timestamp 随向量一起存储，支持过滤
 */
@Slf4j
@Component
public class VectorMemoryStore {

    /** 对话记忆 VectorStore（通过 VectorStoreConfig 路由到 Milvus/Qdrant）
     *  v10 修正：required=false，本地无 Milvus/Qdrant 时优雅降级 */
    @Autowired(required = false)
    @Qualifier("chatMemoryVectorStore")
    private VectorStore chatMemoryVectorStore;

    @Value("${jarvis.memory.retrieval.top-k:5}")
    private int defaultTopK;

    @Value("${jarvis.memory.retrieval.session-scope:false}")
    private boolean sessionScope;

    /**
     * 将 Spring AI Message 存入向量库
     * @param sessionId 会话ID
     * @param message Spring AI 消息（UserMessage/AssistantMessage）
     */
    public void store(String sessionId, Message message) {
        if (message == null) return;
        if (chatMemoryVectorStore == null) return;
        String text = message.getText();
        if (text == null || text.isBlank()) return;

        try {
            String role = determineRole(message);
            Document doc = Document.builder()
                    .id(UUID.randomUUID().toString())
                    .text(text)
                    .metadata(Map.of(
                            "sessionId", sessionId != null ? sessionId : "unknown",
                            "role", role,
                            "timestamp", System.currentTimeMillis()
                    ))
                    .build();
            chatMemoryVectorStore.add(List.of(doc));
            log.debug("向量记忆写入成功 - sessionId: {}, role: {}, textLength: {}",
                    sessionId, role, text.length());
        } catch (Exception e) {
            log.error("向量记忆写入失败 - sessionId: {}, error: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 检索与查询相关的历史记忆
     * @param sessionId 会话ID（sessionScope=true 时限定当前会话）
     * @param query 查询文本
     * @param topK 返回条数
     * @return 相关历史文档列表
     */
    public List<Document> retrieveRelevant(String sessionId, String query, int topK) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        if (chatMemoryVectorStore == null) {
            return Collections.emptyList();
        }

        int limit = topK > 0 ? topK : defaultTopK;

        try {
            SearchRequest.Builder builder = SearchRequest.builder()
                    .query(query)
                    .topK(limit);

            // 可选：限定当前会话
            if (sessionScope && sessionId != null) {
                builder.filterExpression("sessionId == '" + sessionId + "'");
            }

            SearchRequest request = builder.build();
            List<Document> results = chatMemoryVectorStore.similaritySearch(request);

            log.debug("历史记忆检索完成 - sessionId: {}, query: {}, 命中: {} 条",
                    sessionId, query.substring(0, Math.min(query.length(), 30)), results.size());
            return results;
        } catch (Exception e) {
            log.error("历史记忆检索失败 - sessionId: {}, error: {}", sessionId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 检索并格式化为上下文文本
     * @param sessionId 会话ID
     * @param query 查询文本
     * @param topK 返回条数
     * @return 格式化的历史上下文文本
     */
    public String retrieveAndFormat(String sessionId, String query, int topK) {
        List<Document> docs = retrieveRelevant(sessionId, query, topK);
        if (docs.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("以下是从历史对话中检索到的相关上下文：\n\n");
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            String role = doc.getMetadata() != null
                    ? String.valueOf(doc.getMetadata().getOrDefault("role", "unknown"))
                    : "unknown";
            context.append(String.format("【历史记录%d】(%s)\n%s\n\n", i + 1, role, doc.getText()));
        }
        return context.toString();
    }

    /**
     * 判断消息角色
     */
    private String determineRole(Message message) {
        if (message instanceof org.springframework.ai.chat.messages.UserMessage) {
            return "user";
        } else if (message instanceof org.springframework.ai.chat.messages.AssistantMessage) {
            return "assistant";
        } else if (message instanceof org.springframework.ai.chat.messages.SystemMessage) {
            return "system";
        }
        return "unknown";
    }
}
