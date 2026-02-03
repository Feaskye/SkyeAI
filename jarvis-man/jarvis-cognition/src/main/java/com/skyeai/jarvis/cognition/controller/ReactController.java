package com.skyeai.jarvis.cognition.controller;

import com.skyeai.jarvis.cognition.service.ReactControllerAdapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * React控制器
 * 用于处理来自java-jarvis的React相关请求
 */
@RestController
@RequestMapping("/api/llm")
public class ReactController {
    
    @Autowired
    private ReactControllerAdapterService reactControllerAdapterService;
    
    /**
     * 执行ReAct决策流程
     */
    @PostMapping("/react")
    public ResponseEntity<String> executeReact(@RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            if (query == null || query.isEmpty()) {
                return ResponseEntity.badRequest().body("Query is required");
            }
            
            String result = reactControllerAdapterService.executeReact(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error executing ReAct: " + e.getMessage());
        }
    }
    
    /**
     * 获取工具调用提示
     */
    @PostMapping("/tool-prompt")
    public ResponseEntity<String> getToolCallPrompt(@RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            if (query == null || query.isEmpty()) {
                return ResponseEntity.badRequest().body("Query is required");
            }
            
            String prompt = reactControllerAdapterService.getToolCallPrompt(query);
            return ResponseEntity.ok(prompt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating tool prompt: " + e.getMessage());
        }
    }
}
