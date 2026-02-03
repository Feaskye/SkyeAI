package com.skyeai.jarvis.llm.service;

public interface AliyunAIService {
    /**
     * 调用阿里AI模型处理查询
     * @param query 用户查询
     * @return 模型响应
     */
    String callAliyunAI(String query);

    /**
     * 生成响应
     * @param prompt 提示文本
     * @return 生成的响应
     */
    String generateResponse(String prompt);

    /**
     * 带上下文调用AI模型
     * @param query 用户查询
     * @param context 上下文信息
     * @return 模型响应
     */
    String callWithContext(String query, String context);

    /**
     * 调用AI模型处理提示文本
     * @param prompt 提示文本
     * @return 模型响应
     */
    String callAIModel(String prompt);

    /**
     * 调用阿里多模态AI模型处理图片
     * @param imageData 图片数据
     * @param userPrompt 用户提示文本
     * @return 模型响应
     */
    String processImageWithMultimodal(byte[] imageData, String userPrompt);

    /**
     * 调用阿里多模态AI模型处理语音
     * @param audioData 语音数据
     * @param userPrompt 用户提示文本
     * @return 模型响应
     */
    String processAudioWithMultimodal(byte[] audioData, String userPrompt);
}
