package com.skyeai.jarvis.service.nlp;

import java.util.HashMap;
import java.util.Map;

/**
 * 意图识别结果
 */
public class IntentResult {

    private String intent;
    private double confidence;
    private Map<String, String> entities;

    public IntentResult() {
        this.entities = new HashMap<>();
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

    public void addEntity(String type, String value) {
        this.entities.put(type, value);
    }
}
