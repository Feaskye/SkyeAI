package com.skyeai.jarvis.controller;

import com.skyeai.jarvis.service.ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plugin")
public class PluginController {
    @Autowired
    private ServiceClient serviceClient;
    
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getPlugins() {
        Map<String, Object> response = serviceClient.getFromPluginService("/list");
        if (response != null && response.containsKey("data")) {
            List<Map<String, Object>> plugins = (List<Map<String, Object>>) response.get("data");
            return ResponseEntity.ok(plugins);
        }
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/get/{pluginName}")
    public ResponseEntity<Map<String, Object>> getPlugin(@PathVariable String pluginName) {
        Map<String, Object> response = serviceClient.getFromPluginService("/get?pluginName=" + pluginName);
        if (response != null && response.get("success").equals(true)) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "插件不存在"));
    }
    
    @PostMapping("/install")
    public ResponseEntity<Map<String, Object>> installPlugin(@RequestParam("pluginFile") MultipartFile pluginFile) {
        if (pluginFile == null || pluginFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "请上传插件文件"));
        }
        
        // 注意：文件上传需要特殊处理，这里简化处理
        Map<String, Object> request = Map.of("pluginFile", pluginFile);
        Map<String, Object> result = serviceClient.callPluginService("/install", request);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/uninstall/{pluginName}")
    public ResponseEntity<Map<String, Object>> uninstallPlugin(@PathVariable String pluginName) {
        Map<String, Object> request = Map.of("pluginName", pluginName);
        Map<String, Object> result = serviceClient.callPluginService("/uninstall", request);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/update/{pluginName}")
    public ResponseEntity<Map<String, Object>> updatePlugin(@PathVariable String pluginName, @RequestParam("pluginFile") MultipartFile pluginFile) {
        if (pluginFile == null || pluginFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "请上传插件文件"));
        }
        
        // 注意：文件上传需要特殊处理，这里简化处理
        Map<String, Object> request = Map.of(
            "pluginName", pluginName,
            "pluginFile", pluginFile
        );
        Map<String, Object> result = serviceClient.callPluginService("/update", request);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/enable/{pluginName}")
    public ResponseEntity<Map<String, Object>> enablePlugin(@PathVariable String pluginName) {
        Map<String, Object> request = Map.of("pluginName", pluginName);
        Map<String, Object> result = serviceClient.callPluginService("/enable", request);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/disable/{pluginName}")
    public ResponseEntity<Map<String, Object>> disablePlugin(@PathVariable String pluginName) {
        Map<String, Object> request = Map.of("pluginName", pluginName);
        Map<String, Object> result = serviceClient.callPluginService("/disable", request);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/execute/{pluginName}")
    public ResponseEntity<Map<String, Object>> executePlugin(@PathVariable String pluginName, @RequestBody Map<String, Object> parameters) {
        Map<String, Object> request = Map.of(
            "pluginName", pluginName,
            "parameters", parameters
        );
        Map<String, Object> result = serviceClient.callPluginService("/execute", request);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/configuration/{pluginName}")
    public ResponseEntity<Map<String, Object>> getPluginConfiguration(@PathVariable String pluginName) {
        Map<String, Object> request = Map.of("pluginName", pluginName);
        Map<String, Object> result = serviceClient.getFromPluginService("/configuration?pluginName=" + pluginName);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/configuration/{pluginName}")
    public ResponseEntity<Map<String, Object>> updatePluginConfiguration(@PathVariable String pluginName, @RequestBody Map<String, Object> configuration) {
        Map<String, Object> request = Map.of(
            "pluginName", pluginName,
            "configuration", configuration
        );
        Map<String, Object> result = serviceClient.callPluginService("/configuration", request);
        return ResponseEntity.ok(result);
    }
}
