package com.skyeai.jarvis.service;

import com.skyeai.jarvis.model.ScheduleEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日程解析服务
 * 实现混合方法：规则引擎 + AI 解析
 */
@Service
public class ScheduleParserService {
    
    @Autowired
    private ScheduleService scheduleService;
    
    // 简单日程表达的正则表达式
    private static final Pattern SIMPLE_SCHEDULE_PATTERN = Pattern.compile(
            "(?:(明天|后天|大后天|下周一|下周二|下周三|下周四|下周五|下周六|下周日|\\d{4}-\\d{2}-\\d{2}|\\d{1,2}月\\d{1,2}日)\\s*)" +
            "(?:(上午|下午|晚上)?\\s*)" +
            "(?:(\\d{1,2})点(?:(\\d{1,2})分)?)\\s*" +
            "(?:(提醒|安排|预约|设置|开会|会议|约会|培训)\\s*)" +
            "(.+)?",
            Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 解析日程表达式
     * @param input 用户输入
     * @return 日程事件
     */
    public ScheduleEvent parseSchedule(String input) {
        System.out.println("ScheduleParserService.parseSchedule被调用: " + input);
        try {
            // 1. 首先尝试使用规则引擎解析
            System.out.println("步骤1: 尝试使用规则引擎解析");
            ScheduleEvent event = parseWithRules(input);
            if (event != null) {
                System.out.println("使用规则引擎解析成功: " + input);
                return event;
            }
            
            // 2. 如果规则引擎失败，使用AI解析
            System.out.println("步骤2: 尝试使用AI解析");
            event = parseWithAI(input);
            if (event != null) {
                System.out.println("使用AI解析成功: " + input);
                return event;
            }
            
            // 3. 解析失败
            System.err.println("步骤3: 日程解析失败: " + input);
            return null;
        } catch (Exception e) {
            System.err.println("解析日程时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 使用规则引擎解析日程
     * @param input 用户输入
     * @return 日程事件
     */
    private ScheduleEvent parseWithRules(String input) {
        System.out.println("尝试使用规则引擎解析: " + input);
        Matcher matcher = SIMPLE_SCHEDULE_PATTERN.matcher(input);
        if (matcher.find()) {
            System.out.println("规则引擎匹配成功！");
            String dateStr = matcher.group(1);
            String timeOfDay = matcher.group(2);
            String hourStr = matcher.group(3);
            String minuteStr = matcher.group(4);
            String action = matcher.group(5);
            String title = matcher.group(6);
            
            System.out.println("解析结果: 日期=" + dateStr + ", 时间段=" + timeOfDay + ", 小时=" + hourStr + ", 分钟=" + minuteStr + ", 动作=" + action + ", 标题=" + title);
            
            // 解析时间
            LocalDateTime dateTime = parseDateTime(dateStr, timeOfDay, hourStr, minuteStr);
            if (dateTime == null) {
                System.err.println("时间解析失败");
                return null;
            }
            
            System.out.println("时间解析成功: " + dateTime);
            
            // 创建日程事件
            ScheduleEvent event = scheduleService.createEvent(
                    title,
                    dateTime.toString(),
                    "通过规则引擎创建的日程: " + input
            );
            
            if (event != null) {
                System.out.println("日程事件创建成功: " + event.getTitle() + " " + event.getDateTime());
            } else {
                System.err.println("日程事件创建失败");
            }
            
            return event;
        }
        System.err.println("规则引擎匹配失败");
        return null;
    }
    
    /**
     * 使用AI解析日程
     * @param input 用户输入
     * @return 日程事件
     */
    private ScheduleEvent parseWithAI(String input) {
        try {
            // 暂时禁用AI解析，避免循环依赖
            System.out.println("AI解析功能暂时禁用: " + input);
            
            // 这里可以在未来实现直接调用AI服务API的逻辑
            // 或者使用其他方式来解析复杂的日程表达式
        } catch (Exception e) {
            System.err.println("AI解析失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 解析日期时间
     * @param dateStr 日期字符串
     * @param timeOfDay 时间段（上午/下午/晚上）
     * @param hourStr 小时字符串
     * @param minuteStr 分钟字符串
     * @return LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateStr, String timeOfDay, String hourStr, String minuteStr) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int year = now.getYear();
            int month = now.getMonthValue();
            int day = now.getDayOfMonth();
            int hour = 0;
            int minute = 0;
            
            // 解析日期
            if (dateStr.equals("明天")) {
                now = now.plusDays(1);
            } else if (dateStr.equals("后天")) {
                now = now.plusDays(2);
            } else if (dateStr.equals("大后天")) {
                now = now.plusDays(3);
            } else if (dateStr.startsWith("下周")) {
                // 处理下周一到下周日
                int daysToAdd = getDaysToAddForNextWeekDay(dateStr);
                now = now.plusDays(daysToAdd);
            } else if (dateStr.contains("-")) {
                // 处理YYYY-MM-DD格式
                String[] parts = dateStr.split("-");
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
                now = LocalDateTime.of(year, month, day, 0, 0);
            } else if (dateStr.contains("月")) {
                // 处理MM月DD日格式
                Pattern monthDayPattern = Pattern.compile("(\\d{1,2})月(\\d{1,2})日");
                Matcher matcher = monthDayPattern.matcher(dateStr);
                if (matcher.find()) {
                    month = Integer.parseInt(matcher.group(1));
                    day = Integer.parseInt(matcher.group(2));
                    now = LocalDateTime.of(year, month, day, 0, 0);
                }
            }
            
            // 解析时间
            if (hourStr != null) {
                hour = Integer.parseInt(hourStr);
                // 处理上午/下午
                if ("下午".equals(timeOfDay) && hour < 12) {
                    hour += 12;
                } else if ("晚上".equals(timeOfDay) && hour < 12) {
                    hour += 12;
                }
            }
            
            if (minuteStr != null) {
                minute = Integer.parseInt(minuteStr);
            }
            
            return now.withHour(hour).withMinute(minute);
        } catch (Exception e) {
            System.err.println("解析日期时间失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从字符串解析日期时间
     * @param dateStr 日期字符串
     * @param timeStr 时间字符串
     * @return LocalDateTime
     */
    private LocalDateTime parseDateTimeFromStrings(String dateStr, String timeStr) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, dateFormatter);
            
            if (timeStr != null && !timeStr.isEmpty()) {
                String[] timeParts = timeStr.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;
                dateTime = dateTime.withHour(hour).withMinute(minute);
            }
            
            return dateTime;
        } catch (Exception e) {
            System.err.println("解析日期时间失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取下周一到下周日的天数
     * @param dayStr 星期字符串
     * @return 天数
     */
    private int getDaysToAddForNextWeekDay(String dayStr) {
        LocalDateTime now = LocalDateTime.now();
        int currentDayOfWeek = now.getDayOfWeek().getValue(); // 1-7
        int targetDayOfWeek = 0;
        
        switch (dayStr) {
            case "下周一": targetDayOfWeek = 1; break;
            case "下周二": targetDayOfWeek = 2; break;
            case "下周三": targetDayOfWeek = 3; break;
            case "下周四": targetDayOfWeek = 4; break;
            case "下周五": targetDayOfWeek = 5; break;
            case "下周六": targetDayOfWeek = 6; break;
            case "下周日": targetDayOfWeek = 7; break;
            default: return 7; // 默认下周
        }
        
        int daysToAdd = targetDayOfWeek - currentDayOfWeek;
        if (daysToAdd <= 0) {
            daysToAdd += 7;
        }
        
        return daysToAdd;
    }
    
    /**
     * 从JSON字符串中提取字段
     * @param json JSON字符串
     * @param field 字段名
     * @return 字段值
     */
    private String extractJsonField(String json, String field) {
        try {
            // 简单的字符串操作提取字段
            String searchStr = "\"" + field + "\":\"";
            int startIndex = json.indexOf(searchStr);
            if (startIndex != -1) {
                startIndex += searchStr.length();
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            System.err.println("提取JSON字段失败: " + e.getMessage());
        }
        return null;
    }
}
