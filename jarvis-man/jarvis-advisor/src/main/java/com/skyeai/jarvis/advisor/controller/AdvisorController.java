package com.skyeai.jarvis.advisor.controller;

import com.skyeai.jarvis.advisor.service.AdvisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advisor")
public class AdvisorController {

    @Autowired
    private AdvisorService advisorService;

    /**
     * 生成溯源回答
     * @param query 用户查询
     * @param sources 信息来源列表
     * @return 溯源回答
     */
    @PostMapping("/traceable-answer")
    public AdvisorService.TraceableAnswer generateTraceableAnswer(
            @RequestParam String query,
            @RequestBody List<AdvisorService.InformationSource> sources) {
        return advisorService.generateTraceableAnswer(query, sources);
    }

    /**
     * 创建元数据
     * @param metadata 元数据
     * @return 元数据ID
     */
    @PostMapping("/metadata")
    public String createMetadata(@RequestBody AdvisorService.Metadata metadata) {
        return advisorService.createMetadata(metadata);
    }

    /**
     * 更新元数据
     * @param metadataId 元数据ID
     * @param metadata 元数据
     * @return 是否成功
     */
    @PutMapping("/metadata/{metadataId}")
    public boolean updateMetadata(
            @PathVariable String metadataId,
            @RequestBody AdvisorService.Metadata metadata) {
        return advisorService.updateMetadata(metadataId, metadata);
    }

    /**
     * 获取元数据
     * @param metadataId 元数据ID
     * @return 元数据
     */
    @GetMapping("/metadata/{metadataId}")
    public AdvisorService.Metadata getMetadata(@PathVariable String metadataId) {
        return advisorService.getMetadata(metadataId);
    }

    /**
     * 健康检查
     * @return 健康状态
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "Advisor Service is healthy";
    }
}
