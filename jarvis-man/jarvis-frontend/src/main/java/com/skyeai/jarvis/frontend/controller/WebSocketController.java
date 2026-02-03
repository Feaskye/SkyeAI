package com.skyeai.jarvis.frontend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Controller
@EnableWebSocket
public class WebSocketController implements WebSocketConfigurer {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/ws/chat").setAllowedOrigins("*");
    }

    private class ChatWebSocketHandler extends TextWebSocketHandler {
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            sessions.put(session.getId(), session);
            System.out.println("WebSocket连接已建立: " + session.getId());

            // 发送连接成功消息
            ObjectNode response = objectMapper.createObjectNode();
            response.put("type", "connection");
            response.put("status", "connected");
            response.put("message", "WebSocket连接成功");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            String payload = message.getPayload();
            System.out.println("收到WebSocket消息: " + payload);

            try {
                // 解析JSON消息
                JsonNode jsonNode = objectMapper.readTree(payload);

                // 检查消息类型
                JsonNode typeNode = jsonNode.get("type");
                if (typeNode != null) {
                    String messageType = typeNode.asText();
                    switch (messageType) {
                        case "chat":
                            handleChatMessage(session, jsonNode);
                            break;
                        case "audio":
                            handleAudioMessage(session, jsonNode);
                            break;
                        case "image":
                            handleImageMessage(session, jsonNode);
                            break;
                        case "notification":
                            handleNotificationMessage(session, jsonNode);
                            break;
                        case "task":
                            handleTaskMessage(session, jsonNode);
                            break;
                        default:
                            handleUnknownMessage(session, jsonNode);
                            break;
                    }
                } else {
                    // 未知消息类型
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("type", "error");
                    errorResponse.put("message", "缺少消息类型");
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                }
            } catch (Exception e) {
                // 处理异常
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("type", "error");
                errorResponse.put("message", "处理消息失败: " + e.getMessage());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                System.err.println("处理WebSocket消息时发生错误: " + e.getMessage());
            }
        }

        private void handleChatMessage(WebSocketSession session, JsonNode jsonNode) throws Exception {
            // 处理聊天消息
            String message = jsonNode.has("query") ? jsonNode.get("query").asText() : "";
            String model = jsonNode.has("model") ? jsonNode.get("model").asText() : "aliyun";
            String userId = jsonNode.has("userId") ? jsonNode.get("userId").asText() : "default";

            System.out.println("处理聊天消息: " + message + "，模型: " + model);

            // 调用后端服务处理消息
            try {
                // 构建请求URL，指向 java-jarvis 服务，使用通用聊天接口
                String url = "http://localhost:8000/api/chat";
                System.out.println("请求URL: " + url);
                
                // 创建HTTP客户端
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000); // 10秒连接超时
                connection.setReadTimeout(30000); // 30秒读取超时
                
                // 构建请求体，包含query和model参数
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("query", message);
                requestBody.put("model", model);
                String requestBodyStr = objectMapper.writeValueAsString(requestBody);
                System.out.println("请求体: " + requestBodyStr);
                
                // 发送请求
                try (java.io.OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBodyStr.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }
                
                // 获取响应码
                int responseCode = connection.getResponseCode();
                System.out.println("响应码: " + responseCode);
                
                // 读取响应
                String responseContent;
                if (responseCode == 200) {
                    try (java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine);
                        }
                        responseContent = response.toString();
                    }
                    
                    System.out.println("响应内容: " + responseContent);
                    
                    // 解析后端响应
                    try {
                        JsonNode backendResponse = objectMapper.readTree(responseContent);
                        String aiResponse = backendResponse.has("response") ? backendResponse.get("response").asText() : "";
                        System.out.println("AI响应: " + aiResponse);
                        
                        // 构建WebSocket响应
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("type", "chat");
                        response.put("userId", userId);
                        response.put("model", model);
                        response.put("response", aiResponse);
                        response.put("timestamp", System.currentTimeMillis());

                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                        System.out.println("返回大模型响应成功");
                    } catch (Exception e) {
                        System.err.println("解析响应失败: " + e.getMessage());
                        throw new Exception("解析响应失败: " + e.getMessage());
                    }
                } else {
                    // 读取错误响应
                    try (java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine);
                        }
                        responseContent = response.toString();
                    }
                    System.err.println("HTTP错误响应: " + responseContent);
                    throw new Exception("HTTP错误: " + responseCode + "，响应: " + responseContent);
                }
            } catch (Exception e) {
                // 处理异常
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("type", "error");
                errorResponse.put("message", "调用大模型服务失败: " + e.getMessage());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                System.err.println("调用大模型服务时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleNotificationMessage(WebSocketSession session, JsonNode jsonNode) throws Exception {
            // 处理通知消息
            String notificationType = jsonNode.has("notificationType") ? jsonNode.get("notificationType").asText() : "info";

            // 这里应该调用后端服务获取通知，暂时返回模拟响应
            ObjectNode response = objectMapper.createObjectNode();
            response.put("type", "notification");
            response.put("notificationType", notificationType);
            response.put("message", "这是一条" + notificationType + "类型的通知");
            response.put("timestamp", System.currentTimeMillis());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }

        private void handleTaskMessage(WebSocketSession session, JsonNode jsonNode) throws Exception {
            // 处理任务消息
            String taskId = jsonNode.has("taskId") ? jsonNode.get("taskId").asText() : "";
            String taskStatus = jsonNode.has("status") ? jsonNode.get("status").asText() : "";

            // 这里应该调用后端服务处理任务，暂时返回模拟响应
            ObjectNode response = objectMapper.createObjectNode();
            response.put("type", "task");
            response.put("taskId", taskId);
            response.put("status", taskStatus);
            response.put("message", "任务" + taskId + "状态已更新为" + taskStatus);
            response.put("timestamp", System.currentTimeMillis());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }

        private void handleAudioMessage(WebSocketSession session, JsonNode jsonNode) throws Exception {
            // 处理音频消息
            String audio = jsonNode.has("audio") ? jsonNode.get("audio").asText() : "";
            String model = jsonNode.has("model") ? jsonNode.get("model").asText() : "aliyun";
            String userId = jsonNode.has("userId") ? jsonNode.get("userId").asText() : "default";

            System.out.println("处理音频消息，模型: " + model);

            // 调用后端服务处理音频消息
            try {
                // 构建请求URL，指向 java-jarvis 服务，注意添加 /api 前缀
                String url = "http://localhost:8000/api/chat/audio/" + model;
                System.out.println("请求URL: " + url);
                
                // 创建HTTP客户端
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000); // 10秒连接超时
                connection.setReadTimeout(30000); // 30秒读取超时
                
                // 构建请求体
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("audio", audio);
                String requestBodyStr = objectMapper.writeValueAsString(requestBody);
                System.out.println("请求体长度: " + requestBodyStr.length() + " bytes");
                
                // 发送请求
                try (java.io.OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBodyStr.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }
                
                // 获取响应码
                int responseCode = connection.getResponseCode();
                System.out.println("响应码: " + responseCode);
                
                // 读取响应
                String responseContent;
                if (responseCode == 200) {
                    try (java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine);
                        }
                        responseContent = response.toString();
                    }
                    
                    System.out.println("响应内容: " + responseContent);
                    
                    // 解析后端响应
                    try {
                        JsonNode backendResponse = objectMapper.readTree(responseContent);
                        String aiResponse = backendResponse.has("response") ? backendResponse.get("response").asText() : "";
                        System.out.println("AI响应: " + aiResponse);
                        
                        // 构建WebSocket响应
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("type", "audio");
                        response.put("userId", userId);
                        response.put("model", model);
                        response.put("response", aiResponse);
                        response.put("timestamp", System.currentTimeMillis());

                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                        System.out.println("返回音频消息响应成功");
                    } catch (Exception e) {
                        System.err.println("解析响应失败: " + e.getMessage());
                        throw new Exception("解析响应失败: " + e.getMessage());
                    }
                } else {
                    // 读取错误响应
                    try (java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine);
                        }
                        responseContent = response.toString();
                    }
                    System.err.println("HTTP错误响应: " + responseContent);
                    throw new Exception("HTTP错误: " + responseCode + "，响应: " + responseContent);
                }
            } catch (Exception e) {
                // 处理异常
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("type", "error");
                errorResponse.put("message", "调用大模型服务失败: " + e.getMessage());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                System.err.println("调用大模型服务时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleImageMessage(WebSocketSession session, JsonNode jsonNode) throws Exception {
            // 处理图片消息
            String image = jsonNode.has("image") ? jsonNode.get("image").asText() : "";
            String model = jsonNode.has("model") ? jsonNode.get("model").asText() : "aliyun";
            String format = jsonNode.has("format") ? jsonNode.get("format").asText() : "jpg";
            String userId = jsonNode.has("userId") ? jsonNode.get("userId").asText() : "default";

            System.out.println("处理图片消息，模型: " + model + "，格式: " + format);

            // 调用后端服务处理图片消息
            try {
                // 构建请求URL，指向 java-jarvis 服务，注意添加 /api 前缀
                String url = "http://localhost:8000/api/chat/image/" + model;
                System.out.println("请求URL: " + url);
                
                // 创建HTTP客户端
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000); // 10秒连接超时
                connection.setReadTimeout(30000); // 30秒读取超时
                
                // 构建请求体
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("image", image);
                requestBody.put("format", format);
                String requestBodyStr = objectMapper.writeValueAsString(requestBody);
                System.out.println("请求体长度: " + requestBodyStr.length() + " bytes");
                
                // 发送请求
                try (java.io.OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBodyStr.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }
                
                // 获取响应码
                int responseCode = connection.getResponseCode();
                System.out.println("响应码: " + responseCode);
                
                // 读取响应
                String responseContent;
                if (responseCode == 200) {
                    try (java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine);
                        }
                        responseContent = response.toString();
                    }
                    
                    System.out.println("响应内容: " + responseContent);
                    
                    // 解析后端响应
                    try {
                        JsonNode backendResponse = objectMapper.readTree(responseContent);
                        String aiResponse = backendResponse.has("response") ? backendResponse.get("response").asText() : "";
                        System.out.println("AI响应: " + aiResponse);
                        
                        // 构建WebSocket响应
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("type", "image");
                        response.put("userId", userId);
                        response.put("model", model);
                        response.put("response", aiResponse);
                        response.put("timestamp", System.currentTimeMillis());

                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                        System.out.println("返回图片消息响应成功");
                    } catch (Exception e) {
                        System.err.println("解析响应失败: " + e.getMessage());
                        throw new Exception("解析响应失败: " + e.getMessage());
                    }
                } else {
                    // 读取错误响应
                    try (java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine);
                        }
                        responseContent = response.toString();
                    }
                    System.err.println("HTTP错误响应: " + responseContent);
                    throw new Exception("HTTP错误: " + responseCode + "，响应: " + responseContent);
                }
            } catch (Exception e) {
                // 处理异常
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("type", "error");
                errorResponse.put("message", "调用大模型服务失败: " + e.getMessage());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                System.err.println("调用大模型服务时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleUnknownMessage(WebSocketSession session, JsonNode jsonNode) throws Exception {
            // 处理未知消息类型
            ObjectNode response = objectMapper.createObjectNode();
            response.put("type", "error");
            response.put("message", "未知消息类型");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            sessions.remove(session.getId());
            System.out.println("WebSocket连接已关闭: " + session.getId());
        }
    }

    /**
     * 向指定会话发送消息
     */
    public void sendMessageToSession(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                System.err.println("向会话发送消息失败: " + e.getMessage());
            }
        }
    }

    /**
     * 向所有会话广播消息
     */
    public void broadcastMessage(String message) {
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    System.err.println("广播消息失败: " + e.getMessage());
                }
            }
        }
    }
}
