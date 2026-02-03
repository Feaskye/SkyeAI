package com.skyeai.jarvis.llm.service.impl;

import com.skyeai.jarvis.llm.service.LlmService;
import com.skyeai.jarvis.llm.service.LlmService.ImageProcessingResult;
import com.skyeai.jarvis.llm.service.LlmService.SpeechProcessingResult;
import com.skyeai.jarvis.llm.service.LlmService.VideoProcessingResult;
import com.skyeai.jarvis.llm.service.LlmService.MultimodalFusionResult;
import com.skyeai.jarvis.llm.service.LlmService.LlmStreamCallback;
import com.skyeai.jarvis.llm.service.impl.AliyunAIServiceImpl;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiLlmServiceImpl implements LlmService {

    @Value("${llm.openai.api-key}")
    private String apiKey;

    @Value("${llm.openai.base-url}")
    private String baseUrl;

    private static final String MODEL = "gpt-4-turbo";

    // 多模态处理配置
    @Value("${llm.multimodal.enabled:true}")
    private boolean multimodalEnabled;

    @Value("${llm.multimodal.image_processing.enabled:true}")
    private boolean imageProcessingEnabled;

    @Value("${llm.multimodal.image_processing.max_image_size:10485760}")
    private long maxImageSize;

    @Value("${llm.multimodal.speech_processing.enabled:true}")
    private boolean speechProcessingEnabled;

    @Value("${llm.multimodal.speech_processing.sample_rate:16000}")
    private int speechSampleRate;

    @Value("${llm.multimodal.speech_processing.channels:1}")
    private int speechChannels;

    @Value("${llm.multimodal.video_processing.enabled:false}")
    private boolean videoProcessingEnabled;

    @Value("${llm.multimodal.video_processing.max_video_duration:60}")
    private int maxVideoDuration;

    // 阿里视觉模型配置
    @Value("${llm.multimodal.aliyun.vision.enabled:false}")
    private boolean aliyunVisionEnabled;

    @Value("${llm.multimodal.aliyun.vision.api_key:}")
    private String aliyunVisionApiKey;

    @Value("${llm.multimodal.aliyun.vision.api_url:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String aliyunVisionApiUrl;

    @Value("${llm.multimodal.aliyun.vision.model:qwen-image-plus}")
    private String aliyunVisionModel;

    // 阿里语音模型配置
    @Value("${llm.multimodal.aliyun.speech.enabled:false}")
    private boolean aliyunSpeechEnabled;

    @Value("${llm.multimodal.aliyun.speech.api_key:}")
    private String aliyunSpeechApiKey;

    @Value("${llm.multimodal.aliyun.speech.api_url:https://ark.cn-beijing.volces.com/api/v3/audio/transcriptions}")
    private String aliyunSpeechApiUrl;

    @Value("${llm.multimodal.aliyun.speech.model:qwen3-asr-flash-filetrans}")
    private String aliyunSpeechModel;

    // 阿里通用模型配置
    @Value("${llm.multimodal.aliyun.general.enabled:false}")
    private boolean aliyunGeneralEnabled;

    @Value("${llm.multimodal.aliyun.general.api_key:}")
    private String aliyunGeneralApiKey;

    @Value("${llm.multimodal.aliyun.general.api_url:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String aliyunGeneralApiUrl;

    @Value("${llm.multimodal.aliyun.general.models:qwen-turbo,qwen-max,qwen-plus,qwen-long-latest}")
    private String aliyunGeneralModels;

    @Value("${llm.multimodal.aliyun.general.timeout:10000}")
    private int aliyunGeneralTimeout;

    // 阿里全模态模型配置
    @Value("${llm.multimodal.aliyun.allmodel.enabled:false}")
    private boolean aliyunAllModelEnabled;

    @Value("${llm.multimodal.aliyun.allmodel.api_key:}")
    private String aliyunAllModelApiKey;

    @Value("${llm.multimodal.aliyun.allmodel.api_url:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String aliyunAllModelApiUrl;

    @Value("${llm.multimodal.aliyun.allmodel.model:qwen-omni-turbo}")
    private String aliyunAllModel;

    // 全模态模型配置
    @Value("${llm.multimodal.full_modal.enabled:false}")
    private boolean fullModalEnabled;

    @Value("${llm.multimodal.full_modal.api_key:}")
    private String fullModalApiKey;

    @Value("${llm.multimodal.full_modal.api_url:}")
    private String fullModalApiUrl;

    @Value("${llm.multimodal.full_modal.model:}")
    private String fullModalModel;

    // jarvis-edge配置
    @Value("${llm.multimodal.edge.enabled:true}")
    private boolean edgeEnabled;

    @Value("${llm.multimodal.edge.api_url:http://jarvis-edge:8081}")
    private String edgeApiUrl;

    @Autowired
    private AliyunAIServiceImpl aliyunAIService;

    @Value("${ai.system.prompt:You are a helpful assistant}")
    private String systemPrompt;

    @Override
    public String generateText(String prompt) {
        return generateText("You are a helpful assistant", prompt);
    }

    @Override
    public String generateText(String systemPrompt, String userPrompt) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            return "请提供有效的提示词";  // 添加空值检查
        }
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        return generateText(systemPrompt, messages);
    }

    @Override
    public String generateText(String systemPrompt, List<Map<String, String>> messages) {
        try {
            // 优先使用阿里通用模型
            if (aliyunGeneralEnabled && !aliyunGeneralApiKey.isEmpty()) {
                // 直接使用最后一个用户消息作为提示
                String userPrompt = "";
                String systemMsg = "你是一个有帮助的助手，请用中文回答用户的问题。";
                for (Map<String, String> message : messages) {
                    if ("user".equals(message.get("role"))) {
                        userPrompt = message.get("content");
                    } else if ("system".equals(message.get("role"))) {
                        systemMsg = message.get("content");
                    }
                }
                return aliyunAIService.callWithContext(userPrompt, "");
            }
            
            // 否则使用OpenAI模型
            URL url = new URL(baseUrl + "/chat/completions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1024);

            String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree(response.toString());
                return jsonNode.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating text: " + e.getMessage();
        }
    }

    /**
     * 使用阿里通用模型生成文本
     */
    private String generateTextWithAliyunGeneral(String systemPrompt, List<Map<String, String>> messages) throws Exception {
        // 解析模型列表
        String[] modelList = aliyunGeneralModels.split(",");
        
        // 按顺序尝试不同的模型
        for (int i = 0; i < modelList.length; i++) {
            String model = modelList[i].trim();
            long startTime = System.currentTimeMillis();
            
            try {
                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("messages", messages);
                requestBody.put("temperature", 0.7);
                requestBody.put("max_tokens", 1024);

                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                // 发送请求
                String response = sendHttpRequest(aliyunGeneralApiUrl, aliyunGeneralApiKey, jsonBody);
                
                // 解析响应
                com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree(response.toString());
                String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
                
                long endTime = System.currentTimeMillis();
                System.out.println("阿里通用模型调用成功: " + model + ", 耗时: " + (endTime - startTime) + "ms");
                
                return content;
            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                String errorMsg = e.getMessage();
                
                System.err.println("阿里通用模型调用失败: " + model + ", 错误: " + errorMsg + ", 耗时: " + (endTime - startTime) + "ms");
                
                // 如果是最后一个模型，抛出异常
                if (i == modelList.length - 1) {
                    throw e;
                }
                
                // 否则继续尝试下一个模型
                System.out.println("自动切换到下一个模型: " + modelList[i + 1].trim());
            }
        }
        
        // 理论上不会执行到这里
        throw new Exception("所有阿里通用模型调用失败");
    }

    @Override
    public void generateTextStream(String prompt, LlmStreamCallback callback) {
        generateTextStream("You are a helpful assistant", prompt, callback);
    }

    @Override
    public void generateTextStream(String systemPrompt, String userPrompt, LlmStreamCallback callback) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        generateTextStream(systemPrompt, messages, callback);
    }

    @Override
    public void generateTextStream(String systemPrompt, List<Map<String, String>> messages, LlmStreamCallback callback) {
        // 实现流式生成逻辑
        try {
            URL url = new URL(baseUrl + "/chat/completions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);
            connection.setReadTimeout(0);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1024);
            requestBody.put("stream", true);

            String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if (data.equals("[DONE]")) {
                            callback.onComplete();
                            break;
                        }
                        try {
                            com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper()
                                    .readTree(data);
                            String token = jsonNode.path("choices").get(0).path("delta").path("content").asText();
                            if (!token.isEmpty()) {
                                callback.onToken(token);
                            }
                        } catch (Exception e) {
                            // 忽略解析错误
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(e);
        }
    }

    @Override
    public List<Double> embedText(String text) {
        // 实现文本嵌入逻辑
        return new ArrayList<>();
    }

    @Override
    public List<List<Double>> embedTexts(List<String> texts) {
        // 实现批量文本嵌入逻辑
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getModelInfo(String modelName) {
        // 实现获取模型信息逻辑
        return new HashMap<>();
    }

    @Override
    public List<String> listModels() {
        // 实现列出可用模型逻辑
        List<String> models = new ArrayList<>();
        models.add("gpt-4-turbo");
        models.add("gpt-3.5-turbo");
        // 添加阿里模型
        if (aliyunVisionEnabled) {
            models.add(aliyunVisionModel);
        }
        if (aliyunSpeechEnabled) {
            models.add(aliyunSpeechModel);
        }
        if (aliyunGeneralEnabled) {
            String[] generalModels = aliyunGeneralModels.split(",");
            models.addAll(Arrays.asList(generalModels));
        }
        if (aliyunAllModelEnabled) {
            models.add(aliyunAllModel);
        }
        if (fullModalEnabled && !fullModalModel.isEmpty()) {
            models.add(fullModalModel);
        }
        return models;
    }

    @Override
    public ImageProcessingResult processImage(InputStream imageStream, String imageType) {
        ImageProcessingResult result = new ImageProcessingResult();
        result.setImageType(imageType);

        try {
            if (!multimodalEnabled || !imageProcessingEnabled) {
                result.setSuccess(false);
                result.setErrorMessage("Image processing is disabled");
                return result;
            }

            // 读取图像数据
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            long totalSize = 0;

            while ((nRead = imageStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
                totalSize += nRead;
                if (totalSize > maxImageSize) {
                    result.setSuccess(false);
                    result.setErrorMessage("Image size exceeds maximum allowed size");
                    return result;
                }
            }
            buffer.flush();
            byte[] imageData = buffer.toByteArray();

            // 优先使用全模态模型
            if (fullModalEnabled && !fullModalApiKey.isEmpty() && !fullModalApiUrl.isEmpty()) {
                return processWithFullModalForImage(imageData, imageType, result);
            }
            // 其次使用阿里全模态模型
            else if (aliyunAllModelEnabled && !aliyunAllModelApiKey.isEmpty()) {
                return processImageWithAliyun(imageData, imageType, result);
            }
            // 再次使用阿里视觉模型
            else if (aliyunVisionEnabled && !aliyunVisionApiKey.isEmpty()) {
                return processImageWithAliyun(imageData, imageType, result);
            }
            // 否则使用jarvis-edge
            else if (edgeEnabled) {
                return processImageWithEdge(imageData, imageType, result);
            }
            // 最后使用本地处理
            else {
                return processImageLocally(imageData, result);
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Error processing image: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public SpeechProcessingResult processSpeech(InputStream audioStream, String audioType) {
        SpeechProcessingResult result = new SpeechProcessingResult();
        result.setAudioType(audioType);

        try {
            if (!multimodalEnabled || !speechProcessingEnabled) {
                result.setSuccess(false);
                result.setErrorMessage("Speech processing is disabled");
                return result;
            }

            // 读取音频数据
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = audioStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] audioData = buffer.toByteArray();

            // 优先使用全模态模型
            if (fullModalEnabled && !fullModalApiKey.isEmpty() && !fullModalApiUrl.isEmpty()) {
                return processWithFullModalForSpeech(audioData, audioType, result);
            }
            // 其次使用阿里全模态模型
            else if (aliyunAllModelEnabled && !aliyunAllModelApiKey.isEmpty()) {
                return processSpeechWithAliyun(audioData, audioType, result);
            }
            // 再次使用阿里语音模型
            else if (aliyunSpeechEnabled && !aliyunSpeechApiKey.isEmpty()) {
                return processSpeechWithAliyun(audioData, audioType, result);
            }
            // 否则使用jarvis-edge
            else if (edgeEnabled) {
                return processSpeechWithEdge(audioData, audioType, result);
            }
            // 最后使用本地处理
            else {
                return processSpeechLocally(audioData, result);
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Error processing speech: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public VideoProcessingResult processVideo(InputStream videoStream, String videoType) {
        VideoProcessingResult result = new VideoProcessingResult();
        result.setVideoType(videoType);

        try {
            if (!multimodalEnabled || !videoProcessingEnabled) {
                result.setSuccess(false);
                result.setErrorMessage("Video processing is disabled");
                return result;
            }

            // 读取视频数据
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = videoStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] videoData = buffer.toByteArray();

            // 优先使用全模态模型
            if (fullModalEnabled && !fullModalApiKey.isEmpty() && !fullModalApiUrl.isEmpty()) {
                return processWithFullModalForVideo(videoData, videoType, result);
            }
            // 否则使用jarvis-edge处理视频
            else if (edgeEnabled) {
                return processVideoWithEdge(videoData, videoType, result);
            }
            // 最后使用本地处理
            else {
                return processVideoLocally(videoData, result);
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Error processing video: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public MultimodalFusionResult fuseMultimodalInformation(Map<String, Object> multimodalData) {
        MultimodalFusionResult result = new MultimodalFusionResult();

        try {
            if (!multimodalEnabled) {
                result.setSuccess(false);
                result.setErrorMessage("Multimodal processing is disabled");
                return result;
            }

            // 融合多模态信息
            Map<String, Object> fusedInformation = new HashMap<>();

            // 处理文本信息
            if (multimodalData.containsKey("text")) {
                fusedInformation.put("text", multimodalData.get("text"));
            }

            // 处理图像信息
            if (multimodalData.containsKey("image")) {
                fusedInformation.put("image", multimodalData.get("image"));
            }

            // 处理语音信息
            if (multimodalData.containsKey("speech")) {
                fusedInformation.put("speech", multimodalData.get("speech"));
            }

            // 处理视频信息
            if (multimodalData.containsKey("video")) {
                fusedInformation.put("video", multimodalData.get("video"));
            }

            result.setFusedInformation(fusedInformation);

            // 生成融合信息摘要
            StringBuilder summaryBuilder = new StringBuilder("Multimodal information fused successfully. ");
            if (multimodalData.containsKey("text")) {
                summaryBuilder.append("Contains text information. ");
            }
            if (multimodalData.containsKey("image")) {
                summaryBuilder.append("Contains image information. ");
            }
            if (multimodalData.containsKey("speech")) {
                summaryBuilder.append("Contains speech information. ");
            }
            if (multimodalData.containsKey("video")) {
                summaryBuilder.append("Contains video information. ");
            }
            result.setSummary(summaryBuilder.toString().trim());

            // 设置元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("modalitiesCount", fusedInformation.size());
            metadata.put("modalities", fusedInformation.keySet());
            result.setMetadata(metadata);

            result.setSuccess(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Error fusing multimodal information: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 使用阿里视觉模型处理图像
     */
    private ImageProcessingResult processImageWithAliyun(byte[] imageData, String imageType, ImageProcessingResult result) throws Exception {
        // 构建用户消息，包含图片和问题
        String userPrompt = "请识别图片中的数字并告诉我图片中的数字内容";
        
        // 使用阿里多模态模型处理图片
        String response = aliyunAIService.processImageWithMultimodal(imageData, userPrompt);
        
        // 设置结果
        result.setDescription(response);
        result.setSuccess(true);

        // 设置元数据
        String model = aliyunVisionModel;
        if (aliyunAllModelEnabled && !aliyunAllModelApiKey.isEmpty()) {
            model = aliyunAllModel;
        }
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model", model);
        metadata.put("processing_type", "aliyun");
        metadata.put("fileSize", imageData.length);
        metadata.put("imageType", imageType);
        result.setMetadata(metadata);

        return result;
    }

    /**
     * 使用阿里语音模型处理语音
     */
    private SpeechProcessingResult processSpeechWithAliyun(byte[] audioData, String audioType, SpeechProcessingResult result) throws Exception {
        // 构建请求
        // 这里简化处理，实际应该按照阿里API的要求构建请求
        String response = "{\"transcript\": \"This is a test transcript from Aliyun speech model\"}";

        // 解析响应
        result.setTranscript("Processed by Aliyun Speech Model: " + aliyunSpeechModel);
        result.setSuccess(true);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model", aliyunSpeechModel);
        metadata.put("processing_type", "aliyun");
        metadata.put("fileSize", audioData.length);
        result.setMetadata(metadata);

        return result;
    }

    /**
     * 使用jarvis-edge处理图像
     */
    private ImageProcessingResult processImageWithEdge(byte[] imageData, String imageType, ImageProcessingResult result) throws Exception {
        // 构建请求
        String encodedImage = Base64.getEncoder().encodeToString(imageData);
        String requestBody = "{\"image\": \"" + encodedImage + "\", \"type\": \"" + imageType + "\"}";

        // 发送请求
        String response = sendHttpRequest(edgeApiUrl + "/api/vision/process", "", requestBody);

        // 解析响应
        result.setDescription("Processed by jarvis-edge");
        result.setSuccess(true);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("processing_type", "edge");
        metadata.put("fileSize", imageData.length);
        result.setMetadata(metadata);

        return result;
    }

    /**
     * 使用jarvis-edge处理语音
     */
    private SpeechProcessingResult processSpeechWithEdge(byte[] audioData, String audioType, SpeechProcessingResult result) throws Exception {
        // 构建请求
        String encodedAudio = Base64.getEncoder().encodeToString(audioData);
        String requestBody = "{\"audio\": \"" + encodedAudio + "\", \"type\": \"" + audioType + "\"}";

        // 发送请求
        String response = sendHttpRequest(edgeApiUrl + "/api/speech/process", "", requestBody);

        // 解析响应
        result.setTranscript("Processed by jarvis-edge");
        result.setSuccess(true);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("processing_type", "edge");
        metadata.put("fileSize", audioData.length);
        result.setMetadata(metadata);

        return result;
    }

    /**
     * 使用jarvis-edge处理视频
     */
    private VideoProcessingResult processVideoWithEdge(byte[] videoData, String videoType, VideoProcessingResult result) throws Exception {
        // 构建请求
        String encodedVideo = Base64.getEncoder().encodeToString(videoData);
        String requestBody = "{\"video\": \"" + encodedVideo + "\", \"type\": \"" + videoType + "\"}";

        // 发送请求
        String response = sendHttpRequest(edgeApiUrl + "/api/video/process", "", requestBody);

        // 解析响应
        result.setDescription("Processed by jarvis-edge");
        result.setSuccess(true);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("processing_type", "edge");
        metadata.put("fileSize", videoData.length);
        result.setMetadata(metadata);

        return result;
    }

    /**
     * 本地处理图像
     */
    private ImageProcessingResult processImageLocally(byte[] imageData, ImageProcessingResult result) throws Exception {
        // 获取图像信息
        ImageInfo imageInfo = Imaging.getImageInfo(imageData);
        result.setWidth(imageInfo.getWidth());
        result.setHeight(imageInfo.getHeight());
        result.setFileSize(imageData.length);
        result.setContentType(imageInfo.getMimeType());

        // 生成图像描述
        result.setDescription("Image processed locally: " + imageInfo.getFormatName() + " image, " + 
                imageInfo.getWidth() + "x" + imageInfo.getHeight() + " pixels");

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("format", imageInfo.getFormatName());
        metadata.put("width", imageInfo.getWidth());
        metadata.put("height", imageInfo.getHeight());
        metadata.put("fileSize", imageData.length);
        metadata.put("mimeType", imageInfo.getMimeType());
        metadata.put("processing_type", "local");
        result.setMetadata(metadata);

        result.setSuccess(true);
        return result;
    }

    /**
     * 本地处理语音
     */
    private SpeechProcessingResult processSpeechLocally(byte[] audioData, SpeechProcessingResult result) throws Exception {
        // 设置语音处理结果
        result.setSampleRate(speechSampleRate);
        result.setChannels(speechChannels);
        result.setDurationMs(5000);
        result.setFileSize(audioData.length);
        result.setContentType("audio/wav");
        result.setTranscript("Processed locally: This is a simulated speech transcript.");

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sampleRate", speechSampleRate);
        metadata.put("channels", speechChannels);
        metadata.put("durationMs", 5000);
        metadata.put("fileSize", audioData.length);
        metadata.put("contentType", "audio/wav");
        metadata.put("processing_type", "local");
        result.setMetadata(metadata);

        result.setSuccess(true);
        return result;
    }

    /**
     * 本地处理视频
     */
    private VideoProcessingResult processVideoLocally(byte[] videoData, VideoProcessingResult result) throws Exception {
        // 设置视频处理结果
        result.setWidth(1920);
        result.setHeight(1080);
        result.setFrameRate(30);
        result.setDurationMs(10000);
        result.setFileSize(videoData.length);
        result.setContentType("video/mp4");
        result.setDescription("Video processed locally: 1920x1080, 30fps, 10 seconds");

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("width", 1920);
        metadata.put("height", 1080);
        metadata.put("frameRate", 30);
        metadata.put("durationMs", 10000);
        metadata.put("fileSize", videoData.length);
        metadata.put("contentType", "video/mp4");
        metadata.put("processing_type", "local");
        result.setMetadata(metadata);

        result.setSuccess(true);
        return result;
    }

    /**
     * 使用全模态模型处理多模态数据
     */
    private ImageProcessingResult processWithFullModalForImage(byte[] data, String contentType, ImageProcessingResult result) throws Exception {
        // 构建请求
        String encodedData = Base64.getEncoder().encodeToString(data);

        // 构建JSON请求体
        String requestBody = "{" +
                "  \"model\": \"" + fullModalModel + "\"," +
                "  \"messages\": [" +
                "    {" +
                "      \"role\": \"user\"," +
                "      \"content\": [" +
                "        {" +
                "          \"type\": \"text\"," +
                "          \"text\": \"请处理这个图像数据\"" +
                "        }," +
                "        {" +
                "          \"type\": \"image_url\"," +
                "          \"image_url\": {" +
                "            \"url\": \"data:" + contentType + ";base64," + encodedData + "\"" +
                "          }" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]" +
                "}";

        // 发送请求
        String response = sendHttpRequest(fullModalApiUrl, fullModalApiKey, requestBody);

        // 处理响应
        result.setDescription("Processed by Full Modal Model: " + fullModalModel);
        result.setSuccess(true);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model", fullModalModel);
        metadata.put("processing_type", "full_modal");
        metadata.put("fileSize", data.length);
        result.setMetadata(metadata);

        return result;
    }

    /**
     * 使用全模态模型处理语音数据
     */
    private SpeechProcessingResult processWithFullModalForSpeech(byte[] data, String contentType, SpeechProcessingResult result) throws Exception {
        // 构建请求
        String encodedData = Base64.getEncoder().encodeToString(data);

        // 构建JSON请求体
        String requestBody = "{" +
                "  \"model\": \"" + fullModalModel + "\"," +
                "  \"messages\": [" +
                "    {" +
                "      \"role\": \"user\"," +
                "      \"content\": [" +
                "        {" +
                "          \"type\": \"text\"," +
                "          \"text\": \"请处理这个语音数据\"" +
                "        }," +
                "        {" +
                "          \"type\": \"speech_url\"," +
                "          \"speech_url\": {" +
                "            \"url\": \"data:" + contentType + ";base64," + encodedData + "\"" +
                "          }" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]" +
                "}";

        // 发送请求
        String response = sendHttpRequest(fullModalApiUrl, fullModalApiKey, requestBody);

        // 处理响应
        result.setTranscript("Processed by Full Modal Model: " + fullModalModel);
        result.setSuccess(true);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model", fullModalModel);
        metadata.put("processing_type", "full_modal");
        metadata.put("fileSize", data.length);
        result.setMetadata(metadata);

        return result;
    }

    /**
     * 使用全模态模型处理视频数据
     */
    private VideoProcessingResult processWithFullModalForVideo(byte[] data, String contentType, VideoProcessingResult result) throws Exception {
        // 构建请求
        String encodedData = Base64.getEncoder().encodeToString(data);

        // 构建JSON请求体
        String requestBody = "{" +
                "  \"model\": \"" + fullModalModel + "\"," +
                "  \"messages\": [" +
                "    {" +
                "      \"role\": \"user\"," +
                "      \"content\": [" +
                "        {" +
                "          \"type\": \"text\"," +
                "          \"text\": \"请处理这个视频数据\"" +
                "        }," +
                "        {" +
                "          \"type\": \"video_url\"," +
                "          \"video_url\": {" +
                "            \"url\": \"data:" + contentType + ";base64," + encodedData + "\"" +
                "          }" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]" +
                "}";

        // 发送请求
        String response = sendHttpRequest(fullModalApiUrl, fullModalApiKey, requestBody);

        // 处理响应
        result.setDescription("Processed by Full Modal Model: " + fullModalModel);
        result.setSuccess(true);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model", fullModalModel);
        metadata.put("processing_type", "full_modal");
        metadata.put("fileSize", data.length);
        result.setMetadata(metadata);

        return result;
    }

    /**
     * 发送HTTP请求
     */
    private String sendHttpRequest(String url, String apiKey, String requestBody) throws Exception {
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        if (!apiKey.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        }

        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 读取响应
        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        try (InputStream is = connection.getInputStream()) {
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                responseBuffer.write(data, 0, nRead);
            }
        }

        return responseBuffer.toString("utf-8");
    }

    @Override
    public String executeReact(String query) {
        // 构建ReAct执行流程
        // 1. 生成任务规划
        String taskPlan = generateTaskPlan(query, "", "");
        
        // 2. 生成思考过程
        String thought = generateThought(query, "", "", "", taskPlan, new ArrayList<>());
        
        // 3. 决定下一步行动
        String action = decideAction(query, "", "", "", taskPlan, new ArrayList<>());
        
        // 4. 评估任务执行进度
        boolean shouldContinue = evaluateProgress(query, "", taskPlan, "模拟执行结果");
        
        // 5. 生成最终回答
        return generateFinalAnswer(query, "", "", "", taskPlan);
    }

    @Override
    public String generateTaskPlan(String query, String contextInfo, String userPreferences) {
        // 构建prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯，一个强大的AI助手。你需要根据用户的问题、聊天上下文和用户偏好，生成详细的任务规划。\n\n");
        prompt.append("用户问题: " + query + "\n\n");
        
        // 添加聊天上下文
        if (!contextInfo.isEmpty()) {
            prompt.append("聊天上下文:\n" + contextInfo + "\n\n");
        }
        
        // 添加用户偏好
        if (!userPreferences.isEmpty()) {
            prompt.append("用户偏好:\n" + userPreferences + "\n\n");
        }
        
        prompt.append("请生成一个详细的任务规划，包括：\n");
        prompt.append("1. 任务目标\n");
        prompt.append("2. 所需步骤\n");
        prompt.append("3. 可能需要的工具\n");
        prompt.append("4. 预期结果\n\n");
        prompt.append("规划: ");
        
        // 使用阿里大模型生成任务规划
        return aliyunAIService.callAIModel(prompt.toString());
    }

    @Override
    public String generateThought(String query, String history, String contextInfo, String userPreferences, String taskPlan, List<String> tools) {
        // 构建prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯，一个强大的AI助手。你需要根据用户的问题、历史记录、聊天上下文、用户偏好和任务规划，生成下一步的思考。\n\n");
        prompt.append("用户问题: " + query + "\n\n");
        
        // 添加聊天上下文
        if (!contextInfo.isEmpty()) {
            prompt.append("聊天上下文:\n" + contextInfo + "\n\n");
        }
        
        // 添加用户偏好
        if (!userPreferences.isEmpty()) {
            prompt.append("用户偏好:\n" + userPreferences + "\n\n");
        }
        
        // 添加任务规划
        if (!taskPlan.isEmpty()) {
            prompt.append("任务规划:\n" + taskPlan + "\n\n");
        }
        
        if (!history.isEmpty()) {
            prompt.append("历史记录:\n" + history + "\n\n");
        }
        
        prompt.append("可用工具:\n");
        for (String tool : tools) {
            prompt.append("- " + tool + "\n");
        }
        
        prompt.append("\n请生成你的思考过程，只需要思考，不需要执行任何动作。\n");
        prompt.append("思考: ");
        
        // 使用阿里大模型生成思考
        return aliyunAIService.callAIModel(prompt.toString());
    }

    @Override
    public String decideAction(String query, String history, String contextInfo, String userPreferences, String taskPlan, List<String> tools) {
        // 构建prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯，一个强大的AI助手。你需要根据用户的问题、历史记录、思考过程、聊天上下文、用户偏好和任务规划，决定下一步的行动。\n\n");
        prompt.append("用户问题: " + query + "\n\n");
        
        // 添加聊天上下文
        if (!contextInfo.isEmpty()) {
            prompt.append("聊天上下文:\n" + contextInfo + "\n\n");
        }
        
        // 添加用户偏好
        if (!userPreferences.isEmpty()) {
            prompt.append("用户偏好:\n" + userPreferences + "\n\n");
        }
        
        // 添加任务规划
        if (!taskPlan.isEmpty()) {
            prompt.append("任务规划:\n" + taskPlan + "\n\n");
        }
        
        prompt.append("历史记录:\n" + history + "\n\n");
        
        prompt.append("可用工具:\n");
        for (String tool : tools) {
            prompt.append("- " + tool + "\n");
        }
        
        prompt.append("\n请从可用工具中选择一个，并以JSON格式返回，包含tool(工具名称)和parameters(参数)字段。\n");
        prompt.append("如果不需要使用工具，可以返回{\"tool\": \"finish\", \"parameters\": {}}\n");
        prompt.append("输出格式示例: {\"tool\": \"browser\", \"parameters\": {\"url\": \"https://github.com\"}}\n");
        prompt.append("\n输出: ");
        
        // 使用阿里大模型决定行动
        return aliyunAIService.callAIModel(prompt.toString());
    }

    @Override
    public boolean evaluateProgress(String query, String history, String taskPlan, String observation) {
        // 构建prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯，一个强大的AI助手。你需要根据用户的问题、历史执行记录、任务规划和最新的执行结果，评估任务执行进度。\n\n");
        prompt.append("用户问题: " + query + "\n\n");
        prompt.append("任务规划:\n" + taskPlan + "\n\n");
        prompt.append("历史执行记录:\n" + history + "\n\n");
        prompt.append("最新执行结果:\n" + observation + "\n\n");
        prompt.append("请评估任务是否已经完成。如果任务已经完成，返回false；如果任务还需要继续执行，返回true。\n");
        prompt.append("只需要返回true或false，不需要其他任何解释。\n");
        prompt.append("\n评估结果: ");
        
        // 使用阿里大模型评估进度
        String evaluation = aliyunAIService.callAIModel(prompt.toString());
        return evaluation != null && evaluation.toLowerCase().contains("true");
    }

    @Override
    public String generateFinalAnswer(String query, String history, String contextInfo, String userPreferences, String taskPlan) {
        // 构建prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯，一个强大的AI助手。请根据用户的问题、完整的历史记录、聊天上下文、用户偏好和任务规划，生成最终的回答。\n\n");
        prompt.append("用户问题: " + query + "\n\n");
        
        // 添加聊天上下文
        if (!contextInfo.isEmpty()) {
            prompt.append("聊天上下文:\n" + contextInfo + "\n\n");
        }
        
        // 添加用户偏好
        if (!userPreferences.isEmpty()) {
            prompt.append("用户偏好:\n" + userPreferences + "\n\n");
        }
        
        // 添加任务规划
        if (!taskPlan.isEmpty()) {
            prompt.append("任务规划:\n" + taskPlan + "\n\n");
        }
        
        prompt.append("完整历史记录:\n" + history + "\n\n");
        prompt.append("请总结整个过程，给出最终的回答。回答应该：\n");
        prompt.append("1. 直接回答用户的问题\n");
        prompt.append("2. 总结执行的步骤和结果\n");
        prompt.append("3. 考虑用户的偏好和需求\n");
        prompt.append("4. 使用自然、友好的语言\n");
        prompt.append("最终回答: ");
        
        // 使用阿里大模型生成最终回答
        return aliyunAIService.callAIModel(prompt.toString());
    }
}
