package com.skyeai.jarvis.knowledge.controller;

import com.skyeai.jarvis.knowledge.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 创建知识库
     * @param name 知识库名称
     * @param description 知识库描述
     * @return 知识库ID
     */
    @PostMapping("/create")
    public String createKnowledgeBase(
            @RequestParam String name,
            @RequestParam String description) {
        return knowledgeBaseService.createKnowledgeBase(name, description);
    }

    /**
     * 上传文档到知识库
     * @param kbId 知识库ID
     * @param file 文档文件
     * @return 文档ID
     */
    @PostMapping("/upload")
    public String uploadDocument(
            @RequestParam String kbId,
            @RequestParam("file") MultipartFile file) {
        return knowledgeBaseService.uploadDocument(kbId, file);
    }

    /**
     * 删除知识库
     * @param kbId 知识库ID
     * @return 是否成功
     */
    @DeleteMapping("/delete/{kbId}")
    public boolean deleteKnowledgeBase(@PathVariable String kbId) {
        return knowledgeBaseService.deleteKnowledgeBase(kbId);
    }

    /**
     * 获取知识库列表
     * @return 知识库列表
     */
    @GetMapping("/list")
    public List<KnowledgeBaseService.KnowledgeBaseInfo> getKnowledgeBases() {
        return knowledgeBaseService.getKnowledgeBases();
    }

    /**
     * 健康检查
     * @return 健康状态
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "Knowledge Base Service is healthy";
    }
}
