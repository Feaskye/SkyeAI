package com.skyeai.jarvis.knowledge.service.impl;

import com.skyeai.jarvis.knowledge.model.UserPreference;
import com.skyeai.jarvis.knowledge.model.ChatHistory;
import com.skyeai.jarvis.knowledge.service.LongTermMemoryService;
import com.skyeai.jarvis.protobuf.*;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LongTermMemoryServiceImpl implements LongTermMemoryService {

    @GrpcClient("jarvis-data")
    private DataServiceGrpc.DataServiceBlockingStub dataServiceStub;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public UserPreference saveUserPreference(String userId, String preferenceKey, String value, String preferenceType) {
        try {
            SaveUserPreferenceRequest request = SaveUserPreferenceRequest.newBuilder()
                    .setUserId(userId)
                    .setPreferenceKey(preferenceKey)
                    .setValue(value)
                    .setPreferenceType(preferenceType)
                    .setPriority(0)
                    .build();

            SaveUserPreferenceResponse response = dataServiceStub.saveUserPreference(request);
            return convertToUserPreference(response.getUserPreference());
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to save user preference", e);
        }
    }

    @Override
    public UserPreference getUserPreference(String userId, String preferenceKey) {
        try {
            GetUserPreferencesRequest request = GetUserPreferencesRequest.newBuilder()
                    .setUserId(userId)
                    .setPreferenceKey(preferenceKey)
                    .build();

            GetUserPreferencesResponse response = dataServiceStub.getUserPreferences(request);
            if (!response.getUserPreferencesList().isEmpty()) {
                return convertToUserPreference(response.getUserPreferencesList().get(0));
            }
            return null;
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to get user preference", e);
        }
    }

    @Override
    public List<UserPreference> getAllUserPreferences(String userId) {
        try {
            GetUserPreferencesRequest request = GetUserPreferencesRequest.newBuilder()
                    .setUserId(userId)
                    .setPreferenceKey("")
                    .build();

            GetUserPreferencesResponse response = dataServiceStub.getUserPreferences(request);
            List<UserPreference> preferences = new ArrayList<>();
            for (UserPreferenceProto proto : response.getUserPreferencesList()) {
                preferences.add(convertToUserPreference(proto));
            }
            return preferences;
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to get all user preferences", e);
        }
    }

    @Override
    public List<UserPreference> getUserPreferencesByType(String userId, String preferenceType) {
        try {
            GetUserPreferencesByTypeRequest request = GetUserPreferencesByTypeRequest.newBuilder()
                    .setUserId(userId)
                    .setPreferenceType(preferenceType)
                    .build();

            GetUserPreferencesByTypeResponse response = dataServiceStub.getUserPreferencesByType(request);
            List<UserPreference> preferences = new ArrayList<>();
            for (UserPreferenceProto proto : response.getUserPreferencesList()) {
                preferences.add(convertToUserPreference(proto));
            }
            return preferences;
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to get user preferences by type", e);
        }
    }

    @Override
    public UserPreference updateUserPreference(String userId, String preferenceKey, String value) {
        try {
            SaveUserPreferenceRequest request = SaveUserPreferenceRequest.newBuilder()
                    .setUserId(userId)
                    .setPreferenceKey(preferenceKey)
                    .setValue(value)
                    .setPreferenceType(getUserPreference(userId, preferenceKey).getPreferenceType())
                    .setPriority(0)
                    .build();

            SaveUserPreferenceResponse response = dataServiceStub.saveUserPreference(request);
            return convertToUserPreference(response.getUserPreference());
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to update user preference", e);
        }
    }

    @Override
    public void deleteUserPreference(String userId, String preferenceKey) {
        try {
            DeleteUserPreferenceRequest request = DeleteUserPreferenceRequest.newBuilder()
                    .setUserId(userId)
                    .setPreferenceKey(preferenceKey)
                    .build();

            dataServiceStub.deleteUserPreference(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to delete user preference", e);
        }
    }

    @Override
    public void saveChatHistory(String userId, String content, String role, String sessionId) {
        try {
            SaveChatHistoryRequest request = SaveChatHistoryRequest.newBuilder()
                    .setUserId(userId)
                    .setContent(content)
                    .setRole(role)
                    .setSessionId(sessionId)
                    .build();

            dataServiceStub.saveChatHistory(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to save chat history", e);
        }
    }

    @Override
    public List<ChatHistory> getChatHistory(String userId, int limit) {
        try {
            GetRecentChatHistoryRequest request = GetRecentChatHistoryRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            GetRecentChatHistoryResponse response = dataServiceStub.getRecentChatHistory(request);
            List<ChatHistory> chatHistories = new ArrayList<>();
            int count = 0;
            for (ChatHistoryProto proto : response.getChatHistoriesList()) {
                if (count >= limit) break;
                chatHistories.add(convertToChatHistory(proto));
                count++;
            }
            return chatHistories;
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to get chat history", e);
        }
    }

    @Override
    public List<ChatHistory> getSessionChatHistory(String sessionId) {
        try {
            GetChatHistoryBySessionIdRequest request = GetChatHistoryBySessionIdRequest.newBuilder()
                    .setSessionId(sessionId)
                    .build();

            GetChatHistoryBySessionIdResponse response = dataServiceStub.getChatHistoryBySessionId(request);
            List<ChatHistory> chatHistories = new ArrayList<>();
            for (ChatHistoryProto proto : response.getChatHistoriesList()) {
                chatHistories.add(convertToChatHistory(proto));
            }
            return chatHistories;
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to get session chat history", e);
        }
    }

    @Override
    public void cleanExpiredChatHistory(int days) {
        try {
            CleanExpiredChatHistoryRequest request = CleanExpiredChatHistoryRequest.newBuilder()
                    .setDays(days)
                    .build();

            dataServiceStub.cleanExpiredChatHistory(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to clean expired chat history", e);
        }
    }

    @Override
    public Map<String, Object> analyzeUserPreferences(String userId) {
        try {
            Map<String, Object> analysis = new HashMap<>();
            List<UserPreference> preferences = getAllUserPreferences(userId);
            Map<String, Integer> typeCount = new HashMap<>();
            
            for (UserPreference preference : preferences) {
                typeCount.put(preference.getPreferenceType(), typeCount.getOrDefault(preference.getPreferenceType(), 0) + 1);
            }
            
            analysis.put("totalPreferences", preferences.size());
            analysis.put("typeDistribution", typeCount);
            analysis.put("lastUpdated", new Date());
            return analysis;
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze user preferences", e);
        }
    }

    @Override
    public List<String> extractUserInterests(String userId, int limit) {
        try {
            List<String> interests = new ArrayList<>();
            List<UserPreference> preferences = getAllUserPreferences(userId);
            
            for (UserPreference preference : preferences) {
                if (interests.size() < limit) {
                    interests.add(preference.getPreferenceKey() + ": " + preference.getValue());
                }
            }
            
            return interests;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user interests", e);
        }
    }

    @Override
    public List<ChatHistory> getRecentInteractions(String userId, int hours) {
        try {
            GetRecentInteractionsRequest request = GetRecentInteractionsRequest.newBuilder()
                    .setUserId(userId)
                    .setHours(hours)
                    .build();

            GetRecentInteractionsResponse response = dataServiceStub.getRecentInteractions(request);
            List<ChatHistory> chatHistories = new ArrayList<>();
            for (ChatHistoryProto proto : response.getChatHistoriesList()) {
                chatHistories.add(convertToChatHistory(proto));
            }
            return chatHistories;
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to get recent interactions", e);
        }
    }

    @Override
    public void saveUserPreferencesBatch(String userId, Map<String, String> preferences, String preferenceType) {
        try {
            for (Map.Entry<String, String> entry : preferences.entrySet()) {
                saveUserPreference(userId, entry.getKey(), entry.getValue(), preferenceType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user preferences batch", e);
        }
    }

    @Override
    public Map<String, Object> exportUserMemory(String userId) {
        try {
            Map<String, Object> memoryData = new HashMap<>();
            memoryData.put("userPreferences", getAllUserPreferences(userId));
            memoryData.put("recentChatHistory", getChatHistory(userId, 100));
            memoryData.put("exportedAt", new Date());
            return memoryData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export user memory", e);
        }
    }

    @Override
    public void importUserMemory(String userId, Map<String, Object> memoryData) {
        try {
            if (memoryData.containsKey("userPreferences")) {
                List<UserPreference> preferences = (List<UserPreference>) memoryData.get("userPreferences");
                for (UserPreference preference : preferences) {
                    saveUserPreference(userId, preference.getPreferenceKey(), preference.getValue(), preference.getPreferenceType());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import user memory", e);
        }
    }

    @Override
    public void resetUserMemory(String userId) {
        try {
            ResetUserMemoryRequest request = ResetUserMemoryRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            dataServiceStub.resetUserMemory(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to reset user memory", e);
        }
    }

    @Override
    public Map<String, Object> getUserProfile(String userId) {
        try {
            // 获取用户偏好作为用户画像的基础数据
            List<UserPreference> preferences = getAllUserPreferences(userId);
            Map<String, Object> profile = new HashMap<>();
            
            // 分类用户偏好
            Map<String, List<UserPreference>> preferencesByType = new HashMap<>();
            for (UserPreference preference : preferences) {
                preferencesByType.computeIfAbsent(preference.getPreferenceType(), k -> new ArrayList<>())
                        .add(preference);
            }
            
            // 构建用户画像
            profile.put("userId", userId);
            profile.put("preferencesByType", preferencesByType);
            profile.put("totalPreferences", preferences.size());
            
            // 提取用户兴趣点
            profile.put("interests", extractUserInterests(userId, 10));
            
            // 获取最近的交互
            List<ChatHistory> recentInteractions = getRecentInteractions(userId, 24);
            profile.put("recentInteractionsCount", recentInteractions.size());
            
            // 分析用户偏好
            Map<String, Object> analysis = analyzeUserPreferences(userId);
            profile.putAll(analysis);
            
            profile.put("lastUpdated", new Date());
            
            return profile;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user profile", e);
        }
    }

    @Override
    public void updateUserProfile(String userId, Map<String, Object> profileData) {
        try {
            // 将用户画像数据保存为用户偏好
            for (Map.Entry<String, Object> entry : profileData.entrySet()) {
                String key = "profile_" + entry.getKey();
                String value = entry.getValue().toString();
                saveUserPreference(userId, key, value, "profile");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user profile", e);
        }
    }

    @Override
    public void updateUserProfileFromInteractions(String userId) {
        try {
            // 获取用户最近的交互
            List<ChatHistory> recentInteractions = getRecentInteractions(userId, 72);
            
            // 分析交互内容，提取关键信息
            Map<String, Integer> interestCounts = new HashMap<>();
            
            for (ChatHistory interaction : recentInteractions) {
                String content = interaction.getContent();
                // 简单的关键词提取（实际应用中可以使用更复杂的NLP方法）
                String[] words = content.split("\\s+");
                for (String word : words) {
                    word = word.toLowerCase().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
                    if (word.length() > 1) {
                        interestCounts.put(word, interestCounts.getOrDefault(word, 0) + 1);
                    }
                }
            }
            
            // 提取高频词作为兴趣点
            List<Map.Entry<String, Integer>> sortedInterests = new ArrayList<>(interestCounts.entrySet());
            sortedInterests.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            
            // 保存前10个兴趣点
            Map<String, Object> profileData = new HashMap<>();
            int count = 0;
            for (Map.Entry<String, Integer> entry : sortedInterests) {
                if (count >= 10) break;
                profileData.put("interest_" + count, entry.getKey() + ":" + entry.getValue());
                count++;
            }
            
            // 更新用户画像
            updateUserProfile(userId, profileData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user profile from interactions", e);
        }
    }

    @Override
    public UserPreference saveUserPreferenceWithPriority(String userId, String preferenceKey, String value, String preferenceType, int priority) {
        try {
            SaveUserPreferenceRequest request = SaveUserPreferenceRequest.newBuilder()
                    .setUserId(userId)
                    .setPreferenceKey(preferenceKey)
                    .setValue(value)
                    .setPreferenceType(preferenceType)
                    .setPriority(priority)
                    .build();

            SaveUserPreferenceResponse response = dataServiceStub.saveUserPreference(request);
            return convertToUserPreference(response.getUserPreference());
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to save user preference with priority", e);
        }
    }

    @Override
    public List<UserPreference> getUserPreferencesByPriority(String userId, int minPriority) {
        try {
            List<UserPreference> allPreferences = getAllUserPreferences(userId);
            // 过滤出优先级大于等于minPriority的偏好
            return allPreferences.stream()
                    .filter(preference -> preference.getPriority() >= minPriority)
                    .sorted((p1, p2) -> p2.getPriority() - p1.getPriority()) // 按优先级降序排序
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user preferences by priority", e);
        }
    }

    // Conversion methods
    private UserPreference convertToUserPreference(UserPreferenceProto proto) {
        UserPreference preference = new UserPreference();
        preference.setId(String.valueOf(proto.getId()));
        preference.setUserId(proto.getUserId());
        preference.setPreferenceKey(proto.getPreferenceKey());
        preference.setValue(proto.getValue());
        preference.setPreferenceType(proto.getPreferenceType());
        preference.setPriority(proto.getPriority());
        
        try {
            if (!proto.getCreatedAt().isEmpty()) {
                LocalDateTime createdAt = LocalDateTime.parse(proto.getCreatedAt(), DATE_TIME_FORMATTER);
                Date createdAtDate = Date.from(createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant());
                preference.setCreatedAt(createdAtDate);
            }
            if (!proto.getUpdatedAt().isEmpty()) {
                LocalDateTime updatedAt = LocalDateTime.parse(proto.getUpdatedAt(), DATE_TIME_FORMATTER);
                Date updatedAtDate = Date.from(updatedAt.atZone(java.time.ZoneId.systemDefault()).toInstant());
                preference.setUpdatedAt(updatedAtDate);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse date", e);
        }
        
        return preference;
    }

    private ChatHistory convertToChatHistory(ChatHistoryProto proto) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setId(String.valueOf(proto.getId()));
        chatHistory.setUserId(proto.getUserId());
        chatHistory.setContent(proto.getContent());
        chatHistory.setRole(proto.getRole());
        chatHistory.setSessionId(proto.getSessionId());
        
        try {
            if (!proto.getCreatedAt().isEmpty()) {
                LocalDateTime createdAt = LocalDateTime.parse(proto.getCreatedAt(), DATE_TIME_FORMATTER);
                chatHistory.setCreatedAt(createdAt);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse date", e);
        }
        
        return chatHistory;
    }
}
