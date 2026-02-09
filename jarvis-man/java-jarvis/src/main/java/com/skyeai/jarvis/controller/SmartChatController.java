package com.skyeai.jarvis.controller;

import com.skyeai.jarvis.model.ChatRequest;
import com.skyeai.jarvis.service.chat.SmartChatService;
import com.skyeai.jarvis.service.nlp.ConversationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 智能对话控制器
 * 提供基于函数调用和实时搜索的智能对话接口
 */
@Slf4j
@RestController
@RequestMapping("/api/smart-chat")
public class SmartChatController {
    
    @Autowired
    private SmartChatService smartChatService;
    
    /**
     * 智能对话
     * @param request 对话请求
     * @return 对话响应
     */
    @PostMapping
    public Map<String, Object> chat(@RequestBody ChatRequest request) {
        log.info("Received smart chat request: {}", request.getMessage());
        
        try {
            ConversationResult result = smartChatService.chat(
                    request.getMessage(),
                    request.getSessionId(),
                    request.isUseRealtimeData(),
                    request.getParameters()
            );
            
            // 构建响应
            Map<String, Object> response = Map.of(
                    "response", result.getResponse(),
                    "sources", getSources(result),
                    "sessionId", result.getSessionId()
            );
            
            return response;
        } catch (Exception e) {
            log.error("Error processing smart chat request: {}", e.getMessage(), e);
            return Map.of(
                    "error", "Failed to process chat request",
                    "message", e.getMessage()
            );
        }
    }
    
    /**
     * 获取对话会话
     * @param userId 用户ID
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public Map<String, Object> getSessions(@RequestParam String userId) {
        log.info("Received get sessions request for user: {}", userId);
        return smartChatService.getSessions(userId);
    }
    
    /**
     * 获取会话详情
     * @param id 会话ID
     * @return 会话详情
     */
    @GetMapping("/sessions/{id}")
    public Map<String, Object> getSessionDetail(@PathVariable String id) {
        log.info("Received get session detail request: {}", id);
        return smartChatService.getSessionDetail(id);
    }
    
    /**
     * 结束会话
     * @param id 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/sessions/{id}")
    public Map<String, Object> endSession(@PathVariable String id) {
        log.info("Received end session request: {}", id);
        return smartChatService.endSession(id);
    }
    
    /**
     * 获取对话来源
     * @param result 对话结果
     * @return 来源列表
     */
    private Map<String, Object> getSources(ConversationResult result) {
        // 这里应该从对话结果中提取来源信息
        // 暂时返回空对象
        return Map.of();
    }
    
    /**
     * 对话请求模型
     */
    public static class ChatRequest {
        private String message;
        private boolean useRealtimeData;
        private String sessionId;
        private Map<String, Object> parameters;
        
        // Getters and Setters
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public boolean isUseRealtimeData() {
            return useRealtimeData;
        }
        
        public void setUseRealtimeData(boolean useRealtimeData) {
            this.useRealtimeData = useRealtimeData;
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
        
        public Map<String, Object> getParameters() {
            return parameters;
        }
        
        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }
}