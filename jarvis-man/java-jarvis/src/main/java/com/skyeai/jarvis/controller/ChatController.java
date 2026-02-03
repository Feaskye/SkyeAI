package com.skyeai.jarvis.controller;

import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.skyeai.jarvis.model.AnomalyDetectionResult;
import com.skyeai.jarvis.model.HealthData;
import com.skyeai.jarvis.service.ServiceClient;
import com.skyeai.jarvis.service.nlp.ConversationResult;
import com.skyeai.jarvis.service.nlp.EnhancedLanguageResult;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import api.EdgeServiceGrpc;
import api.Edge;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@EnableWebSocket
@RequestMapping("/api")
public class ChatController implements WebSocketConfigurer {
    
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/ws/chat").setAllowedOrigins("*");
    }
    
    private class ChatWebSocketHandler extends TextWebSocketHandler {
        // 使用Jackson进行JSON处理
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        
        
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            sessions.put(session.getId(), session);
            System.out.println("WebSocket连接已建立: " + session.getId());
        }
        
        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            String payload = message.getPayload();
            System.out.println("收到WebSocket消息: " + payload);
            
            try {
                // 使用Jackson解析JSON消息
                JsonNode jsonNode = objectMapper.readTree(payload);
                
                // 检查消息类型
                JsonNode typeNode = jsonNode.get("type");
                if (typeNode != null && "audio".equals(typeNode.asText())) {
                    // 处理音频消息
                    handleAudioMessage(session, jsonNode);
                } else if (typeNode != null && "image".equals(typeNode.asText())) {
                    // 处理图像消息
                    handleImageMessage(session, jsonNode);
                } else {
                    // 处理文本消息
                    handleTextChatMessage(session, jsonNode);
                }
            } catch (Exception e) {
                // 构建错误响应
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("error", e.getMessage());
                String errorJson = objectMapper.writeValueAsString(errorResponse);
                session.sendMessage(new TextMessage(errorJson));
            }
        }
        
        /**
         * 处理音频消息
         */
        private void handleAudioMessage(WebSocketSession session, JsonNode jsonNode) throws Exception {
            String model = jsonNode.has("model") ? jsonNode.get("model").asText() : "aliyun";
            String audioBase64 = jsonNode.has("audio") ? jsonNode.get("audio").asText() : "";
            String format = jsonNode.has("format") ? jsonNode.get("format").asText() : "wav";
            int sampleRate = jsonNode.has("sample_rate") ? jsonNode.get("sample_rate").asInt() : 16000;
            int channels = jsonNode.has("channels") ? jsonNode.get("channels").asInt() : 1;
            
            System.out.println("收到音频数据，长度: " + audioBase64.length() + " chars, 格式: " + format);
            
            // 解码Base64音频数据
            byte[] audioData = Base64.getDecoder().decode(audioBase64);
            System.out.println("解码后音频大小: " + audioData.length + " bytes");
            
            // 创建音频请求
            Edge.AudioRequest audioRequest = Edge.AudioRequest.newBuilder()
                    .setAudioData(com.google.protobuf.ByteString.copyFrom(audioData))
                    .setFormat(format)
                    .setSampleRate(sampleRate)
                    .setChannels(channels)
                    .build();
            
            // 调用go-edge的音频处理服务
            StringBuilder responseBuilder = new StringBuilder();
            float[] confidenceArray = {0.0f}; // 使用数组作为容器，绕过final限制
            
            // 创建响应观察者
            StreamObserver<Edge.AudioResponse> responseObserver = new StreamObserver<Edge.AudioResponse>() {
                @Override
                public void onNext(Edge.AudioResponse response) {
                    responseBuilder.append(response.getResult());
                    confidenceArray[0] = response.getConfidence();
                    System.out.println("音频处理结果: " + response.getResult() + ", 置信度: " + confidenceArray[0]);
                }
                
                @Override
                public void onError(Throwable t) {
                    System.err.println("音频处理失败: " + t.getMessage());
                    // 发送错误响应
                    try {
                        ObjectNode errorResponse = objectMapper.createObjectNode();
                        errorResponse.put("model", model);
                        errorResponse.put("error", "音频处理失败: " + t.getMessage());
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                    } catch (IOException e) {
                        System.err.println("发送错误响应失败: " + e.getMessage());
                    }
                }
                
                @Override
                public void onCompleted() {
                    try {
                        // 构建响应JSON
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("model", model);
                        response.put("response", responseBuilder.toString());
                        response.put("confidence", confidenceArray[0]);
                        
                        // 发送响应
                        String responseJson = objectMapper.writeValueAsString(response);
                        session.sendMessage(new TextMessage(responseJson));
                    } catch (IOException e) {
                        System.err.println("发送响应失败: " + e.getMessage());
                    }
                }
            };
            
            // 调用gRPC双向流方法
            StreamObserver<Edge.AudioRequest> requestObserver = edgeServiceStub.streamAudio(responseObserver);
            
            // 发送音频数据
            requestObserver.onNext(audioRequest);
            
            // 结束流
            requestObserver.onCompleted();
        }
        
        /**
         * 处理图像消息
         */
        private void handleImageMessage(WebSocketSession session, JsonNode jsonNode) throws Exception {
            String model = jsonNode.has("model") ? jsonNode.get("model").asText() : "aliyun";
            String imageBase64 = jsonNode.has("image") ? jsonNode.get("image").asText() : "";
            String format = jsonNode.has("format") ? jsonNode.get("format").asText() : "jpg";
            
            System.out.println("收到图像数据，长度: " + imageBase64.length() + " chars, 格式: " + format);
            
            try {
                // 调用多模态服务处理图像
                Map<String, Object> request = Map.of(
                        "image", imageBase64,
                        "format", format,
                        "task", "image_understanding"
                );
                Map<String, Object> imageResult = ChatController.this.serviceClient.callMultimodalService("/fusion/process", request);
                
                // 构建响应JSON
                ObjectNode response = objectMapper.createObjectNode();
                response.put("model", model);
                response.put("response", imageResult.get("description").toString());
                response.put("confidence", Double.parseDouble(imageResult.get("confidence").toString()));
                response.put("imageLabel", imageResult.get("label").toString());
                
                // 发送响应
                String responseJson = objectMapper.writeValueAsString(response);
                session.sendMessage(new TextMessage(responseJson));
            } catch (Exception e) {
                System.err.println("图像处理失败: " + e.getMessage());
                // 发送错误响应
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("model", model);
                errorResponse.put("error", "图像处理失败: " + e.getMessage());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
            }
        }
        
        /**
         * 处理文本消息
         */
        private void handleTextChatMessage(WebSocketSession session, JsonNode jsonNode) throws Exception {
            String model = jsonNode.has("model") ? jsonNode.get("model").asText() : "aliyun";
            String query = jsonNode.has("query") ? jsonNode.get("query").asText() : "";
            
            // 调用认知服务执行ReAct决策流程
            Map<String, Object> request = Map.of("query", query);
            String responseContent = ChatController.this.serviceClient.callCognitionService("/react", request);
            
            // 构建响应JSON
            ObjectNode response = objectMapper.createObjectNode();
            response.put("model", model);
            response.put("response", responseContent);
            
            // 发送响应
            String responseJson = objectMapper.writeValueAsString(response);
            System.out.println("发送WebSocket响应: " + responseJson);
            session.sendMessage(new TextMessage(responseJson));
        }
        
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            sessions.remove(session.getId());
            System.out.println("WebSocket连接已关闭: " + session.getId());
        }
    }

    // gRPC客户端注入
    @GrpcClient("edge")
    private EdgeServiceGrpc.EdgeServiceStub edgeServiceStub;
    
    private final WebClient webClient;
    private final String systemPrompt;
    private final ServiceClient serviceClient;

    public ChatController(WebClient.Builder webClientBuilder, 
                         @Value("${ai.system.prompt}") String systemPrompt,
                         ServiceClient serviceClient) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:11434")
                .build();
        this.systemPrompt = systemPrompt;
        this.serviceClient = serviceClient;
    }

    // 通用聊天接口
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        // 调用大模型服务，服务内部已处理异常
        String query = request.getOrDefault("query", "").toString();
        String model = request.getOrDefault("model", "aliyun").toString();
        System.out.println("接收到的查询内容: " + query + "，模型: " + model);
        
        // 先调用认知服务处理请求
        Map<String, Object> cognitionRequest = Map.of(
                "query", query
        );
        System.out.println("调用认知服务处理请求...");
        String cognitionResult = serviceClient.callCognitionService("/react", cognitionRequest);
        System.out.println("认知服务处理结果: " + cognitionResult);
        
        // 使用ServiceClient调用LLM服务
        Map<String, Object> llmRequest = Map.of(
                "prompt", cognitionResult,
                "model", model,
                "systemPrompt", "你是一个有帮助的助手，请用中文回答用户的问题。"
        );
        Map<String, Object> result = serviceClient.callLlmService("/generate/text", llmRequest);
        String response = result.get("result").toString();
        System.out.println("返回响应内容: " + response);
        
        // 构建响应
        Map<String, Object> responseMap = Map.of(
                "response", response,
                "model", model
        );
        
        return ResponseEntity.ok(responseMap);
    }

    // 通用音频处理接口
    @PostMapping("/chat/audio/{model}")
    public ResponseEntity<Map<String, Object>> chatWithAudio(@PathVariable String model, @RequestBody Map<String, String> request) {
        try {
            String audioBase64 = request.getOrDefault("audio", "");
            String format = request.getOrDefault("format", "wav");
            System.out.println("接收到音频数据，模型: " + model + "，格式: " + format);
            
            // 调用多模态服务处理音频
            Map<String, Object> multimodalRequest = Map.of(
                    "audio", audioBase64,
                    "format", format,
                    "task", "audio_transcription",
                    "model", model
            );
            Map<String, Object> result = serviceClient.callMultimodalService("/fusion/process", multimodalRequest);
            
            String response = result.get("description").toString();
            double confidence = Double.parseDouble(result.get("confidence").toString());
            
            System.out.println("音频处理结果: " + response);
            
            return ResponseEntity.ok(Map.of(
                    "response", response,
                    "confidence", confidence,
                    "model", model
            ));
        } catch (Exception e) {
            System.err.println("音频处理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "音频处理失败: " + e.getMessage(),
                    "model", model
            ));
        }
    }

    // 通用图片处理接口
    @PostMapping("/chat/image/{model}")
    public ResponseEntity<Map<String, Object>> chatWithImage(@PathVariable String model, @RequestBody Map<String, String> request) {
        try {
            String imageBase64 = request.getOrDefault("image", "");
            String format = request.getOrDefault("format", "jpg");
            System.out.println("接收到图片数据，模型: " + model + "，格式: " + format);
            
            // 调用多模态服务处理图片
            Map<String, Object> multimodalRequest = Map.of(
                    "image", imageBase64,
                    "format", format,
                    "task", "image_understanding",
                    "model", model
            );
            Map<String, Object> result = serviceClient.callMultimodalService("/fusion/process", multimodalRequest);
            
            String response = result.get("description").toString();
            double confidence = Double.parseDouble(result.get("confidence").toString());
            String label = result.get("label").toString();
            
            System.out.println("图片处理结果: " + response);
            
            return ResponseEntity.ok(Map.of(
                    "response", response,
                    "confidence", confidence,
                    "imageLabel", label,
                    "model", model
            ));
        } catch (Exception e) {
            System.err.println("图片处理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "图片处理失败: " + e.getMessage(),
                    "model", model
            ));
        }
    }

    // 请求和响应类
    public static class ChatRequest {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    public static class ChatResponse {
        private String response;

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }

    // Ollama API请求类
    private static class OllamaRequest {
        private String model;
        private String system;
        private String prompt;
        private boolean stream;

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSystem() {
            return system;
        }

        public void setSystem(String system) {
            this.system = system;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public boolean isStream() {
            return stream;
        }

        public void setStream(boolean stream) {
            this.stream = stream;
        }
    }

    // Ollama API响应类
    private static class OllamaResponse {
        private String response;

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
    
    /**
     * 获取健康数据
     */
    @GetMapping("/health-data")
    public ResponseEntity<Map<String, Object>> getHealthData(
            @RequestParam String userId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        // 调用用户服务获取健康数据
        Map<String, Object> request = Map.of(
                "userId", userId,
                "startTime", startTime,
                "endTime", endTime
        );
        Map<String, Object> result = serviceClient.callUserService("/health-data", request);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取健康数据统计
     */
    @GetMapping("/health-data/stats")
    public ResponseEntity<Map<String, Object>> getHealthStats(
            @RequestParam String userId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        // 调用用户服务获取健康数据统计
        Map<String, Object> request = Map.of(
                "userId", userId,
                "startTime", startTime,
                "endTime", endTime
        );
        Map<String, Object> result = serviceClient.callUserService("/health-data/stats", request);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取异常检测结果
     */
    @GetMapping("/anomalies")
    public ResponseEntity<Map<String, Object>> getAnomalies(
            @RequestParam String userId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        // 调用用户服务获取异常检测结果
        Map<String, Object> request = Map.of(
                "userId", userId,
                "startTime", startTime,
                "endTime", endTime
        );
        Map<String, Object> result = serviceClient.callUserService("/anomalies", request);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 发送IoT命令
     */
    @PostMapping("/iot/command")
    public ResponseEntity<Map<String, String>> sendIoTCommand(
            @RequestBody Map<String, Object> command) {
        
        // 这里应该处理IoT命令，暂时返回模拟结果
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "IoT命令已发送",
                "commandId", command.get("commandId").toString()
        ));
    }
    
    /**
     * 模拟获取健康数据
     */
    @GetMapping("/health-data/simulate")
    public ResponseEntity<Map<String, Object>> simulateHealthData(@RequestParam String userId) {
        // 调用用户服务模拟获取健康数据
        Map<String, Object> request = Map.of(
                "userId", userId,
                "source", "simulated"
        );
        Map<String, Object> result = serviceClient.callUserService("/health-data/simulate", request);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 多模态输入处理API
     */
    @PostMapping("/multimodal/process")
    public ResponseEntity<Map<String, Object>> processMultimodalInput(@RequestBody Map<String, Object> input) {
        try {
            // 调用多模态服务
            Map<String, Object> result = serviceClient.callMultimodalService("/fusion/process", input);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "result", result.get("fusionResult"),
                    "confidence", result.get("confidence"),
                    "inputTypes", result.get("inputTypes"),
                    "processingTime", result.get("processingTime")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to process multimodal input: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 自然语言增强API
     */
    @PostMapping("/nlp/enhance")
    public ResponseEntity<Map<String, Object>> enhanceNaturalLanguage(@RequestBody Map<String, Object> request) {
        try {
            String text = (String) request.get("text");
            if (text == null || text.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Text is required"
                ));
            }
            
            // 调用LLM服务进行自然语言增强
            Map<String, Object> llmRequest = Map.of(
                    "text", text,
                    "task", "enhance"
            );
            Map<String, Object> result = serviceClient.callLlmService("/nlp/enhance", llmRequest);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "enhancedText", result.get("enhancedText"),
                    "intent", result.get("intent"),
                    "entities", result.get("entities"),
                    "sentiment", result.get("sentiment")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to enhance natural language: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 多轮对话管理API
     */
    @PostMapping("/nlp/converse")
    public ResponseEntity<Map<String, Object>> manageConversation(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            String message = (String) request.get("message");
            
            if (sessionId == null || sessionId.isEmpty() || message == null || message.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Session ID and message are required"
                ));
            }
            
            // 调用LLM服务处理多轮对话
            Map<String, Object> llmRequest = Map.of(
                    "sessionId", sessionId,
                    "message", message
            );
            Map<String, Object> result = serviceClient.callLlmService("/nlp/converse", llmRequest);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "response", result.get("response"),
                    "context", result.get("context"),
                    "sessionId", result.get("sessionId"),
                    "isComplete", result.get("isComplete")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to manage conversation: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取历史聊天记录API
     */
    @GetMapping("/chat/history")
    public ResponseEntity<Map<String, Object>> getChatHistory() {
        try {
            // 调用知识服务获取最近的聊天历史
            Map<String, Object> request = Map.of(
                    "userId", "default"
            );
            Map<String, Object> result = serviceClient.callKnowledgeService("/chat/history", request);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to get chat history: " + e.getMessage()
            ));
        }
    }
}