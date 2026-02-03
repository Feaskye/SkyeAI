package com.skyeai.jarvis.llm.service.impl;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class AliyunSDKTest {

    public static void main(String[] args) {
        try {
            // 读取测试图片文件
            File imageFile = new File("d:\\Program Files\\gitWork\\SkyeAI\\jarvis-man\\jarvis-test\\ScreenShot00001.png");
            System.out.println("图片文件存在: " + imageFile.exists());
            System.out.println("图片文件大小: " + imageFile.length() + " bytes");
            
            FileInputStream fis = new FileInputStream(imageFile);
            byte[] imageData = new byte[(int) imageFile.length()];
            fis.read(imageData);
            fis.close();

            // 编码图片数据为Base64
            String encodedImage = Base64.getEncoder().encodeToString(imageData);
            System.out.println("图片编码成功，Base64长度: " + encodedImage.length());

            // 构建用户消息
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(List.of(
                            Collections.singletonMap("base64_image", encodedImage),
                            Collections.singletonMap("text", "请识别图片中的数字")
                    ))
                    .build();

            // 构建请求参数
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey("")
                    .model("qwen3-vl-plus")
                    .messages(List.of(userMessage))
                    .build();

            // 创建并调用模型
            MultiModalConversation conv = new MultiModalConversation();
            System.out.println("开始调用阿里多模态AI模型...");
            MultiModalConversationResult result = conv.call(param);

            // 处理响应
            String response = (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
            System.out.println("\n测试图片识别结果:");
            System.out.println("AI响应: " + response);
        } catch (Exception e) {
            System.err.println("测试过程中出现异常:");
            e.printStackTrace();
        }
    }
}
