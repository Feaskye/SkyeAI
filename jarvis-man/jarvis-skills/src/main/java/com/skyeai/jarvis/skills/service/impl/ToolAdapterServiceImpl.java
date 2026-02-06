package com.skyeai.jarvis.skills.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.skyeai.jarvis.skills.model.Skill;
import com.skyeai.jarvis.skills.model.SkillExecution;
import com.skyeai.jarvis.skills.service.SkillService;
import com.skyeai.jarvis.skills.service.ToolAdapterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工具适配器服务实现类
 */
@Slf4j
@Service
public class ToolAdapterServiceImpl implements ToolAdapterService {
    
    @Autowired
    private SkillService skillService;
    
    @Value("${function-call.execution.timeout:30}")
    private int executionTimeout;
    
    @Value("${function-call.execution.max-retries:3}")
    private int maxRetries;
    
    @Value("${function-call.execution.retry-delay:1000}")
    private int retryDelay;
    
    // 工具注册表，支持版本管理
    private final Map<String, Map<String, Skill>> toolsByVersion = new HashMap<>();
    // 默认版本
    private static final String DEFAULT_VERSION = "1.0";
    // 执行线程池
    private final ExecutorService executorService;
    // 工具执行结果缓存
    private final Map<String, SkillExecution> executionCache;
    // 缓存过期时间（毫秒）
    private static final long CACHE_EXPIRY = 5 * 60 * 1000;
    // 工具调用计数器（用于速率限制）
    private final Map<String, AtomicInteger> toolCallCounters;
    // 速率限制（每分钟调用次数）
    private static final int RATE_LIMIT_PER_MINUTE = 60;
    
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    public ToolAdapterServiceImpl() {
        // 初始化线程池
        this.executorService = Executors.newFixedThreadPool(10, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "tool-executor-" + counter.incrementAndGet());
            }
        });
        
        // 初始化缓存
        this.executionCache = new ConcurrentHashMap<>();
        // 初始化调用计数器
        this.toolCallCounters = new ConcurrentHashMap<>();
        
        // 启动缓存清理任务
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cleanupCache, 5, 5, TimeUnit.MINUTES);
        
        // 启动速率限制重置任务
        scheduler.scheduleAtFixedRate(this::resetRateLimits, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * 清理过期缓存
     */
    private void cleanupCache() {
        long now = System.currentTimeMillis();
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, SkillExecution> entry : executionCache.entrySet()) {
            SkillExecution execution = entry.getValue();
            if (execution.getEndTime() != null && now - execution.getEndTime().toEpochSecond(java.time.ZoneOffset.UTC) * 1000 > CACHE_EXPIRY) {
                toRemove.add(entry.getKey());
            }
        }
        for (String key : toRemove) {
            executionCache.remove(key);
        }
        log.info("Cleaned up {} expired cache entries", toRemove.size());
    }
    
    /**
     * 重置速率限制计数器
     */
    private void resetRateLimits() {
        toolCallCounters.clear();
        log.info("Reset rate limit counters");
    }
    
    /**
     * 检查速率限制
     * @param toolName 工具名称
     * @return 是否允许调用
     */
    private boolean checkRateLimit(String toolName) {
        AtomicInteger counter = toolCallCounters.computeIfAbsent(toolName, k -> new AtomicInteger(0));
        int current = counter.incrementAndGet();
        if (current > RATE_LIMIT_PER_MINUTE) {
            log.warn("Rate limit exceeded for tool: {}", toolName);
            return false;
        }
        return true;
    }
    
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
        String version = (String) toolDefinition.getOrDefault("version", DEFAULT_VERSION);
        
        if (!enabled) {
            log.info("Tool {} is disabled, skipping registration", name);
            return null;
        }
        
        Skill skill = new Skill();
        skill.setName(name);
        skill.setVersion(version);
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
        toolsByVersion.computeIfAbsent(version, k -> new HashMap<>()).put(name, registeredSkill);
        
        log.info("Registered tool as skill: {} v{} - {}", name, version, description);
        return registeredSkill;
    }
    
    /**
     * 注销工具
     */
    @Override
    public void unregisterTool(String toolName) {
        unregisterTool(toolName, DEFAULT_VERSION);
    }
    
    /**
     * 注销指定版本的工具
     */
    public void unregisterTool(String toolName, String version) {
        Map<String, Skill> versionMap = toolsByVersion.get(version);
        if (versionMap != null) {
            Skill skill = versionMap.get(toolName);
            if (skill != null) {
                skillService.unregisterSkill(skill.getId());
                versionMap.remove(toolName);
                log.info("Unregistered tool: {} v{}", toolName, version);
            }
        }
    }
    
    /**
     * 获取工具对应的Skill（默认版本）
     */
    @Override
    public Skill getToolByName(String toolName) {
        return getToolByName(toolName, DEFAULT_VERSION);
    }
    
    /**
     * 获取指定版本的工具
     */
    public Skill getToolByName(String toolName, String version) {
        Map<String, Skill> versionMap = toolsByVersion.get(version);
        if (versionMap != null) {
            return versionMap.get(toolName);
        }
        return null;
    }
    
    /**
     * 获取所有工具对应的Skill
     */
    @Override
    public List<Skill> getAllTools() {
        List<Skill> allTools = new ArrayList<>();
        for (Map<String, Skill> versionMap : toolsByVersion.values()) {
            allTools.addAll(versionMap.values());
        }
        return allTools;
    }
    
    /**
     * 执行工具
     */
    @Override
    public SkillExecution executeTool(String toolName, Map<String, Object> parameters) {
        return executeTool(toolName, DEFAULT_VERSION, parameters);
    }
    
    /**
     * 执行指定版本的工具
     */
    public SkillExecution executeTool(String toolName, String version, Map<String, Object> parameters) {
        // 检查速率限制
        if (!checkRateLimit(toolName)) {
            SkillExecution execution = new SkillExecution();
            execution.setStatus("ERROR");
            execution.setErrorMessage("Rate limit exceeded");
            execution.setEndTime(java.time.LocalDateTime.now());
            return execution;
        }
        
        // 生成缓存键
        String cacheKey = generateCacheKey(toolName, version, parameters);
        
        // 检查缓存
        if (executionCache.containsKey(cacheKey)) {
            log.info("Using cached result for tool: {}", toolName);
            return executionCache.get(cacheKey);
        }
        
        // 获取工具
        Skill skill = getToolByName(toolName, version);
        if (skill == null) {
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }
        
        // 执行工具（带超时和重试）
        SkillExecution execution = executeWithRetry(skill, parameters);
        
        // 缓存结果
        if (execution != null && "SUCCESS".equals(execution.getStatus())) {
            executionCache.put(cacheKey, execution);
        }
        
        return execution;
    }
    
    /**
     * 带重试的工具执行
     */
    private SkillExecution executeWithRetry(Skill skill, Map<String, Object> parameters) {
        int retries = 0;
        while (retries <= maxRetries) {
            try {
                // 执行工具（带超时）
                Future<SkillExecution> future = executorService.submit(() -> 
                        skillService.executeSkill(skill.getId(), parameters)
                );
                
                // 等待执行完成，设置超时
                return future.get(executionTimeout, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("Tool execution timed out: {}", skill.getName());
                retries++;
                if (retries <= maxRetries) {
                    log.info("Retrying tool execution: {} ({} of {})", skill.getName(), retries, maxRetries);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                log.error("Error executing tool: {}", skill.getName(), e);
                retries++;
                if (retries <= maxRetries) {
                    log.info("Retrying tool execution: {} ({} of {})", skill.getName(), retries, maxRetries);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        // 重试失败
        SkillExecution execution = new SkillExecution();
        execution.setStatus("ERROR");
            execution.setErrorMessage("Execution failed after " + maxRetries + " retries");
            execution.setEndTime(java.time.LocalDateTime.now());
        return execution;
    }
    
    /**
     * 生成缓存键
     */
    private String generateCacheKey(String toolName, String version, Map<String, Object> parameters) {
        try {
            return toolName + "_" + version + "_" + yamlMapper.writeValueAsString(parameters);
        } catch (IOException e) {
            return toolName + "_" + version + "_" + System.currentTimeMillis();
        }
    }
    
    /**
     * 批量执行工具
     */
    public List<SkillExecution> executeBatch(List<Map<String, Object>> tasks) {
        List<SkillExecution> results = new ArrayList<>();
        for (Map<String, Object> task : tasks) {
            String toolName = (String) task.get("toolName");
            String version = (String) task.getOrDefault("version", DEFAULT_VERSION);
            Map<String, Object> parameters = (Map<String, Object>) task.getOrDefault("parameters", new HashMap<>());
            
            try {
                SkillExecution execution = executeTool(toolName, version, parameters);
                results.add(execution);
            } catch (Exception e) {
                log.error("Error executing batch task for tool: {}", toolName, e);
                SkillExecution execution = new SkillExecution();
                execution.setStatus("ERROR");
                execution.setErrorMessage("Execution failed: " + e.getMessage());
                execution.setEndTime(java.time.LocalDateTime.now());
                results.add(execution);
            }
        }
        return results;
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
        
        // 添加搜索工具
        registerTool(Map.of(
                "name", "search",
                "description", "执行实时搜索，获取最新网络信息",
                "type", "java",
                "endpoint", "http://localhost:8080/api/tools/search",
                "enabled", true
        ));
        
        log.info("Loaded default tools: {}", getAllTools().stream().map(Skill::getName).toList());
        return getAllTools().size();
    }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
