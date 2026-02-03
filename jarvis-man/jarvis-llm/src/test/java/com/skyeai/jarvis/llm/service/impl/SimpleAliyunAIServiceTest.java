package com.skyeai.jarvis.llm.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class SimpleAliyunAIServiceTest {

    public static void main(String[] args) {
        try {
            // 直接实例化 AliyunAIServiceImpl
            AliyunAIServiceImpl aliyunAIService = new AliyunAIServiceImpl();
            
            // 打印所有字段，查看是否存在
            System.out.println("类的所有字段:");
            Field[] fields = aliyunAIService.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                System.out.println(field.getName() + " (" + field.getType().getName() + "): " + field.get(aliyunAIService));
            }
            
            // 设置必要的属性值
            System.out.println("\n设置属性值:");
            setField(aliyunAIService, "aliyunVisionModel", "qwen3-vl-plus");
            System.out.println("设置 aliyunVisionModel 成功");
            
            setField(aliyunAIService, "aliyunApiKey", ""); // 使用环境变量
            System.out.println("设置 aliyunApiKey 成功");
            
            setField(aliyunAIService, "aliyunAllModelEnabled", false);
            System.out.println("设置 aliyunAllModelEnabled 成功");
            
            setField(aliyunAIService, "aliyunAllModelApiKey", "");
            System.out.println("设置 aliyunAllModelApiKey 成功");
            
            setField(aliyunAIService, "aliyunAllModel", "qwen-omni-turbo");
            System.out.println("设置 aliyunAllModel 成功");
            
            setField(aliyunAIService, "aliyunModels", "qwen-turbo,qwen-max,qwen-plus,qwen-long-latest");
            System.out.println("设置 aliyunModels 成功");
            
            setField(aliyunAIService, "systemPrompt", "You are a helpful assistant.");
            System.out.println("设置 systemPrompt 成功");
            
            // 打印设置的属性值，验证是否正确设置
            System.out.println("\n设置后的属性值:");
            System.out.println("aliyunVisionModel: " + getField(aliyunAIService, "aliyunVisionModel"));
            System.out.println("aliyunApiKey: " + getField(aliyunAIService, "aliyunApiKey"));
            System.out.println("aliyunAllModelEnabled: " + getField(aliyunAIService, "aliyunAllModelEnabled"));
            System.out.println("aliyunAllModelApiKey: " + getField(aliyunAIService, "aliyunAllModelApiKey"));
            System.out.println("aliyunAllModel: " + getField(aliyunAIService, "aliyunAllModel"));
            
            // 读取测试图片文件
            File imageFile = new File("d:\\Program Files\\gitWork\\SkyeAI\\jarvis-man\\jarvis-test\\ScreenShot00001.png");
            System.out.println("\n图片文件存在: " + imageFile.exists());
            System.out.println("图片文件大小: " + imageFile.length() + " bytes");
            
            FileInputStream fis = new FileInputStream(imageFile);
            byte[] imageData = new byte[(int) imageFile.length()];
            fis.read(imageData);
            fis.close();

            // 测试图片识别
            String userPrompt = "请识别图片中的数字";
            System.out.println("\n开始测试图片识别...");
            String response = aliyunAIService.processImageWithMultimodal(imageData, userPrompt);

            System.out.println("\n测试图片识别结果:");
            System.out.println("用户提示: " + userPrompt);
            System.out.println("AI响应: " + response);
        } catch (Exception e) {
            System.err.println("测试过程中出现异常:");
            e.printStackTrace();
        }
    }
    
    // 使用反射设置私有字段
    private static void setField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    
    // 使用反射获取私有字段
    private static Object getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
