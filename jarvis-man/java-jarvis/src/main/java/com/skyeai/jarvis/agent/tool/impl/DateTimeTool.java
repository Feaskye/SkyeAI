package com.skyeai.jarvis.agent.tool.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期时间工具
 * v10 新增：使用 Spring AI 2.0 @Tool 注解声明
 * 提供日期查询、日期计算、格式化等能力
 */
@Slf4j
@Component
public class DateTimeTool {

    @Tool(description = "获取当前日期和时间，可选指定时区格式")
    public String getCurrentDateTime(
            @ToolParam(description = "日期时间格式，如 yyyy-MM-dd HH:mm:ss，为空则使用默认格式") String format) {
        LocalDateTime now = LocalDateTime.now();
        String pattern = (format == null || format.isBlank()) ? "yyyy-MM-dd HH:mm:ss" : format;
        try {
            String formatted = now.format(DateTimeFormatter.ofPattern(pattern));
            log.debug("获取当前时间: {}", formatted);
            return "当前时间：" + formatted;
        } catch (Exception e) {
            log.warn("日期格式无效: {}，使用默认格式", format);
            return "当前时间：" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    @Tool(description = "计算两个日期之间的天数差")
    public String daysBetween(
            @ToolParam(description = "开始日期，格式 yyyy-MM-dd") String startDate,
            @ToolParam(description = "结束日期，格式 yyyy-MM-dd") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            long days = ChronoUnit.DAYS.between(start, end);
            return String.format("从 %s 到 %s 相差 %d 天", startDate, endDate, days);
        } catch (Exception e) {
            log.error("日期计算失败: {} - {}", startDate, endDate, e);
            return "日期格式错误，请使用 yyyy-MM-dd 格式";
        }
    }

    @Tool(description = "获取指定日期是星期几")
    public String getDayOfWeek(
            @ToolParam(description = "日期，格式 yyyy-MM-dd，为空则查询今天") String date) {
        try {
            LocalDate targetDate = (date == null || date.isBlank())
                    ? LocalDate.now() : LocalDate.parse(date);
            String dayOfWeek = targetDate.getDayOfWeek().toString();
            String chineseDay = switch (targetDate.getDayOfWeek().getValue()) {
                case 1 -> "星期一";
                case 2 -> "星期二";
                case 3 -> "星期三";
                case 4 -> "星期四";
                case 5 -> "星期五";
                case 6 -> "星期六";
                case 7 -> "星期日";
                default -> "未知";
            };
            return String.format("%s 是 %s", targetDate, chineseDay);
        } catch (Exception e) {
            log.error("查询星期失败: {}", date, e);
            return "日期格式错误，请使用 yyyy-MM-dd 格式";
        }
    }
}
