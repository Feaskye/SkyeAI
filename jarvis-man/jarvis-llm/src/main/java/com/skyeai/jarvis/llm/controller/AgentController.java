package com.skyeai.jarvis.llm.controller;

import com.skyeai.jarvis.llm.service.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Agent专用控制器
 * 提供Agent调用LLM服务的接口
 */
@RestController
@RequestMapping("/api/llm/agent")
public class AgentController {

    @Autowired
    private LlmService llmService;

    /**
     * Agent调用模型（消息列表格式）
     */
    @PostMapping("/chat")
    public LlmService.AgentResponse chat(@RequestBody Map<String, Object> request) {
        String systemPrompt = (String) request.getOrDefault("systemPrompt", "You are a helpful assistant");
        List<Map<String, String>> messages = (List<Map<String, String>>) request.get("messages");
        List<Map<String, Object>> tools = (List<Map<String, Object>>) request.getOrDefault("tools", List.of());
        boolean toolCall = (Boolean) request.getOrDefault("toolCall", false);
        
        return llmService.chat(systemPrompt, messages, tools, toolCall);
    }

    /**
     * Agent调用模型（带记忆优化）
     */
    @PostMapping("/chat-with-memory")
    public LlmService.AgentResponse chatWithMemory(@RequestBody Map<String, Object> request) {
        String systemPrompt = (String) request.getOrDefault("systemPrompt", "You are a helpful assistant");
        List<Map<String, String>> messages = (List<Map<String, String>>) request.get("messages");
        List<Map<String, Object>> tools = (List<Map<String, Object>>) request.getOrDefault("tools", List.of());
        String memorySummary = (String) request.getOrDefault("memorySummary", "");
        int maxTokens = (Integer) request.getOrDefault("maxTokens", 2048);
        
        return llmService.chatWithMemory(systemPrompt, messages, tools, memorySummary, maxTokens);
    }

    /**
     * 工具调用结果总结
     */
    @PostMapping("/summarize-tool-results")
    public String summarizeToolResults(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        List<Map<String, Object>> toolResults = (List<Map<String, Object>>) request.get("toolResults");
        String history = (String) request.getOrDefault("history", "");
        
        return llmService.summarizeToolResults(query, toolResults, history);
    }

    /**
     * 记忆摘要生成
     */
    @PostMapping("/memory-summary")
    public String generateMemorySummary(@RequestBody Map<String, Object> request) {
        List<Map<String, String>> messages = (List<Map<String, String>>) request.get("messages");
        int maxLength = (Integer) request.getOrDefault("maxLength", 500);
        
        return llmService.generateMemorySummary(messages, maxLength);
    }
}