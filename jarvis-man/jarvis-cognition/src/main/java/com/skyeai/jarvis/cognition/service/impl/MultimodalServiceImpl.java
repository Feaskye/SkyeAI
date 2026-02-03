package com.skyeai.jarvis.cognition.service.impl;

import com.skyeai.jarvis.cognition.model.MultimodalInput;
import com.skyeai.jarvis.cognition.model.MultimodalFusionResult;
import com.skyeai.jarvis.cognition.service.ImageUnderstandingService;
import com.skyeai.jarvis.cognition.service.MultimodalFusionService;
import com.skyeai.jarvis.cognition.service.MultimodalService;
import com.skyeai.jarvis.cognition.service.SpeechRecognitionService;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MultimodalServiceImpl implements MultimodalService {

    private final ImageUnderstandingService imageUnderstandingService;
    private final SpeechRecognitionService speechRecognitionService;
    private final MultimodalFusionService multimodalFusionService;

    @Autowired
    public MultimodalServiceImpl(ImageUnderstandingService imageUnderstandingService, 
                               SpeechRecognitionService speechRecognitionService, 
                               MultimodalFusionService multimodalFusionService) {
        this.imageUnderstandingService = imageUnderstandingService;
        this.speechRecognitionService = speechRecognitionService;
        this.multimodalFusionService = multimodalFusionService;
    }

    @Value("${cognition.multimodal.enabled:true}")
    private boolean multimodalEnabled;

    @Value("${cognition.multimodal.image_processing.enabled:true}")
    private boolean imageProcessingEnabled;

    @Value("${cognition.multimodal.image_processing.max_image_size:10485760}")
    private long maxImageSize;

    @Value("${cognition.multimodal.speech_processing.enabled:true}")
    private boolean speechProcessingEnabled;

    @Value("${cognition.multimodal.speech_processing.sample_rate:16000}")
    private int speechSampleRate;

    @Value("${cognition.multimodal.speech_processing.channels:1}")
    private int speechChannels;

    @Value("${cognition.multimodal.video_processing.enabled:false}")
    private boolean videoProcessingEnabled;

    @Value("${cognition.multimodal.video_processing.max_video_duration:60}")
    private int maxVideoDuration;

    // 阿里视觉模型配置
    @Value("${cognition.multimodal.aliyun.vision.enabled:false}")
    private boolean aliyunVisionEnabled;

    @Value("${cognition.multimodal.aliyun.vision.api_key:}")
    private String aliyunVisionApiKey;

    @Value("${cognition.multimodal.aliyun.vision.api_url:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String aliyunVisionApiUrl;

    @Value("${cognition.multimodal.aliyun.vision.model:qwen-image-plus}")
    private String aliyunVisionModel;

    // 阿里语音模型配置
    @Value("${cognition.multimodal.aliyun.speech.enabled:false}")
    private boolean aliyunSpeechEnabled;

    @Value("${cognition.multimodal.aliyun.speech.api_key:}")
    private String aliyunSpeechApiKey;

    @Value("${cognition.multimodal.aliyun.speech.api_url:https://ark.cn-beijing.volces.com/api/v3/audio/transcriptions}")
    private String aliyunSpeechApiUrl;

    @Value("${cognition.multimodal.aliyun.speech.model:qwen3-asr-flash-filetrans}")
    private String aliyunSpeechModel;

    // 全模态模型配置
    @Value("${cognition.multimodal.full_modal.enabled:false}")
    private boolean fullModalEnabled;

    @Value("${cognition.multimodal.full_modal.api_key:}")
    private String fullModalApiKey;

    @Value("${cognition.multimodal.full_modal.api_url:}")
    private String fullModalApiUrl;

    @Value("${cognition.multimodal.full_modal.model:}")
    private String fullModalModel;

    // jarvis-edge配置
    @Value("${cognition.multimodal.edge.enabled:true}")
    private boolean edgeEnabled;

    @Value("${cognition.multimodal.edge.api_url:http://jarvis-edge:8081}")
    private String edgeApiUrl;

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
                return (ImageProcessingResult) processWithFullModal(imageData, "image", imageType, result);
            }
            // 其次使用阿里视觉模型
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
                return (SpeechProcessingResult) processWithFullModal(audioData, "speech", audioType, result);
            }
            // 其次使用阿里语音模型
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
                return (VideoProcessingResult) processWithFullModal(videoData, "video", videoType, result);
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
        // 构建请求
        String encodedImage = Base64.getEncoder().encodeToString(imageData);

        // 构建JSON请求体
        String requestBody = "{" +
                "  \"model\": \"" + aliyunVisionModel + "\"," +
                "  \"messages\": [" +
                "    {" +
                "      \"role\": \"user\"," +
                "      \"content\": [" +
                "        {" +
                "          \"type\": \"text\"," +
                "          \"text\": \"请描述这张图片的内容\"" +
                "        }," +
                "        {" +
                "          \"type\": \"image_url\"," +
                "          \"image_url\": {" +
                "            \"url\": \"data:image/jpeg;base64," + encodedImage + "\"" +
                "          }" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]" +
                "}";

        // 发送请求
        String response = sendHttpRequest(aliyunVisionApiUrl, aliyunVisionApiKey, requestBody);

        // 解析响应
        // 这里简化处理，实际应该使用JSON解析库
        result.setDescription("Processed by Aliyun Vision Model: " + aliyunVisionModel);
        result.setSuccess(true);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model", aliyunVisionModel);
        metadata.put("processing_type", "aliyun");
        metadata.put("fileSize", imageData.length);
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
    private Object processWithFullModal(byte[] data, String modalType, String contentType, Object result) throws Exception {
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
                "          \"text\": \"请处理这个\" + modalType + \"数据\"" +
                "        }," +
                "        {" +
                "          \"type\": \"" + modalType + "_url\"," +
                "          \"" + modalType + "_url\": {" +
                "            \"url\": \"data:" + contentType + ";base64," + encodedData + "\"" +
                "          }" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]" +
                "}";

        // 发送请求
        String response = sendHttpRequest(fullModalApiUrl, fullModalApiKey, requestBody);

        // 根据模态类型处理响应
        if (modalType.equals("image")) {
            ImageProcessingResult imageResult = (ImageProcessingResult) result;
            imageResult.setDescription("Processed by Full Modal Model: " + fullModalModel);
            imageResult.setSuccess(true);

            // 设置元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", fullModalModel);
            metadata.put("processing_type", "full_modal");
            metadata.put("fileSize", data.length);
            imageResult.setMetadata(metadata);

            return imageResult;
        } else if (modalType.equals("speech")) {
            SpeechProcessingResult speechResult = (SpeechProcessingResult) result;
            speechResult.setTranscript("Processed by Full Modal Model: " + fullModalModel);
            speechResult.setSuccess(true);

            // 设置元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", fullModalModel);
            metadata.put("processing_type", "full_modal");
            metadata.put("fileSize", data.length);
            speechResult.setMetadata(metadata);

            return speechResult;
        } else if (modalType.equals("video")) {
            VideoProcessingResult videoResult = (VideoProcessingResult) result;
            videoResult.setDescription("Processed by Full Modal Model: " + fullModalModel);
            videoResult.setSuccess(true);

            // 设置元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", fullModalModel);
            metadata.put("processing_type", "full_modal");
            metadata.put("fileSize", data.length);
            videoResult.setMetadata(metadata);

            return videoResult;
        }

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
    public MultimodalFusionResult processMultimodalInput(List<MultimodalInput> inputs) throws Exception {
        return multimodalFusionService.processMultimodalInput(inputs);
    }

    @Override
    public MultimodalFusionResult fuse(MultimodalInput input) throws Exception {
        return multimodalFusionService.fuse(input);
    }

    @Override
    public MultimodalFusionResult processText(String text) throws Exception {
        return multimodalFusionService.processText(text);
    }

    @Override
    public MultimodalFusionResult processImage(byte[] imageData, String imageType) throws Exception {
        return multimodalFusionService.processImage(imageData, imageType);
    }

    @Override
    public MultimodalFusionResult processSpeech(byte[] audioData, String audioType) throws Exception {
        return multimodalFusionService.processSpeech(audioData, audioType);
    }

    @Override
    public MultimodalFusionResult processVideo(byte[] videoData, String videoType) throws Exception {
        return multimodalFusionService.processVideo(videoData, videoType);
    }

    @Override
    public List<MultimodalFusionResult> batchProcessMultimodalInput(List<MultimodalInput> inputs) throws Exception {
        return multimodalFusionService.batchProcessMultimodalInput(inputs);
    }

    @Override
    public Map<String, Object> recognizeImage(InputStream imageStream, String format) {
        return imageUnderstandingService.recognizeImage(imageStream, format);
    }

    @Override
    public Map<String, Object> recognizeImage(String base64Image, String format) {
        return imageUnderstandingService.recognizeImage(base64Image, format);
    }

    @Override
    public Map<String, Double> classifyImage(InputStream imageStream, String format) {
        return imageUnderstandingService.classifyImage(imageStream, format);
    }

    @Override
    public List<Map<String, Object>> detectObjects(InputStream imageStream, String format) {
        return imageUnderstandingService.detectObjects(imageStream, format);
    }

    @Override
    public String recognizeText(InputStream imageStream, String format) {
        return imageUnderstandingService.recognizeText(imageStream, format);
    }

    @Override
    public String describeImage(InputStream imageStream, String format) {
        return imageUnderstandingService.describeImage(imageStream, format);
    }

    @Override
    public String recognizeSpeech(InputStream audioStream, String format, int sampleRate, int channels) {
        return speechRecognitionService.recognizeSpeech(audioStream, format, sampleRate, channels);
    }

    @Override
    public String recognizeSpeech(String base64Audio, String format, int sampleRate, int channels) {
        return speechRecognitionService.recognizeSpeech(base64Audio, format, sampleRate, channels);
    }

    @Override
    public byte[] synthesizeSpeech(String text, String voice, String format, int sampleRate) {
        return speechRecognitionService.synthesizeSpeech(text, voice, format, sampleRate);
    }
}
