package com.skyeai.jarvis.user.service.impl;

import com.skyeai.jarvis.user.model.ChatHistory;
import com.skyeai.jarvis.user.repository.ChatHistoryRepository;
import com.skyeai.jarvis.user.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Override
    public ChatHistory createChatHistory(ChatHistory chatHistory) {
        return chatHistoryRepository.save(chatHistory);
    }

    @Override
    public ChatHistory getChatHistoryById(Long id) {
        return chatHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat history not found with id: " + id));
    }

    @Override
    public List<ChatHistory> getChatHistoriesByUserId(Long userId) {
        return chatHistoryRepository.findByUserId(userId);
    }

    @Override
    public List<ChatHistory> getChatHistoriesBySessionId(String sessionId) {
        return chatHistoryRepository.findBySessionId(sessionId);
    }

    @Override
    public List<ChatHistory> getChatHistoriesByUserIdAndSessionId(Long userId, String sessionId) {
        return chatHistoryRepository.findByUserIdAndSessionId(userId, sessionId);
    }

    @Override
    public List<ChatHistory> getRecentChatHistoriesByUserId(Long userId, int limit) {
        return chatHistoryRepository.findRecentByUserId(userId, limit);
    }

    @Override
    public List<ChatHistory> getChatHistoriesByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return chatHistoryRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }

    @Override
    public List<ChatHistory> getChatHistoriesByUserIdAndModelUsed(Long userId, String modelUsed) {
        return chatHistoryRepository.findByUserIdAndModelUsed(userId, modelUsed);
    }

    @Override
    public List<ChatHistory> getChatHistoriesByUserIdAndPromptType(Long userId, String promptType) {
        return chatHistoryRepository.findByUserIdAndPromptType(userId, promptType);
    }

    @Override
    public ChatHistory updateChatHistory(Long id, ChatHistory chatHistory) {
        ChatHistory existingChatHistory = chatHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat history not found with id: " + id));

        // 更新聊天历史记录
        if (chatHistory.getUserMessage() != null) {
            existingChatHistory.setUserMessage(chatHistory.getUserMessage());
        }
        if (chatHistory.getAssistantMessage() != null) {
            existingChatHistory.setAssistantMessage(chatHistory.getAssistantMessage());
        }
        if (chatHistory.getModelUsed() != null) {
            existingChatHistory.setModelUsed(chatHistory.getModelUsed());
        }
        if (chatHistory.getPromptType() != null) {
            existingChatHistory.setPromptType(chatHistory.getPromptType());
        }
        if (chatHistory.getResponseTimeMs() != null) {
            existingChatHistory.setResponseTimeMs(chatHistory.getResponseTimeMs());
        }
        if (chatHistory.getTimestamp() != null) {
            existingChatHistory.setTimestamp(chatHistory.getTimestamp());
        }

        return chatHistoryRepository.save(existingChatHistory);
    }

    @Override
    public void deleteChatHistory(Long id) {
        chatHistoryRepository.deleteById(id);
    }

    @Override
    public void deleteChatHistoriesByUserId(Long userId) {
        chatHistoryRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteChatHistoriesBySessionId(String sessionId) {
        chatHistoryRepository.deleteBySessionId(sessionId);
    }

    @Override
    public long getChatHistoryCountByUserId(Long userId) {
        return chatHistoryRepository.countByUserId(userId);
    }
}
