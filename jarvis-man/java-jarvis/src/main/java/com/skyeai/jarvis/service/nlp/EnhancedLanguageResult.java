package com.skyeai.jarvis.service.nlp;

import java.util.Map;

/**
 * 增强后的语言理解结果
 */
public class EnhancedLanguageResult {

    private String originalText;
    private String enhancedUnderstanding;
    private String intent;
    private double confidence;
    private Map<String, String> entities;
    private String sentiment;

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

    public String getEnhancedText() {
        return enhancedUnderstanding;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}
