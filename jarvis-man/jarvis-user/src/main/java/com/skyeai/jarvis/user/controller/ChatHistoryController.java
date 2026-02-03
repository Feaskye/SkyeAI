package com.skyeai.jarvis.user.controller;

import com.skyeai.jarvis.user.model.ChatHistory;
import com.skyeai.jarvis.user.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat-histories")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    /**
     * 创建聊天历史记录
     */
    @PostMapping
    public ChatHistory createChatHistory(@RequestBody ChatHistory chatHistory) {
        return chatHistoryService.createChatHistory(chatHistory);
    }

    /**
     * 根据ID获取聊天历史记录
     */
    @GetMapping("/{id}")
    public ChatHistory getChatHistoryById(@PathVariable Long id) {
        return chatHistoryService.getChatHistoryById(id);
    }

    /**
     * 根据用户ID获取聊天历史记录
     */
    @GetMapping("/user/{userId}")
    public List<ChatHistory> getChatHistoriesByUserId(@PathVariable Long userId) {
        return chatHistoryService.getChatHistoriesByUserId(userId);
    }

    /**
     * 根据会话ID获取聊天历史记录
     */
    @GetMapping("/session/{sessionId}")
    public List<ChatHistory> getChatHistoriesBySessionId(@PathVariable String sessionId) {
        return chatHistoryService.getChatHistoriesBySessionId(sessionId);
    }

    /**
     * 根据用户ID和会话ID获取聊天历史记录
     */
    @GetMapping("/user/{userId}/session/{sessionId}")
    public List<ChatHistory> getChatHistoriesByUserIdAndSessionId(@PathVariable Long userId, @PathVariable String sessionId) {
        return chatHistoryService.getChatHistoriesByUserIdAndSessionId(userId, sessionId);
    }

    /**
     * 获取用户的最近聊天历史记录
     */
    @GetMapping("/user/{userId}/recent")
    public List<ChatHistory> getRecentChatHistoriesByUserId(@PathVariable Long userId, @RequestParam(defaultValue = "50") int limit) {
        return chatHistoryService.getRecentChatHistoriesByUserId(userId, limit);
    }

    /**
     * 根据时间范围获取用户的聊天历史记录
     */
    @GetMapping("/user/{userId}/time-range")
    public List<ChatHistory> getChatHistoriesByUserIdAndTimeRange(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        return chatHistoryService.getChatHistoriesByUserIdAndTimeRange(userId, startTime, endTime);
    }

    /**
     * 根据模型类型获取用户的聊天历史记录
     */
    @GetMapping("/user/{userId}/model/{modelUsed}")
    public List<ChatHistory> getChatHistoriesByUserIdAndModelUsed(@PathVariable Long userId, @PathVariable String modelUsed) {
        return chatHistoryService.getChatHistoriesByUserIdAndModelUsed(userId, modelUsed);
    }

    /**
     * 根据提示类型获取用户的聊天历史记录
     */
    @GetMapping("/user/{userId}/prompt/{promptType}")
    public List<ChatHistory> getChatHistoriesByUserIdAndPromptType(@PathVariable Long userId, @PathVariable String promptType) {
        return chatHistoryService.getChatHistoriesByUserIdAndPromptType(userId, promptType);
    }

    /**
     * 更新聊天历史记录
     */
    @PutMapping("/{id}")
    public ChatHistory updateChatHistory(@PathVariable Long id, @RequestBody ChatHistory chatHistory) {
        return chatHistoryService.updateChatHistory(id, chatHistory);
    }

    /**
     * 删除聊天历史记录
     */
    @DeleteMapping("/{id}")
    public Map<String, String> deleteChatHistory(@PathVariable Long id) {
        chatHistoryService.deleteChatHistory(id);
        return Map.of("message", "Chat history deleted successfully");
    }

    /**
     * 删除用户的所有聊天历史记录
     */
    @DeleteMapping("/user/{userId}")
    public Map<String, String> deleteChatHistoriesByUserId(@PathVariable Long userId) {
        chatHistoryService.deleteChatHistoriesByUserId(userId);
        return Map.of("message", "All chat histories deleted successfully for user: " + userId);
    }

    /**
     * 删除指定会话的所有聊天历史记录
     */
    @DeleteMapping("/session/{sessionId}")
    public Map<String, String> deleteChatHistoriesBySessionId(@PathVariable String sessionId) {
        chatHistoryService.deleteChatHistoriesBySessionId(sessionId);
        return Map.of("message", "All chat histories deleted successfully for session: " + sessionId);
    }

    /**
     * 获取用户的聊天历史记录总数
     */
    @GetMapping("/user/{userId}/count")
    public Map<String, Long> getChatHistoryCountByUserId(@PathVariable Long userId) {
        long count = chatHistoryService.getChatHistoryCountByUserId(userId);
        return Map.of("count", count);
    }
}
