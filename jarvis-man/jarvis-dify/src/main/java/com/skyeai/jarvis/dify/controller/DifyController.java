package com.skyeai.jarvis.dify.controller;

import com.skyeai.jarvis.dify.service.DifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dify")
public class DifyController {

    @Autowired
    private DifyService difyService;

    /**
     * 调用Dify聊天API
     * @param query 用户查询
     * @param conversationId 对话ID
     * @param inputs 输入参数
     * @return 聊天响应
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(
            @RequestParam String query,
            @RequestParam(required = false) String conversationId,
            @RequestBody(required = false) Map<String, Object> inputs) {
        return difyService.chat(query, conversationId, inputs);
    }

    /**
     * 创建Dify应用
     * @param name 应用名称
     * @param description 应用描述
     * @param model 模型配置
     * @return 应用信息
     */
    @PostMapping("/application")
    public Map<String, Object> createApplication(
            @RequestParam String name,
            @RequestParam String description,
            @RequestBody Map<String, Object> model) {
        return difyService.createApplication(name, description, model);
    }

    /**
     * 获取应用列表
     * @return 应用列表
     */
    @GetMapping("/applications")
    public Map<String, Object> getApplications() {
        return difyService.getApplications();
    }

    /**
     * 扩展文档上下文
     * @param content 文档内容
     * @param maxLength 最大长度
     * @return 扩展后的上下文
     */
    @PostMapping("/extend-context")
    public String extendDocumentContext(
            @RequestParam String content,
            @RequestParam(defaultValue = "10000") int maxLength) {
        return difyService.extendDocumentContext(content, maxLength);
    }

    /**
     * 健康检查
     * @return 健康状态
     */
    @GetMapping("/health")
    public String healthCheck() {
        boolean isHealthy = difyService.healthCheck();
        return "Dify Integration Service is " + (isHealthy ? "healthy" : "unhealthy");
    }
}
