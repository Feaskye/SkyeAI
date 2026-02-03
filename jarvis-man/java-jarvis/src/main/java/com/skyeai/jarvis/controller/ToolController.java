package com.skyeai.jarvis.controller;

import com.skyeai.jarvis.service.ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private static final Logger logger = Logger.getLogger(ToolController.class.getName());

    @Autowired
    private ServiceClient serviceClient;

    /**
     * 获取所有可用工具
     */
    @GetMapping
    public ResponseEntity<?> getAllTools() {
        try {
            logger.info("获取所有可用工具");
            Map<String, Object> response = serviceClient.getFromSkillsService("/tools/all");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("获取工具列表失败: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "获取工具列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * HTTP客户端工具
     */
    @PostMapping("/http")
    public ResponseEntity<?> httpClient(@RequestBody Map<String, Object> request) {
        try {
            logger.info("执行HTTP客户端工具: " + request);
            Map<String, Object> response = serviceClient.callSkillsService("/tools/http", request);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("HTTP客户端执行失败: " + e.getMessage());
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
    public ResponseEntity<?> jsonProcessor(@RequestBody Map<String, Object> request) {
        try {
            logger.info("执行JSON处理工具: " + request);
            Map<String, Object> response = serviceClient.callSkillsService("/tools/json", request);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("JSON处理执行失败: " + e.getMessage());
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
    public ResponseEntity<?> csvProcessor(@RequestBody Map<String, Object> request) {
        try {
            logger.info("执行CSV处理工具: " + request);
            Map<String, Object> response = serviceClient.callSkillsService("/tools/csv", request);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("CSV处理执行失败: " + e.getMessage());
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
    public ResponseEntity<?> imageAnalyzer(@RequestBody Map<String, Object> request) {
        try {
            logger.info("执行图像分析工具");
            Map<String, Object> response = serviceClient.callSkillsService("/tools/image-analysis", request);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("图像分析执行失败: " + e.getMessage());
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
    public ResponseEntity<?> speechProcessor(@RequestBody Map<String, Object> request) {
        try {
            logger.info("执行语音处理工具");
            Map<String, Object> response = serviceClient.callSkillsService("/tools/speech", request);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("语音处理执行失败: " + e.getMessage());
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
    public ResponseEntity<?> codeAnalyzer(@RequestBody Map<String, Object> request) {
        try {
            logger.info("执行代码分析工具");
            Map<String, Object> response = serviceClient.callSkillsService("/tools/code-analysis", request);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("代码分析执行失败: " + e.getMessage());
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
    public ResponseEntity<?> regexTool(@RequestBody Map<String, Object> request) {
        try {
            logger.info("执行正则表达式工具: " + request);
            Map<String, Object> response = serviceClient.callSkillsService("/tools/regex", request);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("正则表达式执行失败: " + e.getMessage());
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
    public ResponseEntity<?> calculator(@RequestBody Map<String, Object> request) {
        try {
            logger.info("执行计算器工具: " + request);
            Map<String, Object> response = serviceClient.callSkillsService("/tools/calculator", request);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("计算器执行失败: " + e.getMessage());
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
    public ResponseEntity<?> dateProcessor(@RequestBody Map<String, Object> request) {
        try {
            logger.info("执行日期处理工具: " + request);
            Map<String, Object> response = serviceClient.callSkillsService("/tools/date", request);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("日期处理执行失败: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "日期处理执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}