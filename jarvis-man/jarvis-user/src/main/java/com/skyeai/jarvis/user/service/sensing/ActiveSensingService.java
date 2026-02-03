package com.skyeai.jarvis.user.service.sensing;

import java.util.Map;

public interface ActiveSensingService {
    void initialize();
    void shutdown();
    
    void startSensing();
    void stopSensing();
    
    Map<String, Object> getSensingStatus();
    Map<String, Object> getSensingData();
    
    void registerEventListener(EventListener listener);
    void unregisterEventListener(EventListener listener);
    
    Map<String, Object> configureSensing(Map<String, Object> configuration);
    
    // 智能预测相关方法
    Map<String, Object> predictUserNeeds();
    Map<String, Object> analyzeUserBehavior();
    Map<String, Object> generateRecommendations();
    
    // 自主决策相关方法
    Map<String, Object> makeAutonomousDecision(Map<String, Object> context);
    Map<String, Object> getDecisionHistory();
}