package com.skyeai.jarvis.agent.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 内容安全过滤器
 * 对用户输入和LLM输出进行内容安全检查
 * v10 改造：敏感词从 classpath:sensitive-words.txt 加载，缺失显式报错；
 * 移除过宽正则；新增 maskWord 脱敏方法。
 */
@Slf4j
@Component
public class ContentSecurityFilter {

    /**
     * 敏感词词库路径（classpath）
     */
    private static final String SENSITIVE_WORDS_LOCATION = "classpath:sensitive-words.txt";

    /**
     * 敏感词集合（从词库加载）
     */
    private final Set<String> sensitiveWords = new HashSet<>();

    /**
     * 敏感词模式（正则表达式）
     * v10：移除过宽的"政治""敏感"等词，仅保留明确有害类别
     */
    private final Pattern sensitivePattern = Pattern.compile(
        ".*(暴力|色情|赌博|诈骗|毒品|恐怖).*",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 危险操作模式
     */
    private final Pattern dangerousPattern = Pattern.compile(
        ".*(删除|破坏|格式化|攻击|入侵|破解|盗取|窃取).*",
        Pattern.CASE_INSENSITIVE
    );

    private final ResourceLoader resourceLoader;

    public ContentSecurityFilter(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 初始化敏感词词库
     * v10：从 classpath:sensitive-words.txt 加载，词库不存在时显式抛异常
     */
    @PostConstruct
    public void init() {
        Resource resource = resourceLoader.getResource(SENSITIVE_WORDS_LOCATION);
        if (!resource.exists()) {
            // 缺失配置显式报错，不静默返回空
            throw new IllegalStateException("敏感词词库不存在: " + SENSITIVE_WORDS_LOCATION
                    + "，请在 src/main/resources/ 下创建 sensitive-words.txt");
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // 跳过空行与注释行（# 开头）
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                sensitiveWords.add(trimmed);
            }
        } catch (IOException e) {
            throw new IllegalStateException("加载敏感词词库失败: " + SENSITIVE_WORDS_LOCATION, e);
        }
        log.info("{\"event\":\"sensitive_words_loaded\",\"count\":{}}", sensitiveWords.size());
    }

    /**
     * 过滤用户输入
     * @param input 输入内容
     * @return 过滤结果（包含 isBlocked 与 reason）
     */
    public SecurityResult filterInput(String input) {
        if (input == null || input.isBlank()) {
            return SecurityResult.safe();
        }

        // 敏感词检查
        for (String word : sensitiveWords) {
            if (input.contains(word)) {
                log.warn("{\"event\":\"input_sensitive_hit\",\"word\":\"{}\"}", maskWord(word));
                return SecurityResult.blocked("输入包含敏感词: " + maskWord(word));
            }
        }

        // 敏感模式检查
        if (sensitivePattern.matcher(input).matches()) {
            log.warn("{\"event\":\"input_sensitive_pattern_hit\"}");
            return SecurityResult.blocked("输入内容包含敏感信息");
        }

        // 危险操作检查
        if (dangerousPattern.matcher(input).matches()) {
            log.warn("{\"event\":\"input_dangerous_hit\"}");
            return SecurityResult.blocked("输入内容包含危险操作");
        }

        return SecurityResult.safe();
    }

    /**
     * 过滤LLM输出
     * @param output 输出内容
     * @return 过滤结果（包含 isBlocked 与 reason）
     */
    public SecurityResult filterOutput(String output) {
        if (output == null || output.isBlank()) {
            return SecurityResult.safe();
        }

        // 敏感词检查
        for (String word : sensitiveWords) {
            if (output.contains(word)) {
                log.warn("{\"event\":\"output_sensitive_hit\",\"word\":\"{}\"}", maskWord(word));
                return SecurityResult.blocked("输出包含敏感词: " + maskWord(word));
            }
        }

        // 敏感模式检查
        if (sensitivePattern.matcher(output).matches()) {
            log.warn("{\"event\":\"output_sensitive_pattern_hit\"}");
            return SecurityResult.blocked("输出内容包含敏感信息");
        }

        return SecurityResult.safe();
    }

    /**
     * 添加敏感词
     * @param word 敏感词
     */
    public void addSensitiveWord(String word) {
        sensitiveWords.add(word);
    }

    /**
     * 移除敏感词
     * @param word 敏感词
     */
    public void removeSensitiveWord(String word) {
        sensitiveWords.remove(word);
    }

    /**
     * 脱敏处理：将文本中所有敏感词替换为 ***
     * @param text 原始文本
     * @return 脱敏后的文本
     */
    public String maskWord(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String masked = text;
        for (String word : sensitiveWords) {
            if (word != null && !word.isBlank()) {
                masked = masked.replace(word, "***");
            }
        }
        return masked;
    }

    /**
     * 安全结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class SecurityResult {
        private boolean safe;
        private String reason;

        public static SecurityResult safe() {
            return new SecurityResult(true, null);
        }

        public static SecurityResult blocked(String reason) {
            return new SecurityResult(false, reason);
        }

        public boolean isBlocked() {
            return !safe;
        }
    }
}
