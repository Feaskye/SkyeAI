package com.skyeai.jarvis.controller;

import com.skyeai.jarvis.service.ReverseControlService;
import com.skyeai.jarvis.service.ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reverse-control")
public class ReverseControlController {
    @Autowired
    private ReverseControlService reverseControlService;
    
    @Autowired
    private ServiceClient serviceClient;
    
    // 反向控制相关接口
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeControl(@RequestBody Map<String, Object> request) {
        String controlType = (String) request.getOrDefault("controlType", "");
        Map<String, Object> parameters = (Map<String, Object>) request.getOrDefault("parameters", Map.of());
        
        if (controlType.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "控制类型不能为空"));
        }
        
        Map<String, Object> result = reverseControlService.executeControl(controlType, parameters);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getControlStatus() {
        Map<String, Object> status = reverseControlService.getControlStatus();
        return ResponseEntity.ok(Map.of("success", true, "data", status));
    }
    
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableControls() {
        Map<String, Object> availableControls = reverseControlService.getAvailableControls();
        return ResponseEntity.ok(Map.of("success", true, "data", availableControls));
    }
    
    // 主动感知相关接口
    @PostMapping("/sensing/start")
    public ResponseEntity<Map<String, Object>> startSensing() {
        serviceClient.callUserService("/sensing/start", Map.of());
        return ResponseEntity.ok(Map.of("success", true, "message", "主动感知已启动"));
    }
    
    @PostMapping("/sensing/stop")
    public ResponseEntity<Map<String, Object>> stopSensing() {
        serviceClient.callUserService("/sensing/stop", Map.of());
        return ResponseEntity.ok(Map.of("success", true, "message", "主动感知已停止"));
    }
    
    @GetMapping("/sensing/status")
    public ResponseEntity<Map<String, Object>> getSensingStatus() {
        Map<String, Object> status = serviceClient.getFromUserService("/sensing/status");
        return ResponseEntity.ok(Map.of("success", true, "data", status));
    }
    
    @GetMapping("/sensing/data")
    public ResponseEntity<Map<String, Object>> getSensingData() {
        Map<String, Object> data = serviceClient.getFromUserService("/sensing/data");
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
    
    @PostMapping("/sensing/configure")
    public ResponseEntity<Map<String, Object>> configureSensing(@RequestBody Map<String, Object> configuration) {
        Map<String, Object> result = serviceClient.callUserService("/sensing/configure", configuration);
        return ResponseEntity.ok(result);
    }
}
