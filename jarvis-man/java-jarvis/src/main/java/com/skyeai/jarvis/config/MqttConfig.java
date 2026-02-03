package com.skyeai.jarvis.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT配置类
 */
@Configuration
public class MqttConfig {
    
    @Value("${mqtt.broker.url:tcp://localhost:9003}")
    private String brokerUrl;
    
    @Value("${mqtt.client.id:java-jarvis}")
    private String clientId;
    
    @Value("${mqtt.username:}")
    private String username;
    
    @Value("${mqtt.password:}")
    private String password;
    
    @Value("${mqtt.qos:1}")
    private int qos;
    
    @Value("${mqtt.keep.alive:60}")
    private int keepAlive;
    
    @Value("${mqtt.clean.session:true}")
    private boolean cleanSession;
    
    /**
     * 创建MQTT客户端
     */
    @Bean
    public MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        return client;
    }
    
    /**
     * 创建MQTT连接选项
     */
    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        
        // 不设置用户名和密码，允许匿名连接
        // 注意：Paho客户端如果设置了空字符串的用户名/密码，仍会发送认证请求
        // 所以这里完全不调用setUserName和setPassword方法
        
        // 设置连接参数
//        options.setUserName(cleanSession);
//        options.setPassword(cleanSession);
        options.setCleanSession(cleanSession);
        options.setKeepAliveInterval(keepAlive);
        options.setAutomaticReconnect(true);
        
        return options;
    }
    
    // Getters and Setters
    public String getBrokerUrl() {
        return brokerUrl;
    }
    
    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getQos() {
        return qos;
    }
    
    public void setQos(int qos) {
        this.qos = qos;
    }
    
    public int getKeepAlive() {
        return keepAlive;
    }
    
    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }
    
    public boolean isCleanSession() {
        return cleanSession;
    }
    
    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }
}