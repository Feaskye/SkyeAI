package com.skyeai.jarvis.llm.service.nlp.context;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MultimodalContextServiceImpl implements MultimodalContextService {

    private final Map<String, List<Map<String, Object>>> sessionContexts = new ConcurrentHashMap<>();

    @Override
    public void saveMultimodalContext(String sessionId, Map<String, Object> input, Map<String, Object> result) {
        List<Map<String, Object>> contextList = sessionContexts.computeIfAbsent(sessionId, k -> new ArrayList<>());
        
        Map<String, Object> context = new HashMap<>();
        context.put("timestamp", LocalDateTime.now());
        context.put("modalityType", input.get("type"));
        context.put("inputData", input.get("data"));
        context.put("inputMetadata", input.get("metadata"));
        context.put("fusionResult", result.get("fusionResult"));
        context.put("confidence", result.get("confidence"));
        context.put("processingTime", result.get("processingTime"));
        
        contextList.add(context);
        
        // 限制上下文数量，只保留最近的100条
        if (contextList.size() > 100) {
            contextList.subList(0, contextList.size() - 100).clear();
        }
    }

    @Override
    public List<Map<String, Object>> getMultimodalContext(String sessionId, int limit) {
        List<Map<String, Object>> contextList = sessionContexts.getOrDefault(sessionId, new ArrayList<>());
        int startIndex = Math.max(0, contextList.size() - limit);
        return contextList.subList(startIndex, contextList.size());
    }

    @Override
    public List<Map<String, Object>> getMultimodalContextByType(String sessionId, String modalityType, int limit) {
        List<Map<String, Object>> contextList = sessionContexts.getOrDefault(sessionId, new ArrayList<>());
        List<Map<String, Object>> filteredContexts = new ArrayList<>();
        
        for (Map<String, Object> context : contextList) {
            if (modalityType.equals(context.get("modalityType"))) {
                filteredContexts.add(context);
            }
        }
        
        int startIndex = Math.max(0, filteredContexts.size() - limit);
        return filteredContexts.subList(startIndex, filteredContexts.size());
    }

    @Override
    public String extractKeyContextInfo(String sessionId, int recentCount) {
        List<Map<String, Object>> contextList = getMultimodalContext(sessionId, recentCount);
        StringBuilder contextInfo = new StringBuilder();
        
        for (Map<String, Object> context : contextList) {
            String modalityType = (String) context.get("modalityType");
            String fusionResult = (String) context.get("fusionResult");
            
            contextInfo.append("[").append(modalityType).append("] ").append(fusionResult).append("\n");
        }
        
        return contextInfo.toString();
    }

    @Override
    public Map<String, Object> analyzeMultimodalContext(String sessionId) {
        List<Map<String, Object>> contextList = sessionContexts.getOrDefault(sessionId, new ArrayList<>());
        Map<String, Object> analysis = new HashMap<>();
        
        // 统计各模态的使用次数
        Map<String, Integer> modalityCount = new HashMap<>();
        for (Map<String, Object> context : contextList) {
            String modalityType = (String) context.get("modalityType");
            modalityCount.put(modalityType, modalityCount.getOrDefault(modalityType, 0) + 1);
        }
        
        analysis.put("modalityCount", modalityCount);
        analysis.put("totalContexts", contextList.size());
        analysis.put("recentContexts", getMultimodalContext(sessionId, 5));
        
        return analysis;
    }

    @Override
    public Map<String, Double> predictUserIntent(String sessionId) {
        // 模拟用户意图预测
        Map<String, Double> intentScores = new HashMap<>();
        intentScores.put("chat", 0.85);
        intentScores.put("query", 0.75);
        intentScores.put("command", 0.45);
        return intentScores;
    }

    @Override
    public String generateContextAwareResponse(String sessionId, String query) {
        // 提取上下文信息
        String contextInfo = extractKeyContextInfo(sessionId, 5);
        
        // 模拟生成上下文感知的响应
        return "基于上下文理解，我理解您的问题是：" + query + "\n\n根据之前的交互，我认为您可能想了解更多关于这个话题的信息。";
    }

    @Override
    public Map<String, Object> fuseMultimodalContext(String sessionId) {
        List<Map<String, Object>> contextList = sessionContexts.getOrDefault(sessionId, new ArrayList<>());
        Map<String, Object> fusionResult = new HashMap<>();
        
        // 融合各模态的信息
        Map<String, List<Object>> modalityResults = new HashMap<>();
        for (Map<String, Object> context : contextList) {
            String modalityType = (String) context.get("modalityType");
            String result = (String) context.get("fusionResult");
            
            modalityResults.computeIfAbsent(modalityType, k -> new ArrayList<>()).add(result);
        }
        
        fusionResult.put("modalityResults", modalityResults);
        fusionResult.put("fusionTime", LocalDateTime.now());
        fusionResult.put("totalContextsFused", contextList.size());
        
        return fusionResult;
    }

    @Override
    public void cleanExpiredContext(long expirationHours) {
        // 清理过期的上下文
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(expirationHours);
        
        for (Map.Entry<String, List<Map<String, Object>>> entry : sessionContexts.entrySet()) {
            List<Map<String, Object>> contextList = entry.getValue();
            List<Map<String, Object>> toRemove = new ArrayList<>();
            
            for (Map<String, Object> context : contextList) {
                LocalDateTime timestamp = (LocalDateTime) context.get("timestamp");
                if (timestamp.isBefore(cutoffTime)) {
                    toRemove.add(context);
                }
            }
            
            contextList.removeAll(toRemove);
            
            // 如果会话上下文为空，移除会话
            if (contextList.isEmpty()) {
                sessionContexts.remove(entry.getKey());
            }
        }
    }

    @Override
    public Map<String, Object> getContextStatistics(String sessionId) {
        List<Map<String, Object>> contextList = sessionContexts.getOrDefault(sessionId, new ArrayList<>());
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("totalContexts", contextList.size());
        
        // 统计各模态的使用次数
        Map<String, Integer> modalityCount = new HashMap<>();
        for (Map<String, Object> context : contextList) {
            String modalityType = (String) context.get("modalityType");
            modalityCount.put(modalityType, modalityCount.getOrDefault(modalityType, 0) + 1);
        }
        statistics.put("modalityCount", modalityCount);
        
        // 计算平均置信度
        double avgConfidence = 0.0;
        for (Map<String, Object> context : contextList) {
            Double confidence = (Double) context.get("confidence");
            if (confidence != null) {
                avgConfidence += confidence;
            }
        }
        if (!contextList.isEmpty()) {
            avgConfidence /= contextList.size();
        }
        statistics.put("averageConfidence", avgConfidence);
        
        return statistics;
    }

    @Override
    public Map<String, Object> exportSessionContext(String sessionId) {
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("sessionId", sessionId);
        exportData.put("contexts", sessionContexts.getOrDefault(sessionId, new ArrayList<>()));
        exportData.put("exportTime", LocalDateTime.now());
        return exportData;
    }

    @Override
    public void importSessionContext(String sessionId, Map<String, Object> contextData) {
        List<Map<String, Object>> contexts = (List<Map<String, Object>>) contextData.get("contexts");
        if (contexts != null) {
            sessionContexts.put(sessionId, contexts);
        }
    }

    @Override
    public void resetSessionContext(String sessionId) {
        sessionContexts.remove(sessionId);
    }

    @Override
    public List<Map<String, Object>> detectContextAnomalies(String sessionId) {
        // 模拟上下文异常检测
        List<Map<String, Object>> anomalies = new ArrayList<>();
        List<Map<String, Object>> contextList = sessionContexts.getOrDefault(sessionId, new ArrayList<>());
        
        if (contextList.size() > 0) {
            Map<String, Object> anomaly = new HashMap<>();
            anomaly.put("type", "context_anomaly");
            anomaly.put("description", "检测到上下文异常");
            anomaly.put("confidence", 0.75);
            anomalies.add(anomaly);
        }
        
        return anomalies;
    }

    @Override
    public void optimizeContextStorage() {
        // 优化上下文存储，例如压缩历史上下文
        for (Map.Entry<String, List<Map<String, Object>>> entry : sessionContexts.entrySet()) {
            List<Map<String, Object>> contextList = entry.getValue();
            // 这里可以添加压缩逻辑
        }
    }
}