package com.skyeai.jarvis.cognition.model;

import java.util.List;
import java.util.Map;

/**
 * 多模态融合结果
 */
public class MultimodalFusionResult {

    private String id;
    private String fusionResult;
    private List<ProcessedInput> processedInputs;
    private String fusionStrategy;
    private List<String> inputTypes;
    private double confidence;
    private long processingTime;
    private Map<String, Object> additionalInfo;
    private String timestamp;
    private Map<String, Object> metadata;
    private boolean success;
    private String errorMessage;
    private Map<String, Object> fusedInformation;
    private String summary;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFusionResult() {
        return fusionResult;
    }

    public void setFusionResult(String fusionResult) {
        this.fusionResult = fusionResult;
    }

    public List<ProcessedInput> getProcessedInputs() {
        return processedInputs;
    }

    public void setProcessedInputs(List<ProcessedInput> processedInputs) {
        this.processedInputs = processedInputs;
    }

    public String getFusionStrategy() {
        return fusionStrategy;
    }

    public void setFusionStrategy(String fusionStrategy) {
        this.fusionStrategy = fusionStrategy;
    }

    public List<String> getInputTypes() {
        return inputTypes;
    }

    public void setInputTypes(List<String> inputTypes) {
        this.inputTypes = inputTypes;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Object> getFusedInformation() {
        return fusedInformation;
    }

    public void setFusedInformation(Map<String, Object> fusedInformation) {
        this.fusedInformation = fusedInformation;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 处理后的输入
     */
    public static class ProcessedInput {
        private String id;
        private String type;
        private String content;
        private Map<String, Object> metadata;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}