package com.skyeai.jarvis.cognition.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 图像理解服务，用于处理图像输入
 */
public interface ImageUnderstandingService {

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
     * 人脸识别
     */
    List<Map<String, Object>> recognizeFaces(InputStream imageStream, String format);

    /**
     * 图像情感分析
     */
    Map<String, Object> analyzeImageEmotion(InputStream imageStream, String format);

    /**
     * 批量处理图像
     */
    Map<String, Map<String, Object>> batchProcessImages(Map<String, InputStream> imageStreams, String format, String taskType);

    /**
     * 图像相似度比较
     */
    double compareImageSimilarity(InputStream imageStream1, InputStream imageStream2, String format);

    /**
     * 图像理解配置
     */
    void setConfig(Map<String, Object> config);

    /**
     * 获取支持的任务类型
     */
    String[] getSupportedTaskTypes();

    /**
     * 检查服务状态
     */
    boolean checkServiceStatus();
}