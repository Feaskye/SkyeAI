package com.skyeai.jarvis.llm.service.impl;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class DirectAudioRecognitionTest {

    @Test
    public void testDirectAudioRecognition() throws IOException, IllegalAccessException, NoSuchFieldException {
        System.out.println("开始测试阿里AI语音识别功能...");

        // 读取测试语音文件
        File audioFile = new File("../jarvis-test/test.mp3");
        if (!audioFile.exists()) {
            System.out.println("错误: 语音文件不存在!");
            return;
        }

        System.out.println("语音文件存在，开始处理...");

        // 读取语音数据
        byte[] audioData;
        try (FileInputStream inputStream = new FileInputStream(audioFile)) {
            audioData = inputStream.readAllBytes();
        }

        System.out.println("语音大小: " + audioData.length + " 字节");

        // 创建AliyunAIServiceImpl实例
        AliyunAIServiceImpl aliyunAIService = new AliyunAIServiceImpl();

        // 使用反射设置必要的属性
        setField(aliyunAIService, "aliyunVisionModel", "qwen-image-plus");
        setField(aliyunAIService, "aliyunApiKey", "test-api-key");
        setField(aliyunAIService, "aliyunAllModelEnabled", false);
        setField(aliyunAIService, "aliyunAllModel", "qwen-omni-turbo");
        setField(aliyunAIService, "aliyunAllModelApiKey", "");
        setField(aliyunAIService, "systemPrompt", "You are a helpful assistant named Jarvis");

        // 构建用户提示
        String userPrompt = "请识别音频中的内容并告诉我";
        System.out.println("用户提示: " + userPrompt);

        // 测试processAudioWithMultimodal方法
        try {
            System.out.println("调用processAudioWithMultimodal方法...");
            String response = aliyunAIService.processAudioWithMultimodal(audioData, userPrompt);
            System.out.println("AI响应结果: " + response);
            System.out.println("测试成功完成!");
        } catch (Exception e) {
            System.out.println("测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("语音识别测试完成!");
    }

    private static void setField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
