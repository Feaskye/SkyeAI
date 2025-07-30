package com.xiaomi.auto.midemo.service;

import com.baidu.aip.ocr.AipOcr;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Service
public class OcrService {
    @Value("${ocr.baidu.app-id}")
    private String appId;

    @Value("${ocr.baidu.api-key}")
    private String apiKey;

    @Value("${ocr.baidu.secret-key}")
    private String secretKey;

    private AipOcr client;

    @PostConstruct
    public void init() {
        // 初始化百度OCR客户端
        client = new AipOcr(appId, apiKey, secretKey);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
    }

    /**
     * 识别图片中的文字
     * @param imageUrl 图片URL
     * @return 识别结果文本
     */
    public String recognizeText(String imageUrl) {
        // 调用百度OCR的通用文字识别接口
        HashMap<String, String> options = new HashMap<>();
        options.put("language_type", "CHN_ENG");
        options.put("detect_direction", "true");
        options.put("detect_language", "true");
        options.put("probability", "true");

        // 远程图片识别
        JSONObject res = client.basicGeneralUrl(imageUrl, options);
        return extractTextFromResult(res);
    }

    /**
     * 从OCR结果中提取文本内容
     */
    private String extractTextFromResult(JSONObject result) {
        if (result.has("words_result")) {
            StringBuilder sb = new StringBuilder();
            result.getJSONArray("words_result").forEach(item -> {
                sb.append(((JSONObject) item).getString("words")).append(" ");
            });
            return sb.toString().trim();
        }
        return "";
    }
}