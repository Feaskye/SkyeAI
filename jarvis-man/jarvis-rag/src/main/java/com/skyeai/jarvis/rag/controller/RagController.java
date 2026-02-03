package com.skyeai.jarvis.rag.controller;

import com.skyeai.jarvis.rag.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    @Autowired
    private RagService ragService;

    /**
     * 检索相关文档
     * @param query 查询文本
     * @param collectionName 集合名称
     * @param limit 检索数量
     * @return 检索结果
     */
    @GetMapping("/retrieve")
    public List<RagService.DocumentResult> retrieveDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "documents") String collectionName,
            @RequestParam(defaultValue = "5") int limit) {
        return ragService.retrieveDocuments(query, collectionName, limit);
    }

    /**
     * 融合检索结果到上下文
     * @param query 查询文本
     * @param collectionName 集合名称
     * @param limit 检索数量
     * @return 融合后的上下文
     */
    @GetMapping("/fuse")
    public String fuseDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "documents") String collectionName,
            @RequestParam(defaultValue = "5") int limit) {
        List<RagService.DocumentResult> documents = ragService.retrieveDocuments(query, collectionName, limit);
        return ragService.fuseDocuments(query, documents);
    }

    /**
     * 健康检查
     * @return 健康状态
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "RAG Service is healthy";
    }
}
