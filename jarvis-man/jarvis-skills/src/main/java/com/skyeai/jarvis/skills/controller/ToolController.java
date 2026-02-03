package com.skyeai.jarvis.skills.controller;

import com.skyeai.jarvis.skills.model.SkillExecution;
import com.skyeai.jarvis.skills.service.SkillService;
import com.skyeai.jarvis.skills.service.ToolAdapterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具控制器，用于处理工具相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/api/skills/tools")
public class ToolController {

    @Autowired
    private ToolAdapterService toolAdapterService;

    @Autowired
    private SkillService skillService;

    /**
     * 获取所有可用工具
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllTools() {
        try {
            log.info("获取所有可用工具");
            Map<String, Object> response = new HashMap<>();
            response.put("tools", toolAdapterService.getAllTools());
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取工具列表失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "获取工具列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 从YAML文件加载工具配置
     */
    @PostMapping("/load-from-yaml")
    public ResponseEntity<Map<String, Object>> loadToolsFromYaml(@RequestBody Map<String, String> request) {
        try {
            String yamlPath = request.get("yamlPath");
            int count = toolAdapterService.loadToolsFromYaml(yamlPath);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "成功加载 " + count + " 个工具");
            response.put("tools", toolAdapterService.getAllTools());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("加载工具配置失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "加载工具配置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * HTTP客户端工具
     */
    @PostMapping("/http")
    public ResponseEntity<Map<String, Object>> httpClient(@RequestBody Map<String, Object> request) {
        try {
            log.info("执行HTTP客户端工具: {}", request);
            // 这里实现HTTP请求逻辑或调用相应的Skill
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "HTTP请求执行成功");
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("HTTP客户端执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "HTTP客户端执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * JSON处理工具
     */
    @PostMapping("/json")
    public ResponseEntity<Map<String, Object>> jsonProcessor(@RequestBody Map<String, Object> request) {
        try {
            log.info("执行JSON处理工具: {}", request);
            // 这里实现JSON处理逻辑或调用相应的Skill
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "JSON处理执行成功");
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("JSON处理执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "JSON处理执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * CSV处理工具
     */
    @PostMapping("/csv")
    public ResponseEntity<Map<String, Object>> csvProcessor(@RequestBody Map<String, Object> request) {
        try {
            log.info("执行CSV处理工具: {}", request);
            // 这里实现CSV处理逻辑或调用相应的Skill
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CSV处理执行成功");
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("CSV处理执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "CSV处理执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 图像分析工具
     */
    @PostMapping("/image-analysis")
    public ResponseEntity<Map<String, Object>> imageAnalyzer(@RequestBody Map<String, Object> request) {
        try {
            log.info("执行图像分析工具");
            // 这里实现图像分析逻辑或调用相应的Skill
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "图像分析执行成功");
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("图像分析执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "图像分析执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 语音处理工具
     */
    @PostMapping("/speech")
    public ResponseEntity<Map<String, Object>> speechProcessor(@RequestBody Map<String, Object> request) {
        try {
            log.info("执行语音处理工具");
            // 这里实现语音处理逻辑或调用相应的Skill
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "语音处理执行成功");
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("语音处理执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "语音处理执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 代码分析工具
     */
    @PostMapping("/code-analysis")
    public ResponseEntity<Map<String, Object>> codeAnalyzer(@RequestBody Map<String, Object> request) {
        try {
            log.info("执行代码分析工具");
            // 这里实现代码分析逻辑或调用相应的Skill
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "代码分析执行成功");
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("代码分析执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "代码分析执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 正则表达式工具
     */
    @PostMapping("/regex")
    public ResponseEntity<Map<String, Object>> regexTool(@RequestBody Map<String, Object> request) {
        try {
            log.info("执行正则表达式工具: {}", request);
            // 这里实现正则表达式处理逻辑或调用相应的Skill
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "正则表达式执行成功");
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("正则表达式执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "正则表达式执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 计算器工具
     */
    @PostMapping("/calculator")
    public ResponseEntity<Map<String, Object>> calculator(@RequestBody Map<String, Object> request) {
        try {
            log.info("执行计算器工具: {}", request);
            // 这里实现计算逻辑或调用相应的Skill
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "计算执行成功");
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("计算器执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "计算器执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 日期处理工具
     */
    @PostMapping("/date")
    public ResponseEntity<Map<String, Object>> dateProcessor(@RequestBody Map<String, Object> request) {
        try {
            log.info("执行日期处理工具: {}", request);
            // 这里实现日期处理逻辑或调用相应的Skill
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "日期处理执行成功");
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("日期处理执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "日期处理执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
