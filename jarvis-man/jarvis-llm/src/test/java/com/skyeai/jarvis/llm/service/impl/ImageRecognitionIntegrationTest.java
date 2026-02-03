package com.skyeai.jarvis.llm.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

public class ImageRecognitionIntegrationTest {

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testImageRecognition() {
        // 图片文件路径
        File imageFile = new File("d:\\Program Files\\gitWork\\SkyeAI\\jarvis-man\\jarvis-test\\ScreenShot00001.png");
        System.out.println("图片文件存在: " + imageFile.exists());
        System.out.println("图片文件大小: " + imageFile.length() + " bytes");

        // 构建请求
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new FileSystemResource(imageFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 发送请求
        System.out.println("开始测试图片识别...");
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8081/api/llm/process/image",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // 处理响应
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应内容: " + response.getBody());
    }

    public static void main(String[] args) {
        new ImageRecognitionIntegrationTest().testImageRecognition();
    }
}
