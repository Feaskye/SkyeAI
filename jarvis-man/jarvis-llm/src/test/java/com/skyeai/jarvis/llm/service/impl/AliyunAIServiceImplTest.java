package com.skyeai.jarvis.llm.service.impl;

import com.skyeai.jarvis.llm.service.AliyunAIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
public class AliyunAIServiceImplTest {

    @Autowired
    private AliyunAIService aliyunAIService;

    @Test
    public void testProcessImageWithMultimodal() throws IOException {
        // 读取测试图片文件
        File imageFile = new File("d:\\Program Files\\gitWork\\SkyeAI\\jarvis-man\\jarvis-test\\ScreenShot00001.png");
        FileInputStream fis = new FileInputStream(imageFile);
        byte[] imageData = new byte[(int) imageFile.length()];
        fis.read(imageData);
        fis.close();

        // 测试图片识别
        String userPrompt = "请识别图片中的数字";
        String response = aliyunAIService.processImageWithMultimodal(imageData, userPrompt);

        System.out.println("测试图片识别结果:");
        System.out.println("用户提示: " + userPrompt);
        System.out.println("AI响应: " + response);
    }

    @Test
    public void testCallAIModel() {
        // 测试文本生成
        String prompt = "你好，你是谁？请用中文回答。";
        String response = aliyunAIService.callAIModel(prompt);

        System.out.println("测试文本生成结果:");
        System.out.println("用户提示: " + prompt);
        System.out.println("AI响应: " + response);
    }
}
