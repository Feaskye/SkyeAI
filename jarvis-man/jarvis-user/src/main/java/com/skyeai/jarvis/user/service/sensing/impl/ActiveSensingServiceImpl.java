package com.skyeai.jarvis.user.service.sensing.impl;

import com.skyeai.jarvis.user.service.sensing.ActiveSensingService;
import com.skyeai.jarvis.user.service.sensing.EventListener;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ActiveSensingServiceImpl implements ActiveSensingService {
    private ScheduledExecutorService executorService;
    private boolean isSensing;
    private List<EventListener> eventListeners;
    private Map<String, Object> sensingData;
    private Map<String, Object> configuration;
    private List<Map<String, Object>> decisionHistory;
    private Map<String, Object> userBehaviorData;
    
    @PostConstruct
    public void initialize() {
        executorService = Executors.newScheduledThreadPool(5);
        eventListeners = new ArrayList<>();
        sensingData = new HashMap<>();
        configuration = new HashMap<>();
        decisionHistory = new ArrayList<>();
        userBehaviorData = new HashMap<>();
        
        // 默认配置
        configuration.put("sensingInterval", 5000); // 5秒
        configuration.put("enabledSensors", List.of("cpu", "memory", "disk", "network"));
        configuration.put("thresholds", Map.of(
            "cpu", 80.0,
            "memory", 85.0,
            "disk", 90.0
        ));
        
        // 初始化用户行为数据
        initializeUserBehaviorData();
    }
    
    /**
     * 初始化用户行为数据
     */
    private void initializeUserBehaviorData() {
        userBehaviorData.put("usage_patterns", Map.of(
            "morning", List.of("check_weather", "read_news", "plan_day"),
            "afternoon", List.of("work", "check_emails", "meetings"),
            "evening", List.of("relax", "watch_tv", "plan_tomorrow")
        ));
        userBehaviorData.put("preferences", Map.of(
            "temperature", 22.0,
            "lighting", "warm",
            "music", "classical",
            "news_sources", List.of("tech", "business", "sports")
        ));
        userBehaviorData.put("frequent_tasks", List.of(
            Map.of("task", "check_weather", "frequency", 0.9),
            Map.of("task", "read_news", "frequency", 0.8),
            Map.of("task", "play_music", "frequency", 0.7),
            Map.of("task", "set_reminder", "frequency", 0.6)
        ));
    }
    
    @PreDestroy
    public void shutdown() {
        stopSensing();
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }
    
    @Override
    public void startSensing() {
        if (isSensing) {
            return;
        }
        
        isSensing = true;
        int interval = (int) configuration.getOrDefault("sensingInterval", 5000);
        
        // 启动传感器数据收集任务
        executorService.scheduleAtFixedRate(this::collectSensingData, 0, interval, TimeUnit.MILLISECONDS);
        
        // 发送启动事件
        Map<String, Object> eventData = Map.of("status", "started", "interval", interval);
        notifyEventListeners(new EventListener.Event("sensing_started", eventData));
    }
    
    @Override
    public void stopSensing() {
        if (!isSensing) {
            return;
        }
        
        isSensing = false;
        
        // 发送停止事件
        Map<String, Object> eventData = Map.of("status", "stopped");
        notifyEventListeners(new EventListener.Event("sensing_stopped", eventData));
    }
    
    @Override
    public Map<String, Object> getSensingStatus() {
        return Map.of(
            "isSensing", isSensing,
            "configuration", configuration,
            "lastSensingData", sensingData
        );
    }
    
    @Override
    public Map<String, Object> getSensingData() {
        return new HashMap<>(sensingData);
    }
    
    @Override
    public void registerEventListener(EventListener listener) {
        eventListeners.add(listener);
    }
    
    @Override
    public void unregisterEventListener(EventListener listener) {
        eventListeners.remove(listener);
    }
    
    @Override
    public Map<String, Object> configureSensing(Map<String, Object> configuration) {
        this.configuration.putAll(configuration);
        
        // 发送配置更新事件
        Map<String, Object> eventData = Map.of("configuration", this.configuration);
        notifyEventListeners(new EventListener.Event("sensing_configured", eventData));
        
        return Map.of("success", true, "message", "配置更新成功", "configuration", this.configuration);
    }
    
    private void collectSensingData() {
        if (!isSensing) {
            return;
        }
        
        List<String> enabledSensors = (List<String>) configuration.getOrDefault("enabledSensors", List.of());
        Map<String, Object> newSensingData = new HashMap<>();
        
        // 模拟收集传感器数据
        if (enabledSensors.contains("cpu")) {
            double cpuUsage = Math.random() * 100;
            newSensingData.put("cpu_usage", cpuUsage);
            
            // 检查CPU阈值
            Map<String, Object> thresholds = (Map<String, Object>) configuration.getOrDefault("thresholds", Map.of());
            Object cpuThresholdObj = thresholds.getOrDefault("cpu", 80.0);
            double cpuThreshold = cpuThresholdObj instanceof Number ? ((Number) cpuThresholdObj).doubleValue() : 80.0;
            if (cpuUsage > cpuThreshold) {
                Map<String, Object> eventData = Map.of("cpu_usage", cpuUsage, "threshold", cpuThreshold);
                notifyEventListeners(new EventListener.Event("cpu_threshold_exceeded", eventData));
            }
        }
        
        if (enabledSensors.contains("memory")) {
            double memoryUsage = Math.random() * 100;
            newSensingData.put("memory_usage", memoryUsage);
            
            // 检查内存阈值
            Map<String, Object> thresholds = (Map<String, Object>) configuration.getOrDefault("thresholds", Map.of());
            Object memoryThresholdObj = thresholds.getOrDefault("memory", 85.0);
            double memoryThreshold = memoryThresholdObj instanceof Number ? ((Number) memoryThresholdObj).doubleValue() : 85.0;
            if (memoryUsage > memoryThreshold) {
                Map<String, Object> eventData = Map.of("memory_usage", memoryUsage, "threshold", memoryThreshold);
                notifyEventListeners(new EventListener.Event("memory_threshold_exceeded", eventData));
            }
        }
        
        if (enabledSensors.contains("disk")) {
            double diskUsage = Math.random() * 100;
            newSensingData.put("disk_usage", diskUsage);
            
            // 检查磁盘阈值
            Map<String, Object> thresholds = (Map<String, Object>) configuration.getOrDefault("thresholds", Map.of());
            Object diskThresholdObj = thresholds.getOrDefault("disk", 90.0);
            double diskThreshold = diskThresholdObj instanceof Number ? ((Number) diskThresholdObj).doubleValue() : 90.0;
            if (diskUsage > diskThreshold) {
                Map<String, Object> eventData = Map.of("disk_usage", diskUsage, "threshold", diskThreshold);
                notifyEventListeners(new EventListener.Event("disk_threshold_exceeded", eventData));
            }
        }
        
        if (enabledSensors.contains("network")) {
            double networkUsage = Math.random() * 100;
            newSensingData.put("network_usage", networkUsage);
        }
        
        // 添加时间戳
        newSensingData.put("timestamp", System.currentTimeMillis());
        
        // 更新传感数据
        sensingData.putAll(newSensingData);
        
        // 发送数据更新事件
        notifyEventListeners(new EventListener.Event("sensing_data_updated", newSensingData));
    }
    
    private void notifyEventListeners(EventListener.Event event) {
        for (EventListener listener : eventListeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> predictUserNeeds() {
        // 模拟预测用户需求
        Map<String, Object> predictions = new HashMap<>();
        
        // 根据时间预测
        String timeOfDay = getTimeOfDay();
        List<String> predictedNeeds = new ArrayList<>();
        
        Map<String, List<String>> usagePatterns = (Map<String, List<String>>) userBehaviorData.getOrDefault("usage_patterns", Map.of());
        if (usagePatterns.containsKey(timeOfDay)) {
            predictedNeeds.addAll(usagePatterns.get(timeOfDay));
        }
        
        // 随机添加一些额外的预测需求
        List<String> possibleNeeds = List.of("check_calendar", "set_reminder", "play_music", "control_home", "search_internet");
        for (int i = 0; i < 2; i++) {
            int randomIndex = (int) (Math.random() * possibleNeeds.size());
            String need = possibleNeeds.get(randomIndex);
            if (!predictedNeeds.contains(need)) {
                predictedNeeds.add(need);
            }
        }
        
        predictions.put("time_of_day", timeOfDay);
        predictions.put("predicted_needs", predictedNeeds);
        predictions.put("confidence", Math.random() * 0.3 + 0.7); // 70-100% 置信度
        predictions.put("timestamp", System.currentTimeMillis());
        
        return predictions;
    }

    @Override
    public Map<String, Object> analyzeUserBehavior() {
        // 模拟分析用户行为
        Map<String, Object> behaviorAnalysis = new HashMap<>();
        
        behaviorAnalysis.put("usage_patterns", userBehaviorData.get("usage_patterns"));
        behaviorAnalysis.put("preferences", userBehaviorData.get("preferences"));
        behaviorAnalysis.put("frequent_tasks", userBehaviorData.get("frequent_tasks"));
        
        // 添加行为趋势分析
        Map<String, Object> trends = new HashMap<>();
        trends.put("morning_usage_increasing", Math.random() > 0.3);
        trends.put("evening_relaxation_time", Math.random() * 60 + 30); // 30-90分钟
        trends.put("preferred_communication_channel", List.of("voice", "text", "app"));
        
        behaviorAnalysis.put("trends", trends);
        behaviorAnalysis.put("analysis_timestamp", System.currentTimeMillis());
        
        return behaviorAnalysis;
    }

    @Override
    public Map<String, Object> generateRecommendations() {
        // 模拟生成推荐
        Map<String, Object> recommendations = new HashMap<>();
        List<Map<String, Object>> recList = new ArrayList<>();
        
        // 根据时间生成推荐
        String timeOfDay = getTimeOfDay();
        
        if (timeOfDay.equals("morning")) {
            recList.add(Map.of(
                "type", "weather",
                "title", "今日天气",
                "description", "查看今天的天气情况",
                "priority", "high"
            ));
            recList.add(Map.of(
                "type", "news",
                "title", "早间新闻",
                "description", "浏览最新的新闻摘要",
                "priority", "medium"
            ));
        } else if (timeOfDay.equals("afternoon")) {
            recList.add(Map.of(
                "type", "productivity",
                "title", "工作效率",
                "description", "打开工作应用",
                "priority", "high"
            ));
            recList.add(Map.of(
                "type", "break",
                "title", "休息提醒",
                "description", "该休息一下了",
                "priority", "medium"
            ));
        } else if (timeOfDay.equals("evening")) {
            recList.add(Map.of(
                "type", "entertainment",
                "title", "休闲娱乐",
                "description", "播放您喜欢的音乐",
                "priority", "medium"
            ));
            recList.add(Map.of(
                "type", "planning",
                "title", "明日计划",
                "description", "规划明天的任务",
                "priority", "low"
            ));
        }
        
        recommendations.put("time_of_day", timeOfDay);
        recommendations.put("recommendations", recList);
        recommendations.put("generated_at", System.currentTimeMillis());
        
        return recommendations;
    }

    @Override
    public Map<String, Object> makeAutonomousDecision(Map<String, Object> context) {
        // 模拟自主决策
        Map<String, Object> decision = new HashMap<>();
        
        // 根据上下文生成决策
        String decisionType = "routine";
        String action = "none";
        double confidence = 0.8;
        
        // 基于时间的决策
        String timeOfDay = getTimeOfDay();
        if (timeOfDay.equals("morning")) {
            action = "check_weather_and_news";
            decisionType = "morning_routine";
        } else if (timeOfDay.equals("afternoon")) {
            action = "optimize_work_environment";
            decisionType = "productivity";
        } else if (timeOfDay.equals("evening")) {
            action = "prepare_relaxation";
            decisionType = "evening_routine";
        }
        
        // 基于传感器数据的决策
        if (sensingData.containsKey("cpu_usage")) {
            double cpuUsage = (double) sensingData.get("cpu_usage");
            if (cpuUsage > 80) {
                action = "optimize_system";
                decisionType = "system_maintenance";
                confidence = 0.95;
            }
        }
        
        decision.put("decision_type", decisionType);
        decision.put("action", action);
        decision.put("confidence", confidence);
        decision.put("context", context);
        decision.put("timestamp", System.currentTimeMillis());
        
        // 记录决策历史
        decisionHistory.add(decision);
        if (decisionHistory.size() > 100) {
            decisionHistory.remove(0); // 保持历史记录在100条以内
        }
        
        return decision;
    }

    @Override
    public Map<String, Object> getDecisionHistory() {
        Map<String, Object> history = new HashMap<>();
        history.put("decisions", decisionHistory);
        history.put("count", decisionHistory.size());
        history.put("last_updated", System.currentTimeMillis());
        return history;
    }

    /**
     * 获取当前时间段
     */
    private String getTimeOfDay() {
        int hour = java.time.LocalDateTime.now().getHour();
        if (hour >= 6 && hour < 12) {
            return "morning";
        } else if (hour >= 12 && hour < 18) {
            return "afternoon";
        } else {
            return "evening";
        }
    }
}