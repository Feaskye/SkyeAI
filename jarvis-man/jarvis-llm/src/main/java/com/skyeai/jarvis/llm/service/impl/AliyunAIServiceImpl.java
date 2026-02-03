package com.skyeai.jarvis.llm.service.impl;

import com.skyeai.jarvis.llm.service.AliyunAIService;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AliyunAIServiceImpl implements AliyunAIService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.system.prompt:You are a helpful assistant}")
    private String systemPrompt;

    @Value("${jarvis.services.intent.url:http://java-jarvis:8000/api/intent}")
    private String intentServiceUrl;

    @Value("${jarvis.services.schedule.url:http://java-jarvis:8000/api/schedule}")
    private String scheduleServiceUrl;

    @Value("${jarvis.services.chat.url:http://java-jarvis:8000/api/chat}")
    private String chatServiceUrl;

    @Value("${llm.multimodal.aliyun.general.api_key:}")
    private String aliyunApiKey;

    @Value("${llm.multimodal.aliyun.general.models:qwen-turbo,qwen-max,qwen-plus,qwen-long-latest}")
    private String aliyunModels;

    @Value("${llm.multimodal.aliyun.vision.model:qwen-image-plus}")
    private String aliyunVisionModel;

    @Value("${llm.multimodal.aliyun.allmodel.enabled:true}")
    private boolean aliyunAllModelEnabled;

    @Value("${llm.multimodal.aliyun.allmodel.api_key}")
    private String aliyunAllModelApiKey;

    @Value("${llm.multimodal.aliyun.allmodel.model:qwen-omni-turbo}")
    private String aliyunAllModel;

    @Override
    public String callAliyunAI(String query) {
        try {
            // 1. 异步保存用户查询到聊天历史（只保存原始查询）
            saveChatHistoryAsync("default", query, "user");

            // 2. 从聊天上下文中提取关键信息
            String contextInfo = extractKeyInfoFromContext("default");

            // 3. 首先进行意图解析
            Map<String, Object> intentResult = parseIntent(query);
            System.out.println("意图解析结果: " + intentResult.get("intent") + ", 详情: " + intentResult.get("details"));

            // 4. 如果是日程管理意图，结合上下文处理
            if ("SCHEDULE".equals(intentResult.get("intent"))) {
                String result = handleScheduleIntent(query, intentResult);
                // 异步保存AI回复到聊天历史
                saveChatHistoryAsync("default", result, "assistant");
                return result;
            }

            // 5. 否则调用LLM服务处理普通查询，带上上下文信息
            String result = callLlmServiceWithContext(query, contextInfo);
            // 异步保存AI回复到聊天历史
            saveChatHistoryAsync("default", result, "assistant");
            return result;
        } catch (Exception e) {
            System.err.println("AI调用失败: " + e.getMessage());
            return "抱歉，AI调用失败：" + e.getMessage();
        }
    }

    @Override
    public String generateResponse(String prompt) {
        return callAIModel(prompt);
    }

    @Override
    public String callWithContext(String query, String context) {
        try {
            // 使用阿里通用模型
            String[] modelList = aliyunModels.split(",");
            String model = modelList[0].trim(); // 使用第一个模型
            
            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt + "\n\n聊天上下文:\n" + context)
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(query)
                    .build();
            
            GenerationParam param;
            if (!aliyunApiKey.isEmpty()) {
                param = GenerationParam.builder()
                        .apiKey(aliyunApiKey)
                        .model(model)
                        .messages(Arrays.asList(systemMsg, userMsg))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .build();
            } else {
                param = GenerationParam.builder()
                        .model(model)
                        .messages(Arrays.asList(systemMsg, userMsg))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .build();
            }
            
            System.out.println("调用阿里AI模型(带上下文): " + model);
            System.out.println("API Key配置: " + (!aliyunApiKey.isEmpty() ? "已配置" : "使用环境变量"));
            System.out.println("上下文内容: " + context);
            System.out.println("请求内容: " + query);
            
            GenerationResult result = gen.call(param);
            String response = result.getOutput().getChoices().get(0).getMessage().getContent();
            
            System.out.println("阿里AI响应: " + response);
            
            return response;
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.err.println("阿里AI调用失败: " + e.getMessage());
            return "抱歉，AI调用失败：" + e.getMessage();
        }
    }

    @Override
    public String callAIModel(String prompt) {
        try {
            // 使用阿里通用模型
            String[] modelList = aliyunModels.split(",");
            String model = modelList[0].trim(); // 使用第一个模型
            
            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt)
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(prompt)
                    .build();
            
            GenerationParam param;
            if (!aliyunApiKey.isEmpty()) {
                param = GenerationParam.builder()
                        .apiKey(aliyunApiKey)
                        .model(model)
                        .messages(Arrays.asList(systemMsg, userMsg))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .build();
            } else {
                param = GenerationParam.builder()
                        .model(model)
                        .messages(Arrays.asList(systemMsg, userMsg))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .build();
            }
            
            System.out.println("调用阿里AI模型: " + model);
            System.out.println("API Key配置: " + (!aliyunApiKey.isEmpty() ? "已配置" : "使用环境变量"));
            System.out.println("请求内容: " + prompt);
            
            GenerationResult result = gen.call(param);
            String response = result.getOutput().getChoices().get(0).getMessage().getContent();
            
            System.out.println("阿里AI响应: " + response);
            
            return response;
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.err.println("阿里AI调用失败: " + e.getMessage());
            return "抱歉，AI调用失败：" + e.getMessage();
        }
    }

    /**
     * 处理日程管理意图
     */
    private String handleScheduleIntent(String query, Map<String, Object> intentResult) {
        try {
            System.out.println("开始处理日程管理意图: " + query);

            // 调用日程解析服务
            Map<String, Object> request = new HashMap<>();
            request.put("query", query);
            request.put("intentResult", intentResult);

            Map<String, Object> response = restTemplate.postForObject(
                    scheduleServiceUrl + "/parse", request, Map.class);

            if (response != null && response.containsKey("success") && (Boolean) response.get("success")) {
                return "日程创建成功！\n标题：" + response.get("title") + "\n时间：" + response.get("time");
            } else {
                return "创建日程失败：无法解析日程表达式";
            }
        } catch (Exception e) {
            System.err.println("处理日程意图失败: " + e.getMessage());
            return "创建日程失败: " + e.getMessage();
        }
    }

    /**
     * 带上下文调用LLM服务处理查询
     */
    private String callLlmServiceWithContext(String query, String context) {
        try {
            // 使用阿里通用模型
            String[] modelList = aliyunModels.split(",");
            String model = modelList[0].trim(); // 使用第一个模型
            
            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt + "\n\n聊天上下文:\n" + context)
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(query)
                    .build();
            
            GenerationParam param;
            if (!aliyunApiKey.isEmpty()) {
                param = GenerationParam.builder()
                        .apiKey(aliyunApiKey)
                        .model(model)
                        .messages(Arrays.asList(systemMsg, userMsg))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .build();
            } else {
                param = GenerationParam.builder()
                        .model(model)
                        .messages(Arrays.asList(systemMsg, userMsg))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .build();
            }
            
            System.out.println("调用阿里AI模型(带上下文): " + model);
            System.out.println("API Key配置: " + (!aliyunApiKey.isEmpty() ? "已配置" : "使用环境变量"));
            System.out.println("上下文内容: " + context);
            System.out.println("请求内容: " + query);
            
            GenerationResult result = gen.call(param);
            String response = result.getOutput().getChoices().get(0).getMessage().getContent();
            
            System.out.println("阿里AI响应: " + response);
            
            return response;
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.err.println("阿里AI调用失败: " + e.getMessage());
            return "抱歉，AI调用失败：" + e.getMessage();
        }
    }

    /**
     * 解析意图
     */
    private Map<String, Object> parseIntent(String query) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("query", query);
            return restTemplate.postForObject(
                    intentServiceUrl + "/parse", request, Map.class);
        } catch (Exception e) {
            System.err.println("调用意图服务失败: " + e.getMessage());
            Map<String, Object> defaultResult = new HashMap<>();
            defaultResult.put("intent", "CHAT");
            defaultResult.put("details", "无法解析意图，默认按聊天处理");
            return defaultResult;
        }
    }

    /**
     * 异步保存聊天历史
     */
    private void saveChatHistoryAsync(String userId, String content, String role) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("content", content);
            request.put("role", role);
            restTemplate.postForObject(
                    chatServiceUrl + "/history/save", request, Void.class);
        } catch (Exception e) {
            System.err.println("保存聊天历史失败: " + e.getMessage());
        }
    }

    /**
     * 提取上下文关键信息
     */
    private String extractKeyInfoFromContext(String userId) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            Map<String, Object> response = restTemplate.postForObject(
                    chatServiceUrl + "/history/extract", request, Map.class);
            if (response != null && response.containsKey("context")) {
                return response.get("context").toString();
            }
        } catch (Exception e) {
            System.err.println("提取聊天上下文失败: " + e.getMessage());
        }
        return "";
    }

    @Override
    public String processImageWithMultimodal(byte[] imageData, String userPrompt) {
        try {
            // 构建请求
            String encodedImage = Base64.getEncoder().encodeToString(imageData);
            
            // 使用阿里全模态模型
            String model = aliyunVisionModel;
            String apiKey = aliyunApiKey;
            
            // 如果启用了全模态模型，优先使用
            if (aliyunAllModelEnabled && !aliyunAllModelApiKey.isEmpty()) {
                model = aliyunAllModel;
                apiKey = aliyunAllModelApiKey;
            }
            
            // 使用MultiModalConversation调用阿里图片模型
            MultiModalConversation conv = new MultiModalConversation();
            
            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("base64_image", encodedImage),
                            Collections.singletonMap("text", userPrompt))).build();
            
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .messages(Arrays.asList(userMessage))
                    .build();
            
            System.out.println("调用阿里多模态AI模型(图片识别): " + model);
            System.out.println("API Key配置: " + (!apiKey.isEmpty() ? "已配置" : "使用环境变量"));
            System.out.println("请求内容: " + userPrompt);
            System.out.println("图片大小: " + imageData.length + " bytes");
            
            MultiModalConversationResult result = conv.call(param);
            String response = (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
            
            System.out.println("阿里AI响应: " + response);
            
            return response;
        } catch (Exception e) {
            System.err.println("阿里多模态AI调用失败: " + e.getMessage());
            return "抱歉，图片识别失败：" + e.getMessage();
        }
    }
    
    @Override
    public String processAudioWithMultimodal(byte[] audioData, String userPrompt) {
        try {
            // 构建请求
            String encodedAudio = Base64.getEncoder().encodeToString(audioData);
            
            // 使用阿里全模态模型
            String model = aliyunVisionModel;
            String apiKey = aliyunApiKey;
            
            // 如果启用了全模态模型，优先使用
            if (aliyunAllModelEnabled && !aliyunAllModelApiKey.isEmpty()) {
                model = aliyunAllModel;
                apiKey = aliyunAllModelApiKey;
            }
            
            // 使用MultiModalConversation调用阿里语音模型
            MultiModalConversation conv = new MultiModalConversation();
            
            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("audio", encodedAudio),
                            Collections.singletonMap("text", userPrompt))).build();
            
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .messages(Arrays.asList(userMessage))
                    .build();
            
            System.out.println("调用阿里多模态AI模型(语音识别): " + model);
            System.out.println("API Key配置: " + (!apiKey.isEmpty() ? "已配置" : "使用环境变量"));
            System.out.println("请求内容: " + userPrompt);
            System.out.println("语音大小: " + audioData.length + " bytes");
            
            MultiModalConversationResult result = conv.call(param);
            String response = (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
            
            System.out.println("阿里AI响应: " + response);
            
            return response;
        } catch (Exception e) {
            System.err.println("阿里多模态AI调用失败: " + e.getMessage());
            return "抱歉，语音识别失败：" + e.getMessage();
        }
    }
}
