package com.skyeai.jarvis.rag.service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.QdrantOuterClass;
import io.qdrant.client.grpc.QdrantOuterClass.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    @Value("${qdrant.host}")
    private String qdrantHost;

    @Value("${qdrant.port}")
    private int qdrantPort;

    @Value("${qdrant.grpc-port}")
    private int qdrantGrpcPort;

    @Value("${qdrant.api-key}")
    private String qdrantApiKey;

    private QdrantClient qdrantClient;

    @PostConstruct
    public void init() {
        // 初始化Qdrant客户端
        try {
            this.qdrantClient = new QdrantClient(
                    QdrantClient.Config.newBuilder()
                            .setHost(qdrantHost)
                            .setPort(qdrantPort)
                            .setApiKey(qdrantApiKey)
                            .build()
            );
            System.out.println("Qdrant client initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize Qdrant client: " + e.getMessage());
        }
    }

    /**
     * 检索相关文档
     * @param query 查询文本
     * @param collectionName 集合名称
     * @param limit 检索数量
     * @return 检索结果
     */
    public List<DocumentResult> retrieveDocuments(String query, String collectionName, int limit) {
        try {
            // 生成查询向量（实际应用中应该使用嵌入模型）
            List<Float> queryVector = generateEmbedding(query);

            // 构建检索请求
            SearchPointsRequest request = SearchPointsRequest.newBuilder()
                    .setCollectionName(collectionName)
                    .setLimit(limit)
                    .setVector(Vector.newBuilder()
                            .addAllData(queryVector)
                            .build())
                    .build();

            // 执行检索
            SearchResponse response = qdrantClient.search(request);

            // 处理检索结果
            List<DocumentResult> results = new ArrayList<>();
            for (ScoredPoint scoredPoint : response.getResultList()) {
                DocumentResult result = new DocumentResult();
                result.setId(scoredPoint.getId().getUuid());
                result.setScore(scoredPoint.getScore());
                result.setPayload(scoredPoint.getPayloadMap());
                results.add(result);
            }

            return results;
        } catch (Exception e) {
            System.err.println("Failed to retrieve documents: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 融合检索结果到上下文
     * @param query 查询文本
     * @param documents 检索到的文档
     * @return 融合后的上下文
     */
    public String fuseDocuments(String query, List<DocumentResult> documents) {
        StringBuilder context = new StringBuilder();
        context.append("Query: " + query + "\n\n");
        context.append("Relevant Documents:\n");

        for (int i = 0; i < documents.size(); i++) {
            DocumentResult doc = documents.get(i);
            context.append("Document " + (i + 1) + " (Score: " + doc.getScore() + "):\n");
            context.append(doc.getPayload().get("content").getStringValue() + "\n\n");
        }

        return context.toString();
    }

    /**
     * 生成文本嵌入向量
     * @param text 文本
     * @return 嵌入向量
     */
    private List<Float> generateEmbedding(String text) {
        // 实际应用中应该使用嵌入模型，这里返回模拟向量
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < 1536; i++) {
            vector.add((float) (Math.random() * 2 - 1));
        }
        return vector;
    }

    /**
     * 文档结果类
     */
    public static class DocumentResult {
        private String id;
        private float score;
        private Map<String, Value> payload;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public float getScore() {
            return score;
        }

        public void setScore(float score) {
            this.score = score;
        }

        public Map<String, Value> getPayload() {
            return payload;
        }

        public void setPayload(Map<String, Value> payload) {
            this.payload = payload;
        }
    }
}
