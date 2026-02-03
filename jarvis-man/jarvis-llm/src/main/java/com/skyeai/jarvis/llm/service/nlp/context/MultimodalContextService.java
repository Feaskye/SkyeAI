package com.skyeai.jarvis.llm.service.nlp.context;

import java.util.List;
import java.util.Map;

/**
 * 多模态上下文服务，用于管理和理解多模态输入的上下文
 */
public interface MultimodalContextService {

    /**
     * 保存多模态上下文
     */
    void saveMultimodalContext(String sessionId, Map<String, Object> input, Map<String, Object> result);

    /**
     * 获取会话的多模态上下文
     */
    List<Map<String, Object>> getMultimodalContext(String sessionId, int limit);

    /**
     * 获取会话的特定类型上下文
     */
    List<Map<String, Object>> getMultimodalContextByType(String sessionId, String modalityType, int limit);

    /**
     * 提取会话的关键上下文信息
     */
    String extractKeyContextInfo(String sessionId, int recentCount);

    /**
     * 分析多模态上下文
     */
    Map<String, Object> analyzeMultimodalContext(String sessionId);

    /**
     * 预测用户意图
     */
    Map<String, Double> predictUserIntent(String sessionId);

    /**
     * 生成上下文感知的响应
     */
    String generateContextAwareResponse(String sessionId, String query);

    /**
     * 融合多模态上下文
     */
    Map<String, Object> fuseMultimodalContext(String sessionId);

    /**
     * 清理过期上下文
     */
    void cleanExpiredContext(long expirationHours);

    /**
     * 获取会话的上下文统计信息
     */
    Map<String, Object> getContextStatistics(String sessionId);

    /**
     * 导出会话上下文
     */
    Map<String, Object> exportSessionContext(String sessionId);

    /**
     * 导入会话上下文
     */
    void importSessionContext(String sessionId, Map<String, Object> contextData);

    /**
     * 重置会话上下文
     */
    void resetSessionContext(String sessionId);

    /**
     * 检测上下文异常
     */
    List<Map<String, Object>> detectContextAnomalies(String sessionId);

    /**
     * 优化上下文存储
     */
    void optimizeContextStorage();
}