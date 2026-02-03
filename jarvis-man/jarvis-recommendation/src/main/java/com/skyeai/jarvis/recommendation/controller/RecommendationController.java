package com.skyeai.jarvis.recommendation.controller;

import com.skyeai.jarvis.recommendation.service.PersonalizedRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController {

    @Autowired
    private PersonalizedRecommendationService recommendationService;

    @GetMapping("/personalized")
    public List<Map<String, Object>> getPersonalizedRecommendations(
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") int limit) {
        return recommendationService.getPersonalizedRecommendations(userId, limit);
    }

    @GetMapping("/by-interest")
    public List<Map<String, Object>> getRecommendationsByInterest(
            @RequestParam String userId,
            @RequestParam String interest,
            @RequestParam(defaultValue = "5") int limit) {
        return recommendationService.getRecommendationsByInterest(userId, interest, limit);
    }

    @GetMapping("/by-behavior")
    public List<Map<String, Object>> getRecommendationsByBehavior(
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") int limit) {
        return recommendationService.getRecommendationsByBehavior(userId, limit);
    }

    @GetMapping("/by-preference")
    public List<Map<String, Object>> getRecommendationsByPreference(
            @RequestParam String userId,
            @RequestParam String preferenceType,
            @RequestParam(defaultValue = "5") int limit) {
        return recommendationService.getRecommendationsByPreference(userId, preferenceType, limit);
    }

    @GetMapping("/popular")
    public List<Map<String, Object>> getPopularRecommendations(
            @RequestParam(defaultValue = "5") int limit) {
        return recommendationService.getPopularRecommendations(limit);
    }

    @GetMapping("/similar-user")
    public List<Map<String, Object>> getSimilarUserRecommendations(
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") int limit) {
        return recommendationService.getSimilarUserRecommendations(userId, limit);
    }

    @GetMapping("/similar-content")
    public List<Map<String, Object>> getContentSimilarRecommendations(
            @RequestParam String itemId,
            @RequestParam(defaultValue = "5") int limit) {
        return recommendationService.getContentSimilarRecommendations(itemId, limit);
    }

    @PostMapping("/behavior")
    public void recordUserBehavior(
            @RequestParam String userId,
            @RequestParam String behaviorType,
            @RequestParam String itemId,
            @RequestBody Map<String, Object> metadata) {
        recommendationService.recordUserBehavior(userId, behaviorType, itemId, metadata);
    }

    @GetMapping("/behavior-history")
    public List<Map<String, Object>> getUserBehaviorHistory(
            @RequestParam String userId,
            @RequestParam(defaultValue = "10") int limit) {
        return recommendationService.getUserBehaviorHistory(userId, limit);
    }

    @GetMapping("/interests")
    public Map<String, Double> analyzeUserInterests(@RequestParam String userId) {
        return recommendationService.analyzeUserInterests(userId);
    }

    @GetMapping("/preferences")
    public Map<String, Object> analyzeUserPreferences(@RequestParam String userId) {
        return recommendationService.analyzeUserPreferences(userId);
    }

    @GetMapping("/predict-rating")
    public double predictUserRating(
            @RequestParam String userId,
            @RequestParam String itemId) {
        return recommendationService.predictUserRating(userId, itemId);
    }

    @PostMapping("/contextual")
    public List<Map<String, Object>> getContextualRecommendations(
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") int limit,
            @RequestBody Map<String, Object> context) {
        return recommendationService.getContextualRecommendations(userId, context, limit);
    }

    @PostMapping("/multimodal")
    public List<Map<String, Object>> getMultimodalRecommendations(
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") int limit,
            @RequestBody Map<String, Object> multimodalInfo) {
        return recommendationService.getMultimodalRecommendations(userId, multimodalInfo, limit);
    }

    @PostMapping("/refresh-model")
    public void refreshRecommendationModel(@RequestParam String userId) {
        recommendationService.refreshRecommendationModel(userId);
    }

    @GetMapping("/export-data")
    public Map<String, Object> exportUserRecommendationData(@RequestParam String userId) {
        return recommendationService.exportUserRecommendationData(userId);
    }

    @PostMapping("/import-data")
    public void importUserRecommendationData(
            @RequestParam String userId,
            @RequestBody Map<String, Object> data) {
        recommendationService.importUserRecommendationData(userId, data);
    }

    @GetMapping("/statistics")
    public Map<String, Object> getRecommendationStatistics() {
        return recommendationService.getRecommendationStatistics();
    }
}