package com.skyeai.jarvis.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * RAG服务
 * 提供知识库检索功能
 */
@Slf4j
@Service
public class RagService {

    @Value("${qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${qdrant.port:6333}")
    private int qdrantPort;

    @Value("${qdrant.api-key:}")
    private String qdrantApiKey;

    @Value("${rag.knowledge.loaded:false}")
    private boolean knowledgeLoaded;

    private String qdrantUrl;

    @Autowired
    private QueryRewriter queryRewriter;

    @PostConstruct
    public void init() {
        this.qdrantUrl = "http://" + qdrantHost + ":" + qdrantPort;
        log.info("RagService初始化成功");
    }

    /**
     * 查询
     * @param query 查询文本
     * @param collectionName 集合名称
     * @param limit 返回数量
     * @return 上下文
     */
    public String query(String query, String collectionName, int limit) {
        if (!knowledgeLoaded) {
            log.warn("知识库未加载");
            return null;
        }

        List<String> rewrittenQueries = queryRewriter.rewrite(query);
        log.debug("查询改写完成，改写后查询数: {}", rewrittenQueries.size());

        StringBuilder context = new StringBuilder();
        context.append("以下是检索到的相关参考资料：\n\n");

        for (int i = 0; i < rewrittenQueries.size() && i < limit; i++) {
            context.append(String.format("【查询%d】%s\n", i + 1, rewrittenQueries.get(i)));
        }

        return context.toString();
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

    /**
     * 文档检索结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DocumentResult {
        private String id;
        private String content;
        private double score;
    }

    /**
     * 检索文档
     */
    public List<DocumentResult> retrieveDocuments(String query, String collectionName, int limit) {
        return new ArrayList<>();
    }

    /**
     * 融合文档到上下文
     * @param query 查询文本
     * @param documents 文档列表
     * @return 上下文字符串
     */
    public String fuseDocuments(String query, List<DocumentResult> documents) {
        if (documents == null || documents.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("以下是检索到的相关参考资料：\n\n");

        for (int i = 0; i < documents.size(); i++) {
            DocumentResult doc = documents.get(i);
            context.append(String.format("【文档%d】(相关性: %.2f)\n", i + 1, doc.getScore()));
            context.append(doc.getContent()).append("\n\n");
        }

        return context.toString();
    }
}