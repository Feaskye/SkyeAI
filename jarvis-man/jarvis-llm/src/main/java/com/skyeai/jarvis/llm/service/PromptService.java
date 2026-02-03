package com.skyeai.jarvis.llm.service;

import java.util.Map;

public interface PromptService {
    /**
     * 获取系统提示
     */
    String getSystemPrompt(String promptType);

    /**
     * 获取系统提示（带参数）
     */
    String getSystemPrompt(String promptType, Map<String, Object> parameters);

    /**
     * 获取用户提示模板
     */
    String getUserPromptTemplate(String promptType);

    /**
     * 渲染用户提示模板
     */
    String renderUserPrompt(String promptType, Map<String, Object> parameters);

    /**
     * 优化提示
     */
    String optimizePrompt(String prompt);

    /**
     * 分析提示效果
     */
    Map<String, Object> analyzePromptEffectiveness(String prompt, String response);
}
