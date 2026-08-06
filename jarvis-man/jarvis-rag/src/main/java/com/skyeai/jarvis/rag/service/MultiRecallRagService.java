package com.skyeai.jarvis.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
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

    @Value("${qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${qdrant.port:6333}")
    private int qdrantPort;

    @Value("${qdrant.api-key:}")
    private String qdrantApiKey;

    @Value("${rag.knowledge.loaded:false}")
    private boolean knowledgeLoaded;

    @Value("${llm.service.url:http://localhost:8081}")
    private String llmServiceUrl;

    @Value("${rag.embedding.enabled:true}")
    private boolean embeddingEnabled;

    private WebClient qdrantWebClient;

    private WebClient llmWebClient;

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
        // 初始化 Qdrant WebClient
        this.qdrantWebClient = WebClient.builder()
                .baseUrl("http://" + qdrantHost + ":" + qdrantPort)
                .defaultHeader("api-key", qdrantApiKey)
                .build();
        log.info("Qdrant WebClient初始化成功");

        // 初始化 LLM 服务 WebClient（用于嵌入生成）
        this.llmWebClient = WebClient.builder()
                .baseUrl(llmServiceUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        log.info("LLM服务WebClient初始化成功，地址: {}", llmServiceUrl);

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
     * 语义向量检索
     */
    private List<Reranker.RerankedDocument> vectorSearch(String query, String collectionName, int limit) {
        List<Reranker.RerankedDocument> results = new ArrayList<>();

        try {
            // 生成查询向量
            List<Float> queryVector = generateEmbedding(query);

            // 构建检索请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("vector", queryVector);
            requestBody.put("limit", limit);
            requestBody.put("withPayload", true);

            // 调用Qdrant REST API
            Map<String, Object> response = qdrantWebClient.post()
                    .uri("/collections/{collection}/points/search", collectionName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 处理结果
            if (response != null && response.containsKey("result")) {
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) response.get("result");
                int rank = 0;
                for (Map<String, Object> point : resultList) {
                    String id = String.valueOf(point.get("id"));
                    double score = ((Number) point.get("score")).doubleValue();

                    Map<String, Object> payload = (Map<String, Object>) point.get("payload");
                    String content = payload != null ? String.valueOf(payload.getOrDefault("content", "")) : "";

                    Reranker.RerankedDocument doc = new Reranker.RerankedDocument(id, content, score, rank++);
                    results.add(doc);
                }
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
     * 生成文本嵌入向量
     * 优先使用真实嵌入服务，失败时降级为基于哈希的伪向量
     */
    private List<Float> generateEmbedding(String text) {
        if (!embeddingEnabled) {
            return generateFallbackEmbedding(text);
        }

        try {
            // 调用 jarvis-llm 的嵌入服务
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);

            List<Double> result = llmWebClient.post()
                    .uri("/api/llm/agent/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<Double>>() {})
                    .block();

            if (result != null && !result.isEmpty()) {
                // 将 Double 转换为 Float
                List<Float> vector = new ArrayList<>(result.size());
                for (Double v : result) {
                    vector.add(v.floatValue());
                }
                log.debug("嵌入生成成功，向量维度: {}", vector.size());
                return vector;
            } else {
                log.warn("嵌入服务返回空结果，使用降级策略");
                return generateFallbackEmbedding(text);
            }
        } catch (Exception e) {
            log.warn("嵌入服务调用失败，使用降级策略: {}", e.getMessage());
            return generateFallbackEmbedding(text);
        }
    }

    /**
     * 降级嵌入策略（当真实嵌入服务不可用时使用）
     * 基于文本哈希生成确定性的伪向量，保证检索功能可用
     */
    private List<Float> generateFallbackEmbedding(String text) {
        List<Float> vector = new ArrayList<>(1536);
        int hash = text.hashCode();
        for (int i = 0; i < 1536; i++) {
            // 基于哈希生成确定性的伪随机向量
            float value = (float) (Math.sin((hash + i) * 0.01) * 0.5);
            vector.add(value);
        }
        // 归一化
        float norm = 0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.size(); i++) {
                vector.set(i, vector.get(i) / norm);
            }
        }
        log.debug("使用降级嵌入策略生成向量，文本哈希: {}", hash);
        return vector;
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