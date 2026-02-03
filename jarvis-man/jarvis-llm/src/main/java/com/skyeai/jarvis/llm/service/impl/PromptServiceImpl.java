package com.skyeai.jarvis.llm.service.impl;

import com.skyeai.jarvis.llm.service.PromptService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PromptServiceImpl implements PromptService {

    private final Map<String, String> systemPrompts = new HashMap<>();
    private final Map<String, String> userPromptTemplates = new HashMap<>();

    public PromptServiceImpl() {
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

    @Override
    public String getSystemPrompt(String promptType) {
        return systemPrompts.getOrDefault(promptType, systemPrompts.get("general"));
    }

    @Override
    public String getSystemPrompt(String promptType, Map<String, Object> parameters) {
        String basePrompt = getSystemPrompt(promptType);
        return renderTemplate(basePrompt, parameters);
    }

    @Override
    public String getUserPromptTemplate(String promptType) {
        return userPromptTemplates.getOrDefault(promptType, "{content}");
    }

    @Override
    public String renderUserPrompt(String promptType, Map<String, Object> parameters) {
        String template = getUserPromptTemplate(promptType);
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

    private String renderTemplate(String template, Map<String, Object> parameters) {
        String rendered = template;
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
