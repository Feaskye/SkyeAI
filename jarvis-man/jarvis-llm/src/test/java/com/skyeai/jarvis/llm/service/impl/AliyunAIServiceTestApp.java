package com.skyeai.jarvis.llm.service.impl;

import com.skyeai.jarvis.llm.service.AliyunAIService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootApplication
public class AliyunAIServiceTestApp {

    public static void main(String[] args) throws IOException {
        // 启动Spring Boot应用
        ConfigurableApplicationContext context = SpringApplication.run(AliyunAIServiceTestApp.class, args);
        
        // 获取AliyunAIService实例
        AliyunAIService aliyunAIService = context.getBean(AliyunAIService.class);
        
        System.out.println("AliyunAIService实例获取成功: " + aliyunAIService);
        
        // 测试图片识别
        testImageRecognition(aliyunAIService);
        
        // 测试语音识别
        testSpeechRecognition(aliyunAIService);
        
        // 测试文本生成
        testTextGeneration(aliyunAIService);
        
        // 关闭应用
        context.close();
    }

    private static void testImageRecognition(AliyunAIService aliyunAIService) throws IOException {
        // 读取测试图片文件
        File imageFile = new File("d:\\Program Files\\gitWork\\SkyeAI\\jarvis-man\\jarvis-test\\ScreenShot00001.png");
        FileInputStream fis = new FileInputStream(imageFile);
        byte[] imageData = new byte[(int) imageFile.length()];
        fis.read(imageData);
        fis.close();

        // 测试图片识别
        String userPrompt = "请识别图片中的数字";
        String response = aliyunAIService.processImageWithMultimodal(imageData, userPrompt);

        System.out.println("\n测试图片识别结果:");
        System.out.println("用户提示: " + userPrompt);
        System.out.println("AI响应: " + response);
    }

    private static void testSpeechRecognition(AliyunAIService aliyunAIService) throws IOException {
        // 读取测试音频文件
        File audioFile = new File("d:\\Program Files\\gitWork\\SkyeAI\\jarvis-man\\jarvis-test\\test.mp3");
        FileInputStream fis = new FileInputStream(audioFile);
        byte[] audioData = new byte[(int) audioFile.length()];
        fis.read(audioData);
        fis.close();

        // 测试语音识别
        String userPrompt = "请识别这段音频中的内容";
        String response = aliyunAIService.processAudioWithMultimodal(audioData, userPrompt);

        System.out.println("\n测试语音识别结果:");
        System.out.println("用户提示: " + userPrompt);
        System.out.println("AI响应: " + response);
    }

    private static void testTextGeneration(AliyunAIService aliyunAIService) {
        // 测试文本生成
        String prompt = "你好，你是谁？请用中文回答。";
        String response = aliyunAIService.callAIModel(prompt);

        System.out.println("\n测试文本生成结果:");
        System.out.println("用户提示: " + prompt);
        System.out.println("AI响应: " + response);
    }
}
