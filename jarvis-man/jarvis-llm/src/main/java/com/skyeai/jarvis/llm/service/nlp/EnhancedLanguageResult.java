package com.skyeai.jarvis.llm.service.nlp;

import java.util.Map;

/**
 * 增强的语言理解结果
 */
public class EnhancedLanguageResult {
    private String originalText;
    private String enhancedUnderstanding;
    private String intent;
    private double confidence;
    private Map<String, String> entities;
    private Map<String, Object> additionalInfo;

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getEnhancedUnderstanding() {
        return enhancedUnderstanding;
    }

    public void setEnhancedUnderstanding(String enhancedUnderstanding) {
        this.enhancedUnderstanding = enhancedUnderstanding;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public Map<String, String> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, String> entities) {
        this.entities = entities;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
