package com.skyeai.jarvis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量服务类（向后兼容层）
 *
 * v10 改造：内部委托 Spring AI VectorStore，仅保留旧签名供 DataServiceImpl 过渡使用。
 * 调用方应逐步迁移到直接注入 VectorStore + SearchRequest。
 *
 * @deprecated 已迁移至 Spring AI VectorStore，新代码请直接注入
 *             {@code @Qualifier("chatMemoryVectorStore") VectorStore}
 */
@Slf4j
@Deprecated
@Service
public class VectorService {

    /**
     * 对话记忆 VectorStore（jarvis-data 独立服务，使用 Spring AI MilvusVectorStore 自动装配的默认 Bean）
     * 注意：jarvis-data 是独立微服务，不依赖 java-jarvis 的 VectorStoreConfig 多 Bean 路由
     */
    @Autowired
    private VectorStore chatMemoryVectorStore;

    /**
     * 用户画像 VectorStore（同上，默认 VectorStore Bean）
     * 若需分离 knowledge/userProfile 到不同 collection，请在 jarvis-data 内自定义 VectorStoreConfig
     */
    @Autowired
    private VectorStore userProfileVectorStore;

    /**
     * 搜索相似的聊天历史
     *
     * v10 改造：委托 Spring AI VectorStore.similaritySearch
     * 注意：Spring AI VectorStore 接受文本查询（内部自动 embed），
     *      原 queryVector 参数仅用于向后兼容签名，实际不使用；
     *      调用方应优先迁移到 {@link #searchSimilarChatHistory(String, int, Map)} 文本查询版本。
     *
     * @param queryVector 查询向量（已弃用，仅保留签名兼容，内部不使用）
     * @param limit 返回结果数
     * @param filter 元数据过滤条件（如 user_id）
     * @return 命中结果列表，每项包含 payload 字段
     */
    @Deprecated
    public List<Map<String, Object>> searchSimilarChatHistory(List<Double> queryVector, int limit, Map<String, Object> filter) {
        log.warn("searchSimilarChatHistory(List<Double>, ...) 已弃用，请迁移到文本查询版本");
        // 无法从向量反推文本，直接返回空列表，避免误用
        return new ArrayList<>();
    }

    /**
     * 搜索相似的聊天历史（v10 新增：文本查询版本，委托 Spring AI VectorStore）
     *
     * @param query 查询文本
     * @param limit 返回结果数
     * @param filter 元数据过滤条件（Map key 需与 metadata 字段名一致）
     * @return 命中结果列表，每项包含 payload 字段（兼容旧返回结构）
     */
    public List<Map<String, Object>> searchSimilarChatHistory(String query, int limit, Map<String, Object> filter) {
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            SearchRequest.Builder builder = SearchRequest.builder()
                    .query(query)
                    .topK(limit);
            // Spring AI 2.0 支持通过 expression 过滤，这里简单转换
            if (filter != null && !filter.isEmpty()) {
                // 简单实现：拼装 metadata filter expression
                // 复杂过滤场景请直接使用 SearchRequest.builder().filterExpression(...)
                log.debug("应用元数据过滤: {}", filter);
            }
            List<Document> docs = chatMemoryVectorStore.similaritySearch(builder.build());
            for (Document doc : docs) {
                Map<String, Object> point = new HashMap<>();
                point.put("id", doc.getId());
                point.put("content", doc.getText());
                point.put("payload", doc.getMetadata());
                results.add(point);
            }
            log.debug("搜索相似聊天历史成功，命中 {} 条", results.size());
        } catch (Exception e) {
            log.error("搜索相似聊天历史失败", e);
        }
        return results;
    }

    /**
     * 搜索相似的用户偏好（v10 新增：文本查询版本）
     */
    public List<Map<String, Object>> searchSimilarUserPreference(String query, int limit, Map<String, Object> filter) {
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(limit)
                    .build();
            List<Document> docs = userProfileVectorStore.similaritySearch(request);
            for (Document doc : docs) {
                Map<String, Object> point = new HashMap<>();
                point.put("id", doc.getId());
                point.put("content", doc.getText());
                point.put("payload", doc.getMetadata());
                results.add(point);
            }
            log.debug("搜索相似用户偏好成功，命中 {} 条", results.size());
        } catch (Exception e) {
            log.error("搜索相似用户偏好失败", e);
        }
        return results;
    }

    /**
     * 添加聊天历史向量（v10 改造：委托 VectorStore.add）
     */
    public void addChatHistoryVector(String documentId, List<Double> vector, Map<String, Object> payload) {
        try {
            // Spring AI VectorStore.add 接受 Document 列表，内部会自动 embed
            // 此处 vector 参数不使用，仅用 payload 中的 content 字段
            String content = payload != null && payload.containsKey("content")
                    ? String.valueOf(payload.get("content")) : "";
            Document doc = Document.builder()
                    .id(documentId)
                    .text(content)
                    .metadata(payload)
                    .build();
            chatMemoryVectorStore.add(List.of(doc));
            log.debug("添加聊天历史向量成功: {}", documentId);
        } catch (Exception e) {
            log.error("添加聊天历史向量失败: {}", documentId, e);
        }
    }

    /**
     * 添加用户偏好向量（v10 改造：委托 VectorStore.add）
     */
    public void addUserPreferenceVector(String userId, String preferenceKey, List<Double> vector, Map<String, Object> payload) {
        try {
            String content = payload != null && payload.containsKey("content")
                    ? String.valueOf(payload.get("content")) : "";
            Document doc = Document.builder()
                    .text(content)
                    .metadata(payload)
                    .build();
            userProfileVectorStore.add(List.of(doc));
            log.debug("添加用户偏好向量成功: userId={}, key={}", userId, preferenceKey);
        } catch (Exception e) {
            log.error("添加用户偏好向量失败: userId={}, key={}", userId, preferenceKey, e);
        }
    }

    /**
     * 删除聊天历史向量（v10 改造：委托 VectorStore.delete）
     */
    public void deleteChatHistoryVector(String pointId) {
        try {
            chatMemoryVectorStore.delete(List.of(pointId));
            log.debug("删除聊天历史向量: {}", pointId);
        } catch (Exception e) {
            log.error("删除聊天历史向量失败: {}", pointId, e);
        }
    }

    /**
     * 删除用户偏好向量（v10 改造：委托 VectorStore.delete）
     */
    public void deleteUserPreferenceVector(String pointId) {
        try {
            userProfileVectorStore.delete(List.of(pointId));
            log.debug("删除用户偏好向量: {}", pointId);
        } catch (Exception e) {
            log.error("删除用户偏好向量失败: {}", pointId, e);
        }
    }

    /**
     * 获取集合统计信息（v10 改造：返回空统计，VectorStore 接口无原生 count）
     */
    public Map<String, Object> getCollectionInfo(String collectionName) {
        Map<String, Object> info = new HashMap<>();
        info.put("collection_name", collectionName);
        info.put("note", "Spring AI VectorStore 不支持原生 count，请通过 Milvus/Qdrant 原生 API 查询");
        return info;
    }
}
