package com.skyeai.jarvis.llm.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface LlmService {
    /**
     * 生成文本响应
     */
    String generateText(String prompt);

    /**
     * 生成文本响应（带系统提示）
     */
    String generateText(String systemPrompt, String userPrompt);

    /**
     * 生成文本响应（带历史消息）
     */
    String generateText(String systemPrompt, List<Map<String, String>> messages);

    /**
     * 流式生成文本响应
     */
    void generateTextStream(String prompt, LlmStreamCallback callback);

    /**
     * 流式生成文本响应（带系统提示）
     */
    void generateTextStream(String systemPrompt, String userPrompt, LlmStreamCallback callback);

    /**
     * 流式生成文本响应（带历史消息）
     */
    void generateTextStream(String systemPrompt, List<Map<String, String>> messages, LlmStreamCallback callback);

    /**
     * 嵌入文本
     */
    List<Double> embedText(String text);

    /**
     * 批量嵌入文本
     */
    List<List<Double>> embedTexts(List<String> texts);

    /**
     * 获取模型信息
     */
    Map<String, Object> getModelInfo(String modelName);

    /**
     * 列出可用模型
     */
    List<String> listModels();

    /**
     * 处理图像
     */
    ImageProcessingResult processImage(InputStream imageStream, String imageType);

    /**
     * 处理语音
     */
    SpeechProcessingResult processSpeech(InputStream audioStream, String audioType);

    /**
     * 处理视频
     */
    VideoProcessingResult processVideo(InputStream videoStream, String videoType);

    /**
     * 融合多模态信息
     */
    MultimodalFusionResult fuseMultimodalInformation(Map<String, Object> multimodalData);

    /**
     * 执行ReAct决策流程
     */
    String executeReact(String query);

    /**
     * 生成任务规划
     */
    String generateTaskPlan(String query, String contextInfo, String userPreferences);

    /**
     * 生成思考过程
     */
    String generateThought(String query, String history, String contextInfo, String userPreferences, String taskPlan, List<String> tools);

    /**
     * 决定下一步行动
     */
    String decideAction(String query, String history, String contextInfo, String userPreferences, String taskPlan, List<String> tools);

    /**
     * 评估任务执行进度
     */
    boolean evaluateProgress(String query, String history, String taskPlan, String observation);

    /**
     * 生成最终回答
     */
    String generateFinalAnswer(String query, String history, String contextInfo, String userPreferences, String taskPlan);

    interface LlmStreamCallback {
        void onToken(String token);
        void onComplete();
        void onError(Exception e);
    }

    class ImageProcessingResult {
        private boolean success;
        private String errorMessage;
        private String description;
        private int width;
        private int height;
        private long fileSize;
        private String contentType;
        private String imageType;
        private Map<String, Object> metadata;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getImageType() { return imageType; }
        public void setImageType(String imageType) { this.imageType = imageType; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    class SpeechProcessingResult {
        private boolean success;
        private String errorMessage;
        private String transcript;
        private int sampleRate;
        private int channels;
        private long durationMs;
        private long fileSize;
        private String contentType;
        private String audioType;
        private Map<String, Object> metadata;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getTranscript() { return transcript; }
        public void setTranscript(String transcript) { this.transcript = transcript; }
        public int getSampleRate() { return sampleRate; }
        public void setSampleRate(int sampleRate) { this.sampleRate = sampleRate; }
        public int getChannels() { return channels; }
        public void setChannels(int channels) { this.channels = channels; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getAudioType() { return audioType; }
        public void setAudioType(String audioType) { this.audioType = audioType; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    class VideoProcessingResult {
        private boolean success;
        private String errorMessage;
        private String description;
        private int width;
        private int height;
        private double frameRate;
        private long durationMs;
        private long fileSize;
        private String contentType;
        private String videoType;
        private Map<String, Object> metadata;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public double getFrameRate() { return frameRate; }
        public void setFrameRate(double frameRate) { this.frameRate = frameRate; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getVideoType() { return videoType; }
        public void setVideoType(String videoType) { this.videoType = videoType; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    class MultimodalFusionResult {
        private boolean success;
        private String errorMessage;
        private Map<String, Object> fusedInformation;
        private String summary;
        private Map<String, Object> metadata;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public Map<String, Object> getFusedInformation() { return fusedInformation; }
        public void setFusedInformation(Map<String, Object> fusedInformation) { this.fusedInformation = fusedInformation; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}
