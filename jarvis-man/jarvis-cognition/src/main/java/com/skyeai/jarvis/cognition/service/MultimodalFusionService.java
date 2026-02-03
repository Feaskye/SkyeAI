package com.skyeai.jarvis.cognition.service;

import com.skyeai.jarvis.cognition.model.MultimodalInput;
import com.skyeai.jarvis.cognition.model.MultimodalFusionResult;

import java.util.List;

/**
 * 多模态融合服务，用于将语音、文本、图像信息融合处理
 */
public interface MultimodalFusionService {

    /**
     * 处理多模态输入
     * @param inputs 多模态输入列表
     * @return 融合处理结果
     * @throws Exception 异常
     */
    MultimodalFusionResult processMultimodalInput(List<MultimodalInput> inputs) throws Exception;

    /**
     * 融合单个多模态输入
     * @param input 多模态输入
     * @return 融合处理结果
     * @throws Exception 异常
     */
    MultimodalFusionResult fuse(MultimodalInput input) throws Exception;

    /**
     * 处理文本输入
     * @param text 文本输入
     * @return 处理结果
     * @throws Exception 异常
     */
    MultimodalFusionResult processText(String text) throws Exception;

    /**
     * 处理图像输入
     * @param imageData 图像数据
     * @param imageType 图像类型
     * @return 处理结果
     * @throws Exception 异常
     */
    MultimodalFusionResult processImage(byte[] imageData, String imageType) throws Exception;

    /**
     * 处理语音输入
     * @param audioData 语音数据
     * @param audioType 语音类型
     * @return 处理结果
     * @throws Exception 异常
     */
    MultimodalFusionResult processSpeech(byte[] audioData, String audioType) throws Exception;

    /**
     * 处理视频输入
     * @param videoData 视频数据
     * @param videoType 视频类型
     * @return 处理结果
     * @throws Exception 异常
     */
    MultimodalFusionResult processVideo(byte[] videoData, String videoType) throws Exception;

    /**
     * 批量处理多模态输入
     * @param inputs 多模态输入列表
     * @return 融合处理结果列表
     * @throws Exception 异常
     */
    List<MultimodalFusionResult> batchProcessMultimodalInput(List<MultimodalInput> inputs) throws Exception;
}