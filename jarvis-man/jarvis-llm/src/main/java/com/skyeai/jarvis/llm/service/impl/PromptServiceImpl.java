package com.skyeai.jarvis.llm.service.impl;

import com.skyeai.jarvis.llm.service.PromptService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词服务实现
 * v10 改造：内存 HashMap 硬编码改为基于 Spring AI PromptTemplate 加载 .st 文件
 * 支持 file / nacos / legacy 三种来源，通过 jarvis.prompt.source 切换
 * - file：从 classpath:prompts/ 加载 .st 文件（模板不存在显式报错）
 * - nacos：从 Nacos 配置中心加载（Phase 5+ 实现热更新，当前为骨架）
 * - legacy：保留原内存 HashMap 硬编码方式（兼容老配置）
 */
@Slf4j
@Service
public class PromptServiceImpl implements PromptService {

    /**
     * 提示词来源：file | nacos | legacy
     */
    @Value("${jarvis.prompt.source:legacy}")
    private String promptSource;

    /**
     * 默认系统提示词类型（file 模式下使用）
     */
    @Value("${jarvis.prompt.default-system-prompt:jarvis}")
    private String defaultSystemPrompt;

    private final ResourceLoader resourceLoader;

    /**
     * file 模式：系统提示词模板
     */
    private final Map<String, PromptTemplate> systemTemplates = new ConcurrentHashMap<>();

    /**
     * file 模式：用户提示词模板
     */
    private final Map<String, PromptTemplate> userTemplates = new ConcurrentHashMap<>();

    /**
     * legacy 模式：系统提示词（内存硬编码）
     */
    private final Map<String, String> systemPrompts = new HashMap<>();

    /**
     * legacy 模式：用户提示模板（内存硬编码）
     */
    private final Map<String, String> userPromptTemplates = new HashMap<>();

    public PromptServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 初始化：根据来源加载提示词
     */
    @PostConstruct
    public void init() {
        initLegacy();
        if ("file".equalsIgnoreCase(promptSource)) {
            loadFileTemplates();
        } else if ("nacos".equalsIgnoreCase(promptSource)) {
            loadNacosTemplates();
        } else {
            log.info("{\"event\":\"prompt_init\",\"source\":\"legacy\"}");
        }
    }

    /**
     * 初始化 legacy 模式硬编码提示词
     */
    private void initLegacy() {
        // 初始化系统提示
        systemPrompts.put("general", "You are a helpful assistant. Answer questions clearly and concisely.");
        systemPrompts.put("expert", "You are an expert in the requested field. Provide detailed and accurate information.");
        systemPrompts.put("creative", "You are a creative thinker. Generate innovative and original ideas.");
        systemPrompts.put("technical", "You are a technical expert. Provide precise and technical explanations.");
        systemPrompts.put("friendly", "You are a friendly and approachable assistant. Make your responses warm and engaging.");

        // 初始化用户提示模板
        userPromptTemplates.put("question", "I have a question about {topic}. {details}");
        userPromptTemplates.put("summarize", "Please summarize the following content: {content}");
        userPromptTemplates.put("generate", "Please generate {type} about {topic}. {requirements}");
        userPromptTemplates.put("analyze", "Please analyze {subject}. {context}");
        userPromptTemplates.put("solve", "Please help me solve this problem: {problem}. {constraints}");
    }

    /**
     * file 模式：从 classpath:prompts/ 加载 .st 模板文件
     * 模板不存在时显式抛异常
     */
    private void loadFileTemplates() {
        // 系统提示词
        loadSystemTemplate("jarvis", "classpath:prompts/system/jarvis.st");
        loadSystemTemplate("expert", "classpath:prompts/system/expert.st");
        loadSystemTemplate("creative", "classpath:prompts/system/creative.st");
        loadSystemTemplate("technical", "classpath:prompts/system/technical.st");
        // general 作为 jarvis 的别名，保持向后兼容
        systemTemplates.put("general", systemTemplates.get("jarvis"));

        // 用户提示词
        loadUserTemplate("rag-enhanced", "classpath:prompts/user/rag-enhanced.st");
        loadUserTemplate("memory-enhanced", "classpath:prompts/user/memory-enhanced.st");
        loadUserTemplate("tool-summary", "classpath:prompts/user/tool-summary.st");

        log.info("{\"event\":\"prompt_init\",\"source\":\"file\",\"system_count\":{},\"user_count\":{}}",
                systemTemplates.size(), userTemplates.size());
    }

    /**
     * nacos 模式：从 Nacos 配置中心加载提示词（支持热更新）
     * TODO Phase 5+：接入 spring-ai-alibaba-starter-config-nacos 实现，当前为骨架
     */
    private void loadNacosTemplates() {
        // 缺失实现时显式报错，不静默返回空
        throw new IllegalStateException(
                "nacos 提示词来源尚未实现（Phase 5+ 接入 spring-ai-alibaba-starter-config-nacos），"
                        + "请将 jarvis.prompt.source 改为 file 或 legacy");
    }

    /**
     * 加载系统提示词模板
     */
    private void loadSystemTemplate(String name, String location) {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            // 缺失配置显式报错
            throw new IllegalStateException("系统提示词模板不存在: " + location);
        }
        try {
            systemTemplates.put(name, new PromptTemplate(resource));
        } catch (Exception e) {
            throw new IllegalStateException("加载系统提示词模板失败: " + location, e);
        }
    }

    /**
     * 加载用户提示词模板
     */
    private void loadUserTemplate(String name, String location) {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            // 缺失配置显式报错
            throw new IllegalStateException("用户提示词模板不存在: " + location);
        }
        try {
            userTemplates.put(name, new PromptTemplate(resource));
        } catch (Exception e) {
            throw new IllegalStateException("加载用户提示词模板失败: " + location, e);
        }
    }

    @Override
    public String getSystemPrompt(String promptType) {
        if ("file".equalsIgnoreCase(promptSource)) {
            PromptTemplate template = systemTemplates.get(promptType);
            if (template == null) {
                template = systemTemplates.get(defaultSystemPrompt);
            }
            if (template == null) {
                // 缺失配置显式报错
                throw new IllegalArgumentException("未知的提示词类型: " + promptType);
            }
            return template.render(Map.of());
        }
        // legacy 模式
        return systemPrompts.getOrDefault(promptType, systemPrompts.get("general"));
    }

    @Override
    public String getSystemPrompt(String promptType, Map<String, Object> parameters) {
        String basePrompt = getSystemPrompt(promptType);
        if ("file".equalsIgnoreCase(promptSource)) {
            PromptTemplate template = systemTemplates.get(promptType);
            if (template == null) {
                template = systemTemplates.get(defaultSystemPrompt);
            }
            if (template == null) {
                throw new IllegalArgumentException("未知的提示词类型: " + promptType);
            }
            return template.render(parameters != null ? parameters : Map.of());
        }
        // legacy 模式：使用自实现渲染
        return renderTemplate(basePrompt, parameters);
    }

    @Override
    public String getUserPromptTemplate(String promptType) {
        if ("file".equalsIgnoreCase(promptSource)) {
            PromptTemplate template = userTemplates.get(promptType);
            if (template == null) {
                // 缺失配置显式报错
                throw new IllegalArgumentException("未知的用户提示词模板: " + promptType);
            }
            return template.render(Map.of());
        }
        // legacy 模式
        return userPromptTemplates.getOrDefault(promptType, "{content}");
    }

    @Override
    public String renderUserPrompt(String promptType, Map<String, Object> parameters) {
        if ("file".equalsIgnoreCase(promptSource)) {
            PromptTemplate template = userTemplates.get(promptType);
            if (template == null) {
                // 缺失配置显式报错
                throw new IllegalArgumentException("未知的用户提示词模板: " + promptType);
            }
            return template.render(parameters != null ? parameters : Map.of());
        }
        // legacy 模式：使用自实现渲染
        String template = userPromptTemplates.getOrDefault(promptType, "{content}");
        return renderTemplate(template, parameters);
    }

    @Override
    public String optimizePrompt(String prompt) {
        // 简单的提示优化逻辑
        String optimized = prompt.trim();

        // 确保提示清晰明确
        if (!optimized.endsWith(".") && !optimized.endsWith("?") && !optimized.endsWith("!")) {
            optimized += ".";
        }

        // 移除重复的空格
        optimized = optimized.replaceAll("\\s+", " ");

        return optimized;
    }

    @Override
    public Map<String, Object> analyzePromptEffectiveness(String prompt, String response) {
        Map<String, Object> analysis = new HashMap<>();

        // 分析提示长度
        analysis.put("promptLength", prompt.length());

        // 分析响应长度
        analysis.put("responseLength", response.length());

        // 分析响应相关性（简单实现）
        int relevanceScore = calculateRelevance(prompt, response);
        analysis.put("relevanceScore", relevanceScore);

        // 分析响应质量（简单实现）
        int qualityScore = calculateQuality(response);
        analysis.put("qualityScore", qualityScore);

        return analysis;
    }

    /**
     * legacy 模式模板渲染：{variable} 占位符替换
     */
    private String renderTemplate(String template, Map<String, Object> parameters) {
        if (template == null) {
            return "";
        }
        String rendered = template;
        if (parameters == null || parameters.isEmpty()) {
            return rendered;
        }
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Object value = parameters.get(placeholder);
            if (value != null) {
                rendered = rendered.replace("{" + placeholder + "}", value.toString());
            }
        }

        return rendered;
    }

    private int calculateRelevance(String prompt, String response) {
        // 简单的相关性计算
        int score = 0;
        String[] promptWords = prompt.toLowerCase().split("\\s+");
        String[] responseWords = response.toLowerCase().split("\\s+");

        for (String promptWord : promptWords) {
            if (promptWord.length() > 3) {
                for (String responseWord : responseWords) {
                    if (responseWord.contains(promptWord) || promptWord.contains(responseWord)) {
                        score++;
                        break;
                    }
                }
            }
        }

        return Math.min(10, score);
    }

    private int calculateQuality(String response) {
        // 简单的质量计算
        int score = 0;

        // 检查响应长度
        if (response.length() > 50) {
            score += 2;
        }

        // 检查响应是否包含完整句子
        if (response.contains(". ")) {
            score += 2;
        }

        // 检查响应是否包含具体信息
        if (response.contains("because") || response.contains("since") || response.contains("due to")) {
            score += 2;
        }

        // 检查响应是否有条理
        if (response.contains("first") || response.contains("second") || response.contains("third") ||
            response.contains("1.") || response.contains("2.") || response.contains("3.")) {
            score += 2;
        }

        // 检查响应是否没有语法错误（简单检查）
        if (!response.contains("  ") && !response.contains(". .")) {
            score += 2;
        }

        return Math.min(10, score);
    }
}
