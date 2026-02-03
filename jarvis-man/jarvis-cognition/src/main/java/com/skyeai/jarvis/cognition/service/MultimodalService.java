package com.skyeai.jarvis.cognition.service;

import com.skyeai.jarvis.cognition.model.MultimodalInput;
import com.skyeai.jarvis.cognition.model.MultimodalFusionResult;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface MultimodalService {

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
     * 处理多模态输入
     */
    MultimodalFusionResult processMultimodalInput(List<MultimodalInput> inputs) throws Exception;

    /**
     * 融合单个多模态输入
     */
    MultimodalFusionResult fuse(MultimodalInput input) throws Exception;

    /**
     * 处理文本输入
     */
    MultimodalFusionResult processText(String text) throws Exception;

    /**
     * 处理图像输入（字节数组）
     */
    MultimodalFusionResult processImage(byte[] imageData, String imageType) throws Exception;

    /**
     * 处理语音输入（字节数组）
     */
    MultimodalFusionResult processSpeech(byte[] audioData, String audioType) throws Exception;

    /**
     * 处理视频输入（字节数组）
     */
    MultimodalFusionResult processVideo(byte[] videoData, String videoType) throws Exception;

    /**
     * 批量处理多模态输入
     */
    List<MultimodalFusionResult> batchProcessMultimodalInput(List<MultimodalInput> inputs) throws Exception;

    /**
     * 图像识别
     */
    Map<String, Object> recognizeImage(InputStream imageStream, String format);

    /**
     * 识别Base64编码的图像
     */
    Map<String, Object> recognizeImage(String base64Image, String format);

    /**
     * 图像分类
     */
    Map<String, Double> classifyImage(InputStream imageStream, String format);

    /**
     * 物体检测
     */
    List<Map<String, Object>> detectObjects(InputStream imageStream, String format);

    /**
     * 光学字符识别（OCR）
     */
    String recognizeText(InputStream imageStream, String format);

    /**
     * 图像描述
     */
    String describeImage(InputStream imageStream, String format);

    /**
     * 识别语音文件
     */
    String recognizeSpeech(InputStream audioStream, String format, int sampleRate, int channels);

    /**
     * 识别Base64编码的语音
     */
    String recognizeSpeech(String base64Audio, String format, int sampleRate, int channels);

    /**
     * 语音合成
     */
    byte[] synthesizeSpeech(String text, String voice, String format, int sampleRate);

    /**
     * 数据模型：图像处理结果
     */
    class ImageProcessingResult {
        private String imageType;
        private int width;
        private int height;
        private long fileSize;
        private String contentType;
        private String description;
        private Map<String, Object> metadata;
        private boolean success;
        private String errorMessage;

        // Getters and Setters
        public String getImageType() {
            return imageType;
        }

        public void setImageType(String imageType) {
            this.imageType = imageType;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
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
    }

    /**
     * 数据模型：语音处理结果
     */
    class SpeechProcessingResult {
        private String audioType;
        private int sampleRate;
        private int channels;
        private long durationMs;
        private long fileSize;
        private String contentType;
        private String transcript;
        private Map<String, Object> metadata;
        private boolean success;
        private String errorMessage;

        // Getters and Setters
        public String getAudioType() {
            return audioType;
        }

        public void setAudioType(String audioType) {
            this.audioType = audioType;
        }

        public int getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
        }

        public int getChannels() {
            return channels;
        }

        public void setChannels(int channels) {
            this.channels = channels;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(long durationMs) {
            this.durationMs = durationMs;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getTranscript() {
            return transcript;
        }

        public void setTranscript(String transcript) {
            this.transcript = transcript;
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
    }

    /**
     * 数据模型：视频处理结果
     */
    class VideoProcessingResult {
        private String videoType;
        private int width;
        private int height;
        private int frameRate;
        private long durationMs;
        private long fileSize;
        private String contentType;
        private String description;
        private Map<String, Object> metadata;
        private boolean success;
        private String errorMessage;

        // Getters and Setters
        public String getVideoType() {
            return videoType;
        }

        public void setVideoType(String videoType) {
            this.videoType = videoType;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getFrameRate() {
            return frameRate;
        }

        public void setFrameRate(int frameRate) {
            this.frameRate = frameRate;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(long durationMs) {
            this.durationMs = durationMs;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
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
    }
}
