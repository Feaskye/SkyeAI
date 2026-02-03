package com.skyeai.jarvis.recommendation.service;

import java.util.List;
import java.util.Map;

public interface PersonalizedRecommendationService {
    List<Map<String, Object>> getPersonalizedRecommendations(String userId, int limit);
    
    List<Map<String, Object>> getRecommendationsByInterest(String userId, String interest, int limit);
    
    List<Map<String, Object>> getRecommendationsByBehavior(String userId, int limit);
    
    List<Map<String, Object>> getRecommendationsByPreference(String userId, String preferenceType, int limit);
    
    List<Map<String, Object>> getPopularRecommendations(int limit);
    
    List<Map<String, Object>> getSimilarUserRecommendations(String userId, int limit);
    
    List<Map<String, Object>> getContentSimilarRecommendations(String itemId, int limit);
    
    void recordUserBehavior(String userId, String behaviorType, String itemId, Map<String, Object> metadata);
    
    List<Map<String, Object>> getUserBehaviorHistory(String userId, int limit);
    
    Map<String, Double> analyzeUserInterests(String userId);
    
    Map<String, Object> analyzeUserPreferences(String userId);
    
    double predictUserRating(String userId, String itemId);
    
    List<Map<String, Object>> getContextualRecommendations(String userId, Map<String, Object> context, int limit);
    
    List<Map<String, Object>> getMultimodalRecommendations(String userId, Map<String, Object> multimodalInfo, int limit);
    
    void refreshRecommendationModel(String userId);
    
    Map<String, Object> exportUserRecommendationData(String userId);
    
    void importUserRecommendationData(String userId, Map<String, Object> data);
    
    Map<String, Object> getRecommendationStatistics();
}