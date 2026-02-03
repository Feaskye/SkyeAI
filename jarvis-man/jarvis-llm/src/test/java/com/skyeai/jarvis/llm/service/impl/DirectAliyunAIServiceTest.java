package com.skyeai.jarvis.llm.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DirectAliyunAIServiceTest {

    public static void main(String[] args) throws IOException {
        // 直接实例化 AliyunAIServiceImpl
        AliyunAIServiceImpl aliyunAIService = new AliyunAIServiceImpl();
        
        try {
            // 读取测试图片文件
            File imageFile = new File("d:\\Program Files\\gitWork\\SkyeAI\\jarvis-man\\jarvis-test\\ScreenShot00001.png");
            System.out.println("图片文件存在: " + imageFile.exists());
            System.out.println("图片文件大小: " + imageFile.length() + " bytes");
            
            FileInputStream fis = new FileInputStream(imageFile);
            byte[] imageData = new byte[(int) imageFile.length()];
            fis.read(imageData);
            fis.close();

            // 测试图片识别
            String userPrompt = "请识别图片中的数字";
            System.out.println("开始测试图片识别...");
            String response = aliyunAIService.processImageWithMultimodal(imageData, userPrompt);

            System.out.println("\n测试图片识别结果:");
            System.out.println("用户提示: " + userPrompt);
            System.out.println("AI响应: " + response);
        } catch (Exception e) {
            System.err.println("测试过程中出现异常:");
            e.printStackTrace();
        }
    }
}
