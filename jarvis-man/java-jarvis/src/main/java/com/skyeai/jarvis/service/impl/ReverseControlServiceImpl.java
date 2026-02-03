package com.skyeai.jarvis.service.impl;

import com.skyeai.jarvis.service.ReverseControlService;
import com.skyeai.jarvis.service.ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReverseControlServiceImpl implements ReverseControlService {
    private Map<String, ControlHandler> controlHandlers;
    private Map<String, Object> controlStatus;
    
    @Autowired
    private ServiceClient serviceClient;
    
    @PostConstruct
    public void initialize() {
        controlHandlers = new HashMap<>();
        controlStatus = new HashMap<>();
        
        // 注册默认控制处理器
        registerDefaultHandlers();
    }
    
    @PreDestroy
    public void shutdown() {
        controlHandlers.clear();
        controlStatus.clear();
    }
    
    private void registerDefaultHandlers() {
        // 系统控制处理器
        registerControlHandler("system", parameters -> {
            String action = (String) parameters.getOrDefault("action", "");
            
            switch (action) {
                case "shutdown":
                    return Map.of("success", true, "message", "系统正在关闭", "action", action);
                case "restart":
                    return Map.of("success", true, "message", "系统正在重启", "action", action);
                case "sleep":
                    return Map.of("success", true, "message", "系统正在进入睡眠状态", "action", action);
                default:
                    return Map.of("success", false, "message", "未知的系统操作", "action", action);
            }
        });
        
        // 服务控制处理器
        registerControlHandler("service", parameters -> {
            String serviceName = (String) parameters.getOrDefault("serviceName", "");
            String action = (String) parameters.getOrDefault("action", "");
            
            return Map.of(
                "success", true,
                "message", String.format("服务 %s 正在执行 %s 操作", serviceName, action),
                "serviceName", serviceName,
                "action", action
            );
        });
        
        // 网络控制处理器
        registerControlHandler("network", parameters -> {
            String action = (String) parameters.getOrDefault("action", "");
            String target = (String) parameters.getOrDefault("target", "");
            
            return Map.of(
                "success", true,
                "message", String.format("网络正在执行 %s 操作，目标: %s", action, target),
                "action", action,
                "target", target
            );
        });
        
        // 设备控制处理器
        registerControlHandler("device", parameters -> {
            String deviceType = (String) parameters.getOrDefault("deviceType", "");
            String deviceId = (String) parameters.getOrDefault("deviceId", "");
            String action = (String) parameters.getOrDefault("action", "");
            
            return Map.of(
                "success", true,
                "message", String.format("设备 %s (ID: %s) 正在执行 %s 操作", deviceType, deviceId, action),
                "deviceType", deviceType,
                "deviceId", deviceId,
                "action", action
            );
        });
        
        // 智能控制处理器
        registerControlHandler("smart", parameters -> {
            String action = (String) parameters.getOrDefault("action", "");
            
            switch (action) {
                case "predict_needs":
                    return serviceClient.getFromUserService("/sensing/predict-needs");
                case "analyze_behavior":
                    return serviceClient.getFromUserService("/sensing/analyze-behavior");
                case "generate_recommendations":
                    return serviceClient.getFromUserService("/sensing/generate-recommendations");
                case "make_decision":
                    Map<String, Object> context = (Map<String, Object>) parameters.getOrDefault("context", Map.of());
                    return serviceClient.callUserService("/sensing/make-decision", context);
                default:
                    return Map.of("success", false, "message", "未知的智能操作", "action", action);
            }
        });
    }
    
    @Override
    public Map<String, Object> executeControl(String controlType, Map<String, Object> parameters) {
        ControlHandler handler = controlHandlers.get(controlType);
        if (handler == null) {
            return Map.of("success", false, "message", "未知的控制类型: " + controlType);
        }
        
        try {
            Map<String, Object> result = handler.handleControl(parameters);
            
            // 更新控制状态
            controlStatus.put(controlType, Map.of(
                "lastExecuted", System.currentTimeMillis(),
                "lastParameters", parameters,
                "lastResult", result
            ));
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "控制执行失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getControlStatus() {
        return new HashMap<>(controlStatus);
    }
    
    @Override
    public Map<String, Object> getAvailableControls() {
        Map<String, Object> availableControls = new HashMap<>();
        for (String controlType : controlHandlers.keySet()) {
            availableControls.put(controlType, Map.of(
                "available", true,
                "lastStatus", controlStatus.getOrDefault(controlType, Map.of())
            ));
        }
        return availableControls;
    }
    
    @Override
    public void registerControlHandler(String controlType, ControlHandler handler) {
        controlHandlers.put(controlType, handler);
    }
    
    @Override
    public void unregisterControlHandler(String controlType) {
        controlHandlers.remove(controlType);
        controlStatus.remove(controlType);
    }
}
