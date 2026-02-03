package com.skyeai.jarvis.skills.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.skyeai.jarvis.skills.model.Skill;
import com.skyeai.jarvis.skills.model.SkillExecution;
import com.skyeai.jarvis.skills.service.SkillService;
import com.skyeai.jarvis.skills.service.ToolAdapterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具适配器服务实现类
 */
@Slf4j
@Service
public class ToolAdapterServiceImpl implements ToolAdapterService {
    
    @Autowired
    private SkillService skillService;
    
    // 工具注册表
    private final Map<String, Skill> tools = new HashMap<>();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    /**
     * 注册工具为Skill
     */
    @Override
    public Skill registerTool(Map<String, Object> toolDefinition) {
        String name = (String) toolDefinition.get("name");
        String description = (String) toolDefinition.get("description");
        String type = (String) toolDefinition.get("type");
        String endpoint = (String) toolDefinition.get("endpoint");
        boolean enabled = (Boolean) toolDefinition.getOrDefault("enabled", true);
        
        if (!enabled) {
            log.info("Tool {} is disabled, skipping registration", name);
            return null;
        }
        
        Skill skill = new Skill();
        skill.setName(name);
        skill.setVersion("1.0");
        skill.setDescription(description);
        skill.setType(type);
        skill.setStatus("ACTIVE");
        
        // 设置输入输出模式
        Map<String, Object> inputSchema = new HashMap<>();
        inputSchema.put("type", "object");
        inputSchema.put("properties", toolDefinition.getOrDefault("parameters", new HashMap<>()));
        
        Map<String, Object> outputSchema = new HashMap<>();
        outputSchema.put("type", "object");
        outputSchema.put("properties", Map.of(
                "status", Map.of("type", "string"),
                "message", Map.of("type", "string"),
                "output", Map.of("type", "object")
        ));
        
        try {
            skill.setInputSchema(yamlMapper.writeValueAsString(inputSchema));
            skill.setOutputSchema(yamlMapper.writeValueAsString(outputSchema));
        } catch (IOException e) {
            log.error("Error writing schema", e);
        }
        
        // 设置配置信息
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", endpoint);
        
        try {
            skill.setConfiguration(yamlMapper.writeValueAsString(config));
        } catch (IOException e) {
            log.error("Error writing configuration", e);
        }
        
        // 注册Skill
        Skill registeredSkill = skillService.registerSkill(skill);
        tools.put(name, registeredSkill);
        
        log.info("Registered tool as skill: {} - {}", name, description);
        return registeredSkill;
    }
    
    /**
     * 注销工具
     */
    @Override
    public void unregisterTool(String toolName) {
        Skill skill = tools.get(toolName);
        if (skill != null) {
            skillService.unregisterSkill(skill.getId());
            tools.remove(toolName);
            log.info("Unregistered tool: {}", toolName);
        }
    }
    
    /**
     * 获取工具对应的Skill
     */
    @Override
    public Skill getToolByName(String toolName) {
        return tools.get(toolName);
    }
    
    /**
     * 获取所有工具对应的Skill
     */
    @Override
    public List<Skill> getAllTools() {
        return new ArrayList<>(tools.values());
    }
    
    /**
     * 执行工具
     */
    @Override
    public SkillExecution executeTool(String toolName, Map<String, Object> parameters) {
        Skill skill = tools.get(toolName);
        if (skill == null) {
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }
        
        return skillService.executeSkill(skill.getId(), parameters);
    }
    
    /**
     * 从YAML文件加载工具配置
     */
    @Override
    public int loadToolsFromYaml(String yamlPath) {
        try {
            File file = new File(yamlPath);
            if (file.exists()) {
                Map<String, Object>[] toolArray = yamlMapper.readValue(file, Map[].class);
                for (Map<String, Object> tool : toolArray) {
                    registerTool(tool);
                }
                log.info("Loaded tools from YAML file: " + yamlPath);
                return toolArray.length;
            } else {
                log.info("YAML file not found: " + yamlPath + ", loading default tools");
                return loadDefaultTools();
            }
        } catch (IOException e) {
            log.error("Error loading tools from YAML", e);
            return 0;
        }
    }
    
    /**
     * 加载默认工具配置
     */
    @Override
    public int loadDefaultTools() {
        // 系统工具
        registerTool(Map.of(
                "name", "browser",
                "description", "控制无头浏览器，抓取/操作网页",
                "type", "go",
                "endpoint", "http://localhost:8081/api/browser",
                "enabled", true
        ));
        
        registerTool(Map.of(
                "name", "sandbox",
                "description", "在安全沙箱中执行代码",
                "type", "go",
                "endpoint", "http://localhost:8081/api/sandbox",
                "enabled", true
        ));
        
        // 网络工具
        registerTool(Map.of(
                "name", "http_client",
                "description", "发送HTTP请求，获取网页内容或API数据",
                "type", "java",
                "endpoint", "http://localhost:8080/api/tools/http",
                "enabled", true
        ));
        
        // 数据处理工具
        registerTool(Map.of(
                "name", "json_processor",
                "description", "处理JSON数据，包括解析、验证和转换",
                "type", "java",
                "endpoint", "http://localhost:8080/api/tools/json",
                "enabled", true
        ));
        
        registerTool(Map.of(
                "name", "csv_processor",
                "description", "处理CSV文件，包括读取、写入和转换",
                "type", "java",
                "endpoint", "http://localhost:8080/api/tools/csv",
                "enabled", true
        ));
        
        // AI增强工具
        registerTool(Map.of(
                "name", "image_analyzer",
                "description", "分析图像内容，识别物体、场景和文本",
                "type", "java",
                "endpoint", "http://localhost:8080/api/tools/image-analysis",
                "enabled", true
        ));
        
        registerTool(Map.of(
                "name", "speech_processor",
                "description", "处理语音数据，包括语音识别和语音合成",
                "type", "java",
                "endpoint", "http://localhost:8080/api/tools/speech",
                "enabled", true
        ));
        
        // 开发工具
        registerTool(Map.of(
                "name", "code_analyzer",
                "description", "分析代码，包括语法检查、风格检查和复杂度分析",
                "type", "java",
                "endpoint", "http://localhost:8080/api/tools/code-analysis",
                "enabled", true
        ));
        
        registerTool(Map.of(
                "name", "regex_tool",
                "description", "使用正则表达式进行文本匹配和提取",
                "type", "java",
                "endpoint", "http://localhost:8080/api/tools/regex",
                "enabled", true
        ));
        
        // 实用工具
        registerTool(Map.of(
                "name", "calculator",
                "description", "执行数学计算，包括基本运算和复杂函数",
                "type", "java",
                "endpoint", "http://localhost:8080/api/tools/calculator",
                "enabled", true
        ));
        
        registerTool(Map.of(
                "name", "date_processor",
                "description", "处理日期和时间，包括格式化、计算和转换",
                "type", "java",
                "endpoint", "http://localhost:8080/api/tools/date",
                "enabled", true
        ));
        
        log.info("Loaded default tools: {}", tools.keySet());
        return tools.size();
    }
}
