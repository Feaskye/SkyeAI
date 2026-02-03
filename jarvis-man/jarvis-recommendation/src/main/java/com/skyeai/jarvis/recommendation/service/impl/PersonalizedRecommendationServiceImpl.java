package com.skyeai.jarvis.recommendation.service.impl;

import com.skyeai.jarvis.recommendation.service.PersonalizedRecommendationService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PersonalizedRecommendationServiceImpl implements PersonalizedRecommendationService {

    private final Map<String, List<Map<String, Object>>> userBehaviors = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> items = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Double>> userInterests = new ConcurrentHashMap<>();

    public PersonalizedRecommendationServiceImpl() {
        initSampleItems();
    }

    private void initSampleItems() {
        items.put("stock_1", createItem("stock_1", "股票分析工具", "金融", "提供实时股票分析和预测"));
        items.put("stock_2", createItem("stock_2", "大盘走势分析", "金融", "分析今日大盘走势和热点板块"));
        items.put("stock_3", createItem("stock_3", "个股推荐", "金融", "基于技术分析推荐潜力个股"));

        items.put("health_1", createItem("health_1", "健康数据监测", "健康", "监测心率、睡眠等健康数据"));
        items.put("health_2", createItem("health_2", "健康建议", "健康", "基于健康数据提供个性化建议"));
        items.put("health_3", createItem("health_3", "运动计划", "健康", "制定个性化运动计划"));

        items.put("iot_1", createItem("iot_1", "智能灯光控制", "IoT", "远程控制智能灯光"));
        items.put("iot_2", createItem("iot_2", "智能空调控制", "IoT", "远程控制智能空调"));
        items.put("iot_3", createItem("iot_3", "智能家居场景", "IoT", "创建和管理智能家居场景"));

        items.put("code_1", createItem("code_1", "代码生成", "编程", "基于需求生成代码"));
        items.put("code_2", createItem("code_2", "代码审查", "编程", "分析和优化代码"));
        items.put("code_3", createItem("code_3", "技术文档", "编程", "生成技术文档"));
    }

    private Map<String, Object> createItem(String id, String name, String category, String description) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("category", category);
        item.put("description", description);
        item.put("score", Math.random() * 5);
        return item;
    }

    @Override
    public List<Map<String, Object>> getPersonalizedRecommendations(String userId, int limit) {
        Map<String, Double> interests = analyzeUserInterests(userId);
        List<Map<String, Object>> recommendations = new ArrayList<>();

        List<Map<String, Object>> sortedItems = new ArrayList<>(items.values());
        sortedItems.sort((a, b) -> {
            String categoryA = (String) a.get("category");
            String categoryB = (String) b.get("category");
            double scoreA = interests.getOrDefault(categoryA, 0.0);
            double scoreB = interests.getOrDefault(categoryB, 0.0);
            return Double.compare(scoreB, scoreA);
        });

        for (int i = 0; i < Math.min(limit, sortedItems.size()); i++) {
            recommendations.add(sortedItems.get(i));
        }

        return recommendations;
    }

    @Override
    public List<Map<String, Object>> getRecommendationsByInterest(String userId, String interest, int limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (Map<String, Object> item : items.values()) {
            String category = (String) item.get("category");
            if (category.equalsIgnoreCase(interest)) {
                recommendations.add(item);
            }
        }

        recommendations.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));

        return recommendations.subList(0, Math.min(limit, recommendations.size()));
    }

    @Override
    public List<Map<String, Object>> getRecommendationsByBehavior(String userId, int limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        List<Map<String, Object>> behaviors = userBehaviors.getOrDefault(userId, new ArrayList<>());

        if (!behaviors.isEmpty()) {
            Map<String, Object> lastBehavior = behaviors.get(behaviors.size() - 1);
            String itemId = (String) lastBehavior.get("itemId");
            Map<String, Object> item = items.get(itemId);
            if (item != null) {
                String category = (String) item.get("category");
                for (Map<String, Object> candidate : items.values()) {
                    if (category.equals(candidate.get("category")) && !itemId.equals(candidate.get("id"))) {
                        recommendations.add(candidate);
                    }
                }
            }
        }

        if (recommendations.size() < limit) {
            List<Map<String, Object>> popularItems = getPopularRecommendations(limit - recommendations.size());
            recommendations.addAll(popularItems);
        }

        return recommendations.subList(0, Math.min(limit, recommendations.size()));
    }

    @Override
    public List<Map<String, Object>> getRecommendationsByPreference(String userId, String preferenceType, int limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (Map<String, Object> item : items.values()) {
            if (item.get("category").equals(preferenceType)) {
                recommendations.add(item);
            }
        }

        recommendations.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));

        return recommendations.subList(0, Math.min(limit, recommendations.size()));
    }

    @Override
    public List<Map<String, Object>> getPopularRecommendations(int limit) {
        List<Map<String, Object>> popularItems = new ArrayList<>(items.values());

        popularItems.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));

        return popularItems.subList(0, Math.min(limit, popularItems.size()));
    }

    @Override
    public List<Map<String, Object>> getSimilarUserRecommendations(String userId, int limit) {
        return getPopularRecommendations(limit);
    }

    @Override
    public List<Map<String, Object>> getContentSimilarRecommendations(String itemId, int limit) {
        List<Map<String, Object>> similarItems = new ArrayList<>();
        Map<String, Object> targetItem = items.get(itemId);

        if (targetItem != null) {
            String category = (String) targetItem.get("category");
            for (Map<String, Object> item : items.values()) {
                if (category.equals(item.get("category")) && !itemId.equals(item.get("id"))) {
                    similarItems.add(item);
                }
            }
        }

        similarItems.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));

        return similarItems.subList(0, Math.min(limit, similarItems.size()));
    }

    @Override
    public void recordUserBehavior(String userId, String behaviorType, String itemId, Map<String, Object> metadata) {
        List<Map<String, Object>> behaviors = userBehaviors.computeIfAbsent(userId, k -> new ArrayList<>());
        Map<String, Object> behavior = new HashMap<>();
        behavior.put("timestamp", System.currentTimeMillis());
        behavior.put("behaviorType", behaviorType);
        behavior.put("itemId", itemId);
        behavior.put("metadata", metadata);
        behaviors.add(behavior);

        updateUserInterests(userId, itemId, behaviorType);
    }

    @Override
    public List<Map<String, Object>> getUserBehaviorHistory(String userId, int limit) {
        List<Map<String, Object>> behaviors = userBehaviors.getOrDefault(userId, new ArrayList<>());
        int startIndex = Math.max(0, behaviors.size() - limit);
        return behaviors.subList(startIndex, behaviors.size());
    }

    @Override
    public Map<String, Double> analyzeUserInterests(String userId) {
        Map<String, Double> interests = userInterests.computeIfAbsent(userId, k -> new HashMap<>());

        if (interests.isEmpty()) {
            interests.put("金融", 0.5);
            interests.put("健康", 0.5);
            interests.put("IoT", 0.5);
            interests.put("编程", 0.5);
        }

        return interests;
    }

    @Override
    public Map<String, Object> analyzeUserPreferences(String userId) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("interests", analyzeUserInterests(userId));
        analysis.put("behaviors", getUserBehaviorHistory(userId, 10));
        analysis.put("recommendations", getPersonalizedRecommendations(userId, 5));
        return analysis;
    }

    @Override
    public double predictUserRating(String userId, String itemId) {
        Map<String, Object> item = items.get(itemId);
        if (item == null) {
            return 3.0;
        }

        String category = (String) item.get("category");
        Map<String, Double> interests = analyzeUserInterests(userId);
        double interestScore = interests.getOrDefault(category, 0.5);

        return interestScore * 5;
    }

    @Override
    public List<Map<String, Object>> getContextualRecommendations(String userId, Map<String, Object> context, int limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>(items.values());

        recommendations.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));

        return recommendations.subList(0, Math.min(limit, recommendations.size()));
    }

    @Override
    public List<Map<String, Object>> getMultimodalRecommendations(String userId, Map<String, Object> multimodalInfo, int limit) {
        return getPersonalizedRecommendations(userId, limit);
    }

    @Override
    public void refreshRecommendationModel(String userId) {
        Map<String, Double> interests = analyzeUserInterests(userId);
        System.out.println("刷新推荐模型 for user " + userId + ": " + interests);
    }

    @Override
    public Map<String, Object> exportUserRecommendationData(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("interests", analyzeUserInterests(userId));
        data.put("behaviors", getUserBehaviorHistory(userId, 100));
        data.put("preferences", analyzeUserPreferences(userId));
        return data;
    }

    @Override
    public void importUserRecommendationData(String userId, Map<String, Object> data) {
        System.out.println("导入推荐数据 for user " + userId + ": " + data.size() + " 个元素");
    }

    @Override
    public Map<String, Object> getRecommendationStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalItems", items.size());
        statistics.put("totalUsers", userBehaviors.size());
        statistics.put("totalBehaviors", userBehaviors.values().stream().mapToInt(List::size).sum());
        return statistics;
    }

    private void updateUserInterests(String userId, String itemId, String behaviorType) {
        Map<String, Object> item = items.get(itemId);
        if (item == null) {
            return;
        }

        String category = (String) item.get("category");
        Map<String, Double> interests = userInterests.computeIfAbsent(userId, k -> new HashMap<>());

        double weight = getBehaviorWeight(behaviorType);
        double currentScore = interests.getOrDefault(category, 0.5);
        double newScore = Math.min(1.0, currentScore + weight * 0.1);
        interests.put(category, newScore);
    }

    private double getBehaviorWeight(String behaviorType) {
        switch (behaviorType) {
            case "click":
                return 0.5;
            case "view":
                return 0.3;
            case "like":
                return 1.0;
            case "dislike":
                return -0.8;
            default:
                return 0.1;
        }
    }
}