package com.skyeai.jarvis.service;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 意图解析服务，用于分析用户输入的意图
 */
@Service
public class IntentService {

    // 日程管理相关的正则表达式
    private static final Pattern SCHEDULE_PATTERN = Pattern.compile(
            "(?:(?:明天|后天|大后天|下周一|下周二|下周三|下周四|下周五|下周六|下周日|\\d{4}-\\d{2}-\\d{2}|\\d{1,2}月\\d{1,2}日)\\s*)" +
            "(?:(?:上午|下午|晚上)?\\s*" +
            "(?:(?:\\d{1,2}:\\d{2})|(?:\\d{1,2}点(?:\\d{1,2}分)?)|(?:\\d{1,2}时(?:\\d{1,2}分)?))\\s*)?" +
            "(?:(?:开会|会议|约会|提醒|安排|预约|日程|设置|培训)\\s*)" +
            "(.+)?",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 解析用户输入的意图
     * @param input 用户输入文本
     * @return 意图类型
     */
    public IntentResult parseIntent(String input) {
        IntentResult result = new IntentResult();
        result.setInput(input);
        result.setIntent(IntentType.GENERAL);
        
        // 检查是否是日程管理意图
        Matcher matcher = SCHEDULE_PATTERN.matcher(input);
        if (matcher.find()) {
            result.setIntent(IntentType.SCHEDULE);
            result.setDetails(matcher.group(1) != null ? matcher.group(1).trim() : "");
            
            // 提取日期和时间
            String matchedText = matcher.group(0);
            String dateTimePart = matchedText.replace(result.getDetails(), "").trim();
            // 移除"提醒"等动作词
            dateTimePart = dateTimePart.replaceAll("(提醒|安排|预约|日程)$", "").trim();
            result.setDateTime(dateTimePart);
        }
        
        return result;
    }
    
    /**
     * 意图类型枚举
     */
    public enum IntentType {
        GENERAL,      // 普通查询
        SCHEDULE,     // 日程管理
        WEATHER,      // 天气查询
        CALCULATION,  // 计算
        TRANSLATION   // 翻译
    }
    
    /**
     * 意图解析结果
     */
    public static class IntentResult {
        private String input;
        private IntentType intent;
        private String details;
        private String dateTime;
        
        // getter and setter methods
        public String getInput() {
            return input;
        }
        
        public void setInput(String input) {
            this.input = input;
        }
        
        public IntentType getIntent() {
            return intent;
        }
        
        public void setIntent(IntentType intent) {
            this.intent = intent;
        }
        
        public String getDetails() {
            return details;
        }
        
        public void setDetails(String details) {
            this.details = details;
        }
        
        public String getDateTime() {
            return dateTime;
        }
        
        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }
    }
}