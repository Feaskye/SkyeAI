package com.skyeai.jarvis.agent.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 内容安全过滤器
 * 对用户输入和LLM输出进行内容安全检查
 */
@Slf4j
@Component
public class ContentSecurityFilter {
    
    /**
     * 敏感词集合
     */
    private final Set<String> sensitiveWords = new HashSet<>();
    
    /**
     * 敏感词模式（正则表达式）
     */
    private final Pattern sensitivePattern = Pattern.compile(
        ".*(暴力|色情|赌博|诈骗|毒品|恐怖|政治|敏感).*",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 危险操作模式
     */
    private final Pattern dangerousPattern = Pattern.compile(
        ".*(删除|破坏|格式化|攻击|入侵|破解|盗取|窃取).*",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 初始化敏感词
     */
    public ContentSecurityFilter() {
        // 添加默认敏感词
        sensitiveWords.addAll(Arrays.asList(
            "敏感词1", "敏感词2", "敏感词3"
        ));
    }
    
    /**
     * 过滤用户输入
     * @param input 输入内容
     * @return 过滤结果
     */
    public SecurityResult filterInput(String input) {
        if (input == null || input.isBlank()) {
            return SecurityResult.safe();
        }
        
        // 敏感词检查
        for (String word : sensitiveWords) {
            if (input.contains(word)) {
                log.warn("输入包含敏感词: {}", word);
                return SecurityResult.blocked("包含敏感词: " + maskWord(word));
            }
        }
        
        // 敏感模式检查
        if (sensitivePattern.matcher(input).matches()) {
            log.warn("输入包含敏感内容");
            return SecurityResult.blocked("输入内容包含敏感信息");
        }
        
        // 危险操作检查
        if (dangerousPattern.matcher(input).matches()) {
            log.warn("输入包含危险操作");
            return SecurityResult.blocked("输入内容包含危险操作");
        }
        
        return SecurityResult.safe();
    }
    
    /**
     * 过滤LLM输出
     * @param output 输出内容
     * @return 过滤结果
     */
    public SecurityResult filterOutput(String output) {
        if (output == null || output.isBlank()) {
            return SecurityResult.safe();
        }
        
        // 敏感词检查
        for (String word : sensitiveWords) {
            if (output.contains(word)) {
                log.warn("输出包含敏感词: {}", word);
                return SecurityResult.blocked("输出包含敏感词: " + maskWord(word));
            }
        }
        
        // 敏感模式检查
        if (sensitivePattern.matcher(output).matches()) {
            log.warn("输出包含敏感内容");
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
     * 掩码敏感词
     */
    private String maskWord(String word) {
        if (word == null || word.length() <= 2) {
            return "***";
        }
        return word.charAt(0) + "***" + word.charAt(word.length() - 1);
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