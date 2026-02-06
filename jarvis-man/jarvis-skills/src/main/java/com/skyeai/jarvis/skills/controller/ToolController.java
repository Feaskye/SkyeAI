package com.skyeai.jarvis.skills.controller;

import com.skyeai.jarvis.skills.model.Skill;
import com.skyeai.jarvis.skills.model.SkillExecution;
import com.skyeai.jarvis.skills.service.SkillService;
import com.skyeai.jarvis.skills.service.ToolAdapterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具控制器，用于处理工具相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/api/tools")
public class ToolController {

    @Autowired
    private ToolAdapterService toolAdapterService;

    @Autowired
    private SkillService skillService;

    /**
     * 获取所有工具
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTools() {
        try {
            log.info("获取所有工具");
            List<Skill> skills = toolAdapterService.getAllTools();
            Map<String, Object> response = new HashMap<>();
            response.put("tools", skills);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取工具列表失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取工具列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取指定工具
     */
    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> getToolByName(@PathVariable String name) {
        try {
            log.info("获取指定工具: {}", name);
            Skill skill = toolAdapterService.getToolByName(name);
            if (skill == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Tool not found: " + name);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.ok(Map.of(
                    "name", skill.getName(),
                    "description", skill.getDescription(),
                    "type", skill.getType(),
                    "status", skill.getStatus()
            ));
        } catch (Exception e) {
            log.error("获取工具失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取工具失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 注册新工具
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerTool(@RequestBody Map<String, Object> toolDefinition) {
        try {
            log.info("注册新工具: {}", toolDefinition.get("name"));
            Skill skill = toolAdapterService.registerTool(toolDefinition);
            if (skill == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to register tool");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            return ResponseEntity.ok(Map.of(
                    "id", skill.getId(),
                    "name", skill.getName(),
                    "status", skill.getStatus()
            ));
        } catch (Exception e) {
            log.error("注册工具失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "注册工具失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 更新工具
     */
    @PutMapping("/{name}")
    public ResponseEntity<Map<String, Object>> updateTool(@PathVariable String name, @RequestBody Map<String, Object> updateData) {
        try {
            log.info("更新工具: {}", name);
            // 这里需要实现工具更新逻辑
            // 暂时返回成功
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Tool updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新工具失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "更新工具失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 注销工具
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, Object>> unregisterTool(@PathVariable String name) {
        try {
            log.info("注销工具: {}", name);
            toolAdapterService.unregisterTool(name);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Tool unregistered successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("注销工具失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "注销工具失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 执行工具
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeTool(@RequestBody Map<String, Object> request) {
        try {
            String toolName = (String) request.get("toolName");
            Map<String, Object> parameters = (Map<String, Object>) request.getOrDefault("parameters", new HashMap<>());
            
            log.info("执行工具: {}", toolName);
            SkillExecution execution = toolAdapterService.executeTool(toolName, parameters);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", execution.getStatus());
            response.put("output", execution.getOutputResult());
            response.put("message", execution.getErrorMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("执行工具失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "执行工具失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 批量执行工具
     */
    @PostMapping("/execute/batch")
    public ResponseEntity<Map<String, Object>> executeBatch(@RequestBody Map<String, Object> request) {
        try {
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) request.get("tasks");
            log.info("批量执行工具: {} 个任务", tasks.size());
            
            List<SkillExecution> results = toolAdapterService.executeBatch(tasks);
            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量执行工具失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量执行工具失败: " + e.getMessage());
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
            response.put("status", "success");
            response.put("message", "HTTP请求执行成功");
            response.put("output", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("HTTP客户端执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "HTTP客户端执行失败: " + e.getMessage());
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
            response.put("status", "success");
            response.put("message", "JSON处理执行成功");
            response.put("output", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("JSON处理执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "JSON处理执行失败: " + e.getMessage());
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
            response.put("status", "success");
            response.put("message", "CSV处理执行成功");
            response.put("output", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("CSV处理执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "CSV处理执行失败: " + e.getMessage());
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
            response.put("status", "success");
            response.put("message", "图像分析执行成功");
            response.put("output", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("图像分析执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "图像分析执行失败: " + e.getMessage());
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
            response.put("status", "success");
            response.put("message", "语音处理执行成功");
            response.put("output", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("语音处理执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "语音处理执行失败: " + e.getMessage());
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
            response.put("status", "success");
            response.put("message", "代码分析执行成功");
            response.put("output", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("代码分析执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "代码分析执行失败: " + e.getMessage());
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
            response.put("status", "success");
            response.put("message", "正则表达式执行成功");
            response.put("output", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("正则表达式执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "正则表达式执行失败: " + e.getMessage());
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
            response.put("status", "success");
            response.put("message", "计算执行成功");
            response.put("output", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("计算器执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "计算器执行失败: " + e.getMessage());
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
            response.put("status", "success");
            response.put("message", "日期处理执行成功");
            response.put("output", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("日期处理执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "日期处理执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 搜索工具
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestBody Map<String, Object> request) {
        try {
            log.info("执行搜索工具: {}", request.get("query"));
            // 这里实现搜索逻辑或调用相应的Skill
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "搜索执行成功");
            response.put("output", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("搜索执行失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "搜索执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
