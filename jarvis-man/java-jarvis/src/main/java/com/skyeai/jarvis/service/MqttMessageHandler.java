package com.skyeai.jarvis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyeai.jarvis.model.HealthData;
import com.skyeai.jarvis.service.ServiceClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.logging.Logger;

/**
 * MQTT消息处理器
 */
@Service
public class MqttMessageHandler implements MqttCallback {
    
    private static final Logger logger = Logger.getLogger(MqttMessageHandler.class.getName());
    
    @Autowired
    private MqttClient mqttClient;
    
    @Autowired
    private MqttConnectOptions mqttConnectOptions;
    
    @Autowired
    private ServiceClient serviceClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${mqtt.health.topic:jarvis/health/#}")
    private String healthTopic;
    
    @Value("${mqtt.iot.command.topic:jarvis/iot/command/#}")
    private String iotCommandTopic;
    
    @Value("${mqtt.iot.status.topic:jarvis/iot/status/#}")
    private String iotStatusTopic;
    
    @Value("${mqtt.qos:1}")
    private int qos;
    
    /**
     * 初始化方法，连接MQTT服务器并订阅主题
     */
    @PostConstruct
    public void init() {
        try {
            // 设置回调
            mqttClient.setCallback(this);
            
            // 连接MQTT服务器
            if (!mqttClient.isConnected()) {
                mqttClient.connect(mqttConnectOptions);
                logger.info("MQTT client connected: " + mqttClient.getClientId());
                
                // 订阅主题
                subscribeToTopics();
            }
        } catch (MqttException e) {
            logger.severe("Failed to initialize MQTT client: " + e.getMessage());
        }
    }
    
    /**
     * 订阅MQTT主题
     */
    private void subscribeToTopics() throws MqttException {
        // 订阅健康数据主题
        mqttClient.subscribe(healthTopic, qos);
        logger.info("Subscribed to health topic: " + healthTopic);
        
        // 订阅IoT命令主题
        mqttClient.subscribe(iotCommandTopic, qos);
        logger.info("Subscribed to IoT command topic: " + iotCommandTopic);
        
        // 订阅IoT状态主题
        mqttClient.subscribe(iotStatusTopic, qos);
        logger.info("Subscribed to IoT status topic: " + iotStatusTopic);
    }
    
    /**
     * 销毁方法，断开MQTT连接
     */
    @PreDestroy
    public void destroy() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                logger.info("MQTT client disconnected: " + mqttClient.getClientId());
            }
        } catch (MqttException e) {
            logger.severe("Failed to disconnect MQTT client: " + e.getMessage());
        }
    }
    
    /**
     * 连接丢失回调
     */
    @Override
    public void connectionLost(Throwable cause) {
        logger.warning("MQTT connection lost: " + cause.getMessage());
    }
    
    /**
     * 消息到达回调
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), "UTF-8");
        logger.info("Received MQTT message: Topic=" + topic + ", Payload=" + payload);
        
        // 根据主题处理不同类型的消息
        if (topic.startsWith("jarvis/health/")) {
            // 处理健康数据消息
            handleHealthDataMessage(payload);
        } else if (topic.startsWith("jarvis/iot/command/")) {
            // 处理IoT命令消息
            handleIoTCommandMessage(payload);
        } else if (topic.startsWith("jarvis/iot/status/")) {
            // 处理IoT状态消息
            handleIoTStatusMessage(payload);
        } else {
            // 其他主题消息
            logger.warning("Unknown topic: " + topic);
        }
    }
    
    /**
     * 消息发送完成回调
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // 消息发送完成，可根据需要处理
    }
    
    /**
     * 处理健康数据消息
     */
    private void handleHealthDataMessage(String payload) {
        try {
            // 将JSON转换为HealthData对象
            // 注意：Go发送的消息字段名可能是下划线格式，需要配置Jackson支持下划线转驼峰
            HealthDataMqttDto mqttDto = objectMapper.readValue(payload, HealthDataMqttDto.class);
            
            // 转换为Java健康数据模型
            HealthData healthData = convertToHealthData(mqttDto);
            
            // 保存健康数据
            Map<String, Object> request = Map.of(
                "type", healthData.getType().toString(),
                "value", healthData.getValue(),
                "unit", healthData.getUnit(),
                "timestamp", healthData.getTimestamp(),
                "source", healthData.getSource(),
                "userId", healthData.getUserId()
            );
            serviceClient.callUserService("/health/receive", request);
        } catch (Exception e) {
            logger.severe("Failed to process health data message: " + e.getMessage());
        }
    }
    
    /**
     * 处理IoT命令消息
     */
    private void handleIoTCommandMessage(String payload) {
        // 处理IoT命令消息
        logger.info("IoT command message received: " + payload);
        // 这里可以添加IoT命令处理逻辑
    }
    
    /**
     * 处理IoT状态消息
     */
    private void handleIoTStatusMessage(String payload) {
        // 处理IoT状态消息
        logger.info("IoT status message received: " + payload);
        // 这里可以添加IoT状态处理逻辑
    }
    
    /**
     * 将MQTT DTO转换为健康数据模型
     */
    private HealthData convertToHealthData(HealthDataMqttDto mqttDto) {
        HealthData healthData = new HealthData();
        
        // 转换数据类型
        HealthData.DataType dataType = HealthData.DataType.valueOf(mqttDto.getDataType().toUpperCase());
        healthData.setType(dataType);
        
        healthData.setValue(mqttDto.getValue());
        healthData.setUnit(mqttDto.getUnit());
        healthData.setTimestamp(mqttDto.getTimestamp());
        healthData.setSource(mqttDto.getSource());
        healthData.setUserId(mqttDto.getUserId());
        
        return healthData;
    }
    
    /**
     * 健康数据MQTT DTO类
     * 用于接收Go发送的健康数据消息
     */
    private static class HealthDataMqttDto {
        private String dataType;
        private double value;
        private String unit;
        private java.time.LocalDateTime timestamp;
        private String source;
        private String userId;
        
        // Getters and Setters
        public String getDataType() {
            return dataType;
        }
        
        public void setDataType(String dataType) {
            this.dataType = dataType;
        }
        
        public double getValue() {
            return value;
        }
        
        public void setValue(double value) {
            this.value = value;
        }
        
        public String getUnit() {
            return unit;
        }
        
        public void setUnit(String unit) {
            this.unit = unit;
        }
        
        public java.time.LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(java.time.LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getSource() {
            return source;
        }
        
        public void setSource(String source) {
            this.source = source;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
    
    /**
     * 发送MQTT消息
     */
    public void sendMessage(String topic, Object payload) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(payload);
        MqttMessage message = new MqttMessage(jsonPayload.getBytes());
        message.setQos(qos);
        
        if (mqttClient.isConnected()) {
            mqttClient.publish(topic, message);
            logger.info("MQTT message published: Topic=" + topic + ", Payload=" + jsonPayload);
        } else {
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }
    }
}