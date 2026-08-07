package com.skyeai.jarvis.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 多路召回RAG服务
 * 集成语义向量检索、关键词检索和查询改写
 * 使用RRF融合算法合并多路召回结果
 */
@Slf4j
@Service
public class MultiRecallRagService {

    /**
     * RRF融合常数
     */
    private static final int RRF_CONSTANT_K = 60;

    @Value("${rag.knowledge.loaded:false}")
    private boolean knowledgeLoaded;

    @Value("${rag.embedding.enabled:true}")
    private boolean embeddingEnabled;

    @Autowired
    private org.springframework.ai.vectorstore.VectorStore vectorStore;

    @Autowired
    private org.springframework.ai.embedding.EmbeddingModel embeddingModel;

    @Autowired
    private QueryRewriter queryRewriter;

    @Autowired
    private KeywordStore keywordStore;

    @Autowired
    private RrfFusion rrfFusion;

    @Autowired
    private Reranker reranker;

    @PostConstruct
    public void init() {
        // 初始化关键词存储（示例数据）
        initializeKeywordStore();
    }

    /**
     * 初始化关键词存储（示例）
     */
    private void initializeKeywordStore() {
        // 添加示例文档到关键词存储
        keywordStore.addDocument("doc1", "贾维斯是Stark Industries开发的AI助手", Map.of("source", "manual"));
        keywordStore.addDocument("doc2", "Jarvis最初是Edwin Jarvis的名字", Map.of("source", "manual"));
        keywordStore.addDocument("doc3", "托尼·斯塔克创造了贾维斯人工智能系统", Map.of("source", "manual"));
        log.info("关键词存储初始化完成，文档数: {}", keywordStore.getDocumentCount());
    }

    /**
     * 查询
     * @param query 查询文本
     * @param collectionName 集合名称
     * @param limit 返回数量
     * @return 融合后的上下文
     */
    public String query(String query, String collectionName, int limit) {
        if (!knowledgeLoaded) {
            log.warn("知识库未加载");
            return null;
        }

        // 1. 查询改写
        List<String> rewrittenQueries = queryRewriter.rewrite(query);
        log.debug("查询改写完成，改写后查询数: {}", rewrittenQueries.size());

        // 2. 多路召回
        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, Reranker.RerankedDocument> keyToDocument = new HashMap<>();

        // 语义向量检索
        List<Reranker.RerankedDocument> vectorResults = vectorSearch(query, collectionName, limit);
        accumulateRrfScores(vectorResults, rrfScores, keyToDocument, 0);

        // 关键词检索
        for (String rewrittenQuery : rewrittenQueries) {
            List<KeywordStore.KeywordSearchResult> keywordResults = keywordStore.search(rewrittenQuery, limit);

            for (int rank = 0; rank < keywordResults.size(); rank++) {
                KeywordStore.KeywordSearchResult kr = keywordResults.get(rank);
                String key = kr.getId();

                Reranker.RerankedDocument existingDoc = keyToDocument.get(key);
                if (existingDoc == null) {
                    Reranker.RerankedDocument newDoc = new Reranker.RerankedDocument(key, kr.getContent(), kr.getScore(), rank);
                    keyToDocument.put(key, newDoc);
                }

                // RRF公式: score += 1.0 / (k + rank)
                double score = 1.0 / (RRF_CONSTANT_K + rank + 1);
                rrfScores.merge(key, score, Double::sum);
            }
        }

        // 3. RRF融合
        List<Reranker.RerankedDocument> fusedResults = rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Reranker.RerankedDocument doc = keyToDocument.get(entry.getKey());
                    doc.setOriginalScore(entry.getValue());
                    return doc;
                })
                .collect(Collectors.toList());

        // 4. 重排
        List<Reranker.RerankedDocument> rerankedResults = reranker.rerank(query, fusedResults);

        // 5. 构建上下文
        return buildContext(rerankedResults);
    }

    /**
     * 语义向量检索（委托 Spring AI VectorStore）
     */
    private List<Reranker.RerankedDocument> vectorSearch(String query, String collectionName, int limit) {
        List<Reranker.RerankedDocument> results = new ArrayList<>();
        try {
            org.springframework.ai.vectorstore.SearchRequest request =
                org.springframework.ai.vectorstore.SearchRequest.builder()
                    .query(query)
                    .topK(limit)
                    .build();
            List<org.springframework.ai.document.Document> docs = vectorStore.similaritySearch(request);
            int rank = 0;
            for (org.springframework.ai.document.Document doc : docs) {
                String id = doc.getId();
                double score = doc.getMetadata() != null && doc.getMetadata().containsKey("distance")
                    ? ((Number) doc.getMetadata().get("distance")).doubleValue() : 1.0 - (rank * 0.1);
                String content = doc.getText();
                results.add(new Reranker.RerankedDocument(id, content, score, rank++));
            }
        } catch (Exception e) {
            log.error("向量检索失败", e);
        }
        return results;
    }

    /**
     * 累积RRF分数
     */
    private void accumulateRrfScores(List<Reranker.RerankedDocument> results,
                                    Map<String, Double> rrfScores,
                                    Map<String, Reranker.RerankedDocument> keyToDocument,
                                    int listIndex) {
        for (int rank = 0; rank < results.size(); rank++) {
            Reranker.RerankedDocument doc = results.get(rank);
            String key = doc.getId();
            keyToDocument.putIfAbsent(key, doc);

            // RRF公式
            double score = 1.0 / (RRF_CONSTANT_K + rank + 1);
            rrfScores.merge(key, score, Double::sum);
        }
    }

    /**
     * 构建上下文
     */
    private String buildContext(List<Reranker.RerankedDocument> documents) {
        if (documents.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("以下是检索到的相关参考资料：\n\n");

        for (int i = 0; i < documents.size(); i++) {
            Reranker.RerankedDocument doc = documents.get(i);
            context.append(String.format("【文档%d】(相关性: %.2f)\n", i + 1, doc.getRelevanceScore()));
            context.append(doc.getContent()).append("\n\n");
        }

        return context.toString();
    }

    /**
     * 生成文本嵌入向量（委托 Spring AI EmbeddingModel）
     */
    private List<Float> generateEmbedding(String text) {
        try {
            float[] vector = embeddingModel.embed(text);
            List<Float> result = new ArrayList<>(vector.length);
            for (float v : vector) {
                result.add(v);
            }
            log.debug("嵌入生成成功，向量维度: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("嵌入生成失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 检查知识库是否已加载
     */
    public boolean isKnowledgeLoaded() {
        return knowledgeLoaded;
    }

    /**
     * 设置知识库加载状态
     */
    public void setKnowledgeLoaded(boolean loaded) {
        this.knowledgeLoaded = loaded;
    }
}