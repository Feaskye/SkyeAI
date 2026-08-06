package com.skyeai.jarvis.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 意图识别器
 * 根据用户输入识别对话意图
 */
@Slf4j
@Component
public class IntentRecognizer {
    
    /**
     * RAG相关关键词
     */
    private static final List<String> RAG_KEYWORDS = Arrays.asList(
        "查询", "查找", "搜索", "资料", "文档", "知识", "了解", "是什么", "什么是", "定义"
    );
    
    /**
     * 工具调用相关关键词
     */
    private static final List<String> TOOL_KEYWORDS = Arrays.asList(
        "天气", "股票", "时间", "日期", "闹钟", "提醒", "发送", "打开", "关闭", "启动"
    );
    
    /**
     * 子代理相关关键词
     */
    private static final List<String> SUB_AGENT_KEYWORDS = Arrays.asList(
        "专家", "分析师", "顾问", "助手", "帮我", "任务", "规划", "分解"
    );
    
    /**
     * 总结相关关键词
     */
    private static final List<String> SUMMARY_KEYWORDS = Arrays.asList(
        "总结", "摘要", "回顾", "概括", "要点"
    );
    
    /**
     * 识别用户意图
     * @param userInput 用户输入
     * @return 识别出的意图
     */
    public Intent recognize(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return Intent.UNKNOWN;
        }
        
        String lowerInput = userInput.toLowerCase();
        
        // 检查总结意图（优先级最高）
        for (String keyword : SUMMARY_KEYWORDS) {
            if (lowerInput.contains(keyword)) {
                log.debug("识别到总结意图: {}", userInput);
                return Intent.SUMMARY;
            }
        }
        
        // 检查子代理意图
        for (String keyword : SUB_AGENT_KEYWORDS) {
            if (lowerInput.contains(keyword)) {
                log.debug("识别到子代理意图: {}", userInput);
                return Intent.SUB_AGENT;
            }
        }
        
        // 检查工具调用意图
        for (String keyword : TOOL_KEYWORDS) {
            if (lowerInput.contains(keyword)) {
                log.debug("识别到工具调用意图: {}", userInput);
                return Intent.TOOL_CALL;
            }
        }
        
        // 检查RAG意图
        for (String keyword : RAG_KEYWORDS) {
            if (lowerInput.contains(keyword)) {
                log.debug("识别到RAG检索意图: {}", userInput);
                return Intent.RAG;
            }
        }
        
        // 默认返回普通对话意图
        log.debug("识别到普通对话意图: {}", userInput);
        return Intent.CHAT;
    }
    
    /**
     * 获取意图描述
     * @param intent 意图
     * @return 意图描述
     */
    public String getIntentDescription(Intent intent) {
        return switch (intent) {
            case CHAT -> "普通对话";
            case RAG -> "知识检索";
            case TOOL_CALL -> "工具调用";
            case SUB_AGENT -> "子代理协作";
            case SUMMARY -> "对话总结";
            case UNKNOWN -> "未知意图";
        };
    }
}