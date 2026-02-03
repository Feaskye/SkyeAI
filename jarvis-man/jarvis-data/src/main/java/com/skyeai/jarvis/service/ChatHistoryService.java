package com.skyeai.jarvis.service;

import com.skyeai.jarvis.config.VectorService;
import com.skyeai.jarvis.model.ChatHistory;
import com.skyeai.jarvis.repository.ChatHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天历史服务，用于管理聊天上下文
 */
@Service
public class ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final VectorService vectorService;
    private final TextEmbeddingService textEmbeddingService;

    @Autowired
    public ChatHistoryService(ChatHistoryRepository chatHistoryRepository, 
                             VectorService vectorService, 
                             TextEmbeddingService textEmbeddingService) {
        this.chatHistoryRepository = chatHistoryRepository;
        this.vectorService = vectorService;
        this.textEmbeddingService = textEmbeddingService;
    }

    /**
     * 保存聊天历史
     * @param chatHistory 聊天历史对象
     * @return 保存后的聊天历史
     */
    public ChatHistory saveChatHistory(ChatHistory chatHistory) {
        if (chatHistory.getCreatedAt() == null) {
            chatHistory.setCreatedAt(LocalDateTime.now());
        }
        
        // 保存到数据库
        ChatHistory saved = chatHistoryRepository.save(chatHistory);
        
        // 保存到向量数据库
        saveToVectorDatabase(saved);
        
        return saved;
    }

    /**
     * 保存聊天历史到向量数据库
     */
    private void saveToVectorDatabase(ChatHistory chatHistory) {
        try {
            // 生成文本向量
            List<Double> vector = textEmbeddingService.embedText(chatHistory.getContent());
            
            // 构建payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("user_id", chatHistory.getUserId());
            payload.put("content", chatHistory.getContent());
            payload.put("role", chatHistory.getRole());
            payload.put("session_id", chatHistory.getSessionId());
            payload.put("created_at", chatHistory.getCreatedAt().toString());
            
            // 保存到向量数据库
            vectorService.addChatHistoryVector(chatHistory.getId().toString(), vector, payload);
        } catch (Exception e) {
            // 向量存储失败不影响主流程，只记录日志
            System.err.println("Failed to save chat history to vector database: " + e.getMessage());
        }
    }

    /**
     * 保存聊天历史
     * @param userId 用户ID
     * @param content 聊天内容
     * @param role 角色（user或assistant）
     * @return 保存的聊天历史
     */
    public ChatHistory saveChatHistory(String userId, String content, String role) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setContent(content);
        chatHistory.setRole(role);
        chatHistory.setCreatedAt(LocalDateTime.now());
        chatHistory.setSessionId(UUID.randomUUID().toString());
        chatHistory.setContentType("text");
        return chatHistoryRepository.save(chatHistory);
    }

    /**
     * 保存聊天历史（带会话ID）
     * @param userId 用户ID
     * @param content 聊天内容
     * @param role 角色（user或assistant）
     * @param sessionId 会话ID
     * @return 保存的聊天历史
     */
    public ChatHistory saveChatHistory(String userId, String content, String role, String sessionId) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setContent(content);
        chatHistory.setRole(role);
        chatHistory.setCreatedAt(LocalDateTime.now());
        chatHistory.setSessionId(sessionId);
        chatHistory.setContentType("text");
        return chatHistoryRepository.save(chatHistory);
    }

    /**
     * 保存多模态聊天历史
     * @param userId 用户ID
     * @param content 聊天内容
     * @param role 角色（user或assistant）
     * @param sessionId 会话ID
     * @param contentType 内容类型
     * @param mediaUrl 媒体URL
     * @param metadata 元数据
     * @return 保存的聊天历史
     */
    public ChatHistory saveMultimodalChatHistory(String userId, String content, String role, String sessionId, 
                                               String contentType, String mediaUrl, String metadata) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setContent(content);
        chatHistory.setRole(role);
        chatHistory.setCreatedAt(LocalDateTime.now());
        chatHistory.setSessionId(sessionId);
        chatHistory.setContentType(contentType);
        chatHistory.setMediaUrl(mediaUrl);
        chatHistory.setMetadata(metadata);
        return chatHistoryRepository.save(chatHistory);
    }

    /**
     * 根据用户ID获取最近的聊天历史
     * @param userId 用户ID
     * @return 聊天历史列表
     */
    public List<ChatHistory> getRecentChatHistory(String userId) {
        return chatHistoryRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 根据会话ID获取聊天历史
     * @param sessionId 会话ID
     * @return 聊天历史列表
     */
    public List<ChatHistory> getChatHistoryBySessionId(String sessionId) {
        return chatHistoryRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * 从聊天上下文中提取关键信息
     * @param userId 用户ID
     * @return 提取的关键信息
     */
    public String extractKeyInfoFromContext(String userId) {
        List<ChatHistory> chatHistoryList = getRecentChatHistory(userId);
        StringBuilder keyInfo = new StringBuilder();
        
        // 只提取最近的用户查询和AI回复（最多5条）
        int limit = Math.min(chatHistoryList.size(), 5);
        for (int i = 0; i < limit; i++) {
            ChatHistory chatHistory = chatHistoryList.get(i);
            keyInfo.append(chatHistory.getRole()).append(": ").append(chatHistory.getContent()).append("\n");
        }
        
        return keyInfo.toString();
    }

    /**
     * 根据ID删除聊天历史
     * @param id 聊天历史ID
     */
    public void deleteChatHistory(Long id) {
        chatHistoryRepository.deleteById(id);
    }

    /**
     * 清理过期的聊天历史
     * @param days 天数，删除超过指定天数的聊天历史
     * @return 删除的记录数
     */
    public int cleanExpiredChatHistory(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return chatHistoryRepository.deleteByCreatedAtBefore(cutoffDate);
    }

    /**
     * 根据用户ID删除所有聊天历史
     * @param userId 用户ID
     */
    public void deleteChatHistoryByUserId(String userId) {
        chatHistoryRepository.deleteByUserId(userId);
    }

    /**
     * 获取用户最近几小时的交互
     * @param userId 用户ID
     * @param hours 小时数
     * @return 聊天历史列表
     */
    public List<ChatHistory> getRecentInteractions(String userId, int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        return chatHistoryRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, cutoffTime);
    }
}