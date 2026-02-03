package com.skyeai.jarvis.cognition.service.impl;

import com.skyeai.jarvis.cognition.model.MultimodalInput;
import com.skyeai.jarvis.cognition.model.MultimodalFusionResult;
import com.skyeai.jarvis.cognition.service.MultimodalFusionService;
import com.skyeai.jarvis.cognition.service.MultimodalInputProcessor;
import com.skyeai.jarvis.cognition.service.ImageUnderstandingService;
import com.skyeai.jarvis.cognition.service.SpeechRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 多模态融合服务实现，用于将语音、文本、图像信息融合处理
 */
@Service
public class MultimodalFusionServiceImpl implements MultimodalFusionService {

    private final MultimodalInputProcessor inputProcessor;
    private final RestTemplate restTemplate;
    private final ImageUnderstandingService imageUnderstandingService;
    private final SpeechRecognitionService speechRecognitionService;

    @Value("${jarvis.services.llm.url:http://jarvis-llm:8080/api/llm}")
    private String llmServiceUrl;

    @Autowired
    public MultimodalFusionServiceImpl(RestTemplate restTemplate, ImageUnderstandingService imageUnderstandingService, SpeechRecognitionService speechRecognitionService) {
        this.inputProcessor = new MultimodalInputProcessor();
        this.restTemplate = restTemplate;
        this.imageUnderstandingService = imageUnderstandingService;
        this.speechRecognitionService = speechRecognitionService;
    }

    @Override
    public MultimodalFusionResult processMultimodalInput(List<MultimodalInput> inputs) throws Exception {
        // 处理每个输入
        List<MultimodalFusionResult.ProcessedInput> processedInputs = new ArrayList<>();
        for (MultimodalInput input : inputs) {
            MultimodalFusionResult.ProcessedInput processedInput = inputProcessor.processInput(input);
            processedInputs.add(processedInput);
        }

        // 融合处理结果
        return fuseInputs(processedInputs);
    }

    @Override
    public MultimodalFusionResult fuse(MultimodalInput input) throws Exception {
        // 将单个输入转换为列表
        List<MultimodalInput> inputs = new ArrayList<>();
        inputs.add(input);

        // 调用现有的处理方法
        MultimodalFusionResult result = processMultimodalInput(inputs);

        // 设置额外的字段
        List<String> inputTypes = new ArrayList<>();
        inputTypes.add(input.getType());
        result.setInputTypes(inputTypes);
        result.setConfidence(0.95); // 设置默认置信度
        result.setProcessingTime(100); // 设置默认处理时间（毫秒）

        return result;
    }

    @Override
    public MultimodalFusionResult processText(String text) throws Exception {
        MultimodalInput input = new MultimodalInput();
        input.setId(UUID.randomUUID().toString());
        input.setType("text");
        input.setContent(text);
        input.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return fuse(input);
    }

    @Override
    public MultimodalFusionResult processImage(byte[] imageData, String imageType) throws Exception {
        MultimodalInput input = new MultimodalInput();
        input.setId(UUID.randomUUID().toString());
        input.setType("image");
        input.setData(imageData);
        input.setDataType(imageType);
        input.setContent("图像数据");
        input.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return fuse(input);
    }

    @Override
    public MultimodalFusionResult processSpeech(byte[] audioData, String audioType) throws Exception {
        MultimodalInput input = new MultimodalInput();
        input.setId(UUID.randomUUID().toString());
        input.setType("speech");
        input.setData(audioData);
        input.setDataType(audioType);
        input.setContent("语音数据");
        input.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return fuse(input);
    }

    @Override
    public MultimodalFusionResult processVideo(byte[] videoData, String videoType) throws Exception {
        MultimodalInput input = new MultimodalInput();
        input.setId(UUID.randomUUID().toString());
        input.setType("video");
        input.setData(videoData);
        input.setDataType(videoType);
        input.setContent("视频数据");
        input.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return fuse(input);
    }

    @Override
    public List<MultimodalFusionResult> batchProcessMultimodalInput(List<MultimodalInput> inputs) throws Exception {
        List<MultimodalFusionResult> results = new ArrayList<>();
        for (MultimodalInput input : inputs) {
            results.add(fuse(input));
        }
        return results;
    }

    /**
     * 融合多个输入
     * @param processedInputs 处理后的输入列表
     * @return 融合结果
     * @throws Exception 异常
     */
    private MultimodalFusionResult fuseInputs(List<MultimodalFusionResult.ProcessedInput> processedInputs) throws Exception {
        // 根据输入类型选择融合策略
        String fusionStrategy = selectFusionStrategy(processedInputs);

        // 构建融合提示
        String fusionPrompt = buildFusionPrompt(processedInputs, fusionStrategy);

        // 使用AI模型进行融合处理
        String fusionResult = callLlmServiceForFusion(fusionPrompt);

        // 构建融合结果
        MultimodalFusionResult result = new MultimodalFusionResult();
        result.setId(UUID.randomUUID().toString());
        result.setFusionResult(fusionResult);
        result.setProcessedInputs(processedInputs);
        result.setFusionStrategy(fusionStrategy);
        result.setTimestamp(String.valueOf(System.currentTimeMillis()));

        // 提取输入类型
        List<String> inputTypes = new ArrayList<>();
        for (MultimodalFusionResult.ProcessedInput input : processedInputs) {
            inputTypes.add(input.getType());
        }
        result.setInputTypes(inputTypes);

        return result;
    }

    /**
     * 选择融合策略
     * @param processedInputs 处理后的输入列表
     * @return 融合策略名称
     */
    private String selectFusionStrategy(List<MultimodalFusionResult.ProcessedInput> processedInputs) {
        // 统计输入类型
        int textCount = 0;
        int speechCount = 0;
        int imageCount = 0;
        int videoCount = 0;

        for (MultimodalFusionResult.ProcessedInput input : processedInputs) {
            switch (input.getType()) {
                case "text":
                    textCount++;
                    break;
                case "speech":
                    speechCount++;
                    break;
                case "image":
                    imageCount++;
                    break;
                case "video":
                    videoCount++;
                    break;
            }
        }

        // 根据输入类型分布选择策略
        if (imageCount > 0 && (textCount > 0 || speechCount > 0)) {
            return "image_text_fusion";
        } else if (speechCount > 0 && textCount > 0) {
            return "speech_text_fusion";
        } else if (imageCount > 0) {
            return "image_only";
        } else if (speechCount > 0) {
            return "speech_only";
        } else if (videoCount > 0) {
            return "video_only";
        } else {
            return "text_only";
        }
    }

    /**
     * 构建融合提示
     * @param processedInputs 处理后的输入列表
     * @param strategy 融合策略
     * @return 融合提示
     */
    private String buildFusionPrompt(List<MultimodalFusionResult.ProcessedInput> processedInputs, String strategy) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个多模态理解助手，需要根据以下输入信息，生成一个综合的理解和响应。\n\n");

        // 根据策略构建不同的提示
        switch (strategy) {
            case "image_text_fusion":
                prompt.append("请融合以下图像和文本/语音信息，生成一个综合的理解和响应：\n\n");
                break;
            case "speech_text_fusion":
                prompt.append("请融合以下语音和文本信息，生成一个综合的理解和响应，注意语音中的情感信息：\n\n");
                break;
            case "image_only":
                prompt.append("请基于以下图像信息，生成一个详细的描述和可能的响应：\n\n");
                break;
            case "speech_only":
                prompt.append("请基于以下语音信息，生成一个综合的理解和响应，注意语音中的情感信息：\n\n");
                break;
            case "video_only":
                prompt.append("请基于以下视频信息，生成一个综合的理解和响应，注意视频中的关键内容：\n\n");
                break;
            case "text_only":
                prompt.append("请基于以下文本信息，生成一个综合的理解和响应：\n\n");
                break;
            default:
                prompt.append("请融合以下多模态输入信息，生成一个综合的理解和响应：\n\n");
        }

        for (int i = 0; i < processedInputs.size(); i++) {
            MultimodalFusionResult.ProcessedInput input = processedInputs.get(i);
            prompt.append("输入 " + (i + 1) + " (类型: " + input.getType() + "):\n");

            switch (input.getType()) {
                case "text":
                    prompt.append("文本: " + input.getContent() + "\n");
                    break;
                case "speech":
                    prompt.append("语音识别结果: " + input.getContent() + "\n");
                    if (input.getMetadata() != null && input.getMetadata().containsKey("emotion")) {
                        prompt.append("情感: " + input.getMetadata().get("emotion") + "\n");
                    }
                    break;
                case "image":
                    prompt.append("图像描述: " + input.getContent() + "\n");
                    if (input.getMetadata() != null) {
                        if (input.getMetadata().containsKey("objects")) {
                            prompt.append("检测到的物体: " + input.getMetadata().get("objects") + "\n");
                        }
                        if (input.getMetadata().containsKey("text")) {
                            prompt.append("识别到的文本: " + input.getMetadata().get("text") + "\n");
                        }
                    }
                    break;
                case "video":
                    prompt.append("视频描述: " + input.getContent() + "\n");
                    if (input.getMetadata() != null) {
                        if (input.getMetadata().containsKey("duration")) {
                            prompt.append("视频时长: " + input.getMetadata().get("duration") + "秒\n");
                        }
                        if (input.getMetadata().containsKey("objects")) {
                            prompt.append("检测到的物体: " + input.getMetadata().get("objects") + "\n");
                        }
                    }
                    break;
                default:
                    prompt.append("内容: " + input.getContent() + "\n");
            }

            prompt.append("\n");
        }

        // 根据策略添加不同的要求
        switch (strategy) {
            case "image_text_fusion":
                prompt.append("请生成一个综合的理解和响应，要求：\n");
                prompt.append("1. 结合图像内容和文本/语音信息\n");
                prompt.append("2. 突出图像中的关键信息\n");
                prompt.append("3. 保持文本/语音中的语义和情感\n");
                prompt.append("4. 生成自然、连贯的响应\n");
                break;
            case "speech_text_fusion":
                prompt.append("请生成一个综合的理解和响应，要求：\n");
                prompt.append("1. 结合语音识别结果和文本信息\n");
                prompt.append("2. 保持语音中的情感色彩\n");
                prompt.append("3. 生成符合上下文的响应\n");
                break;
            default:
                prompt.append("请生成一个综合的理解和响应，考虑所有输入的信息。");
        }

        return prompt.toString();
    }

    /**
     * 调用LLM服务进行融合处理
     * @param prompt 融合提示
     * @return 融合处理结果
     * @throws Exception 异常
     */
    private String callLlmServiceForFusion(String prompt) throws Exception {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("prompt", prompt);
            request.put("systemPrompt", "你是一个多模态理解助手，需要根据输入信息生成综合的理解和响应。");

            Map<String, Object> response = restTemplate.postForObject(
                    llmServiceUrl + "/generate/text", request, Map.class);

            if (response != null && response.containsKey("result")) {
                return response.get("result").toString();
            } else {
                throw new Exception("LLM服务返回格式错误");
            }
        } catch (Exception e) {
            System.err.println("调用LLM服务失败: " + e.getMessage());
            throw e;
        }
    }
}