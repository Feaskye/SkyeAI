package com.skyeai.jarvis.controller;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class TimeParsingTest {
    public static void main(String[] args) {
        // 测试带时区的时间格式
        String startTime = "2026-01-26T05:47:23.128+08:00";
        String endTime = "2026-01-26T15:47:23.128+08:00";
        
        System.out.println("测试时间解析:");
        System.out.println("StartTime: " + startTime);
        System.out.println("EndTime: " + endTime);
        
        try {
            // 测试带时区的解析
            if (startTime.contains("+")) {
                LocalDateTime start = OffsetDateTime.parse(startTime).toLocalDateTime();
                LocalDateTime end = OffsetDateTime.parse(endTime).toLocalDateTime();
                System.out.println("解析成功!");
                System.out.println("Start: " + start);
                System.out.println("End: " + end);
            } else {
                // 测试不带时区的解析
                LocalDateTime start = LocalDateTime.parse(startTime);
                LocalDateTime end = LocalDateTime.parse(endTime);
                System.out.println("解析成功!");
                System.out.println("Start: " + start);
                System.out.println("End: " + end);
            }
        } catch (Exception e) {
            System.out.println("解析失败: " + e.getMessage());
        }
        
        // 测试不带时区的格式
        String startTime2 = "2026-01-26T05:47:23";
        String endTime2 = "2026-01-26T15:47:23";
        
        System.out.println("\n测试不带时区的时间解析:");
        System.out.println("StartTime: " + startTime2);
        System.out.println("EndTime: " + endTime2);
        
        try {
            if (startTime2.contains("+")) {
                LocalDateTime start = OffsetDateTime.parse(startTime2).toLocalDateTime();
                LocalDateTime end = OffsetDateTime.parse(endTime2).toLocalDateTime();
                System.out.println("解析成功!");
                System.out.println("Start: " + start);
                System.out.println("End: " + end);
            } else {
                LocalDateTime start = LocalDateTime.parse(startTime2);
                LocalDateTime end = LocalDateTime.parse(endTime2);
                System.out.println("解析成功!");
                System.out.println("Start: " + start);
                System.out.println("End: " + end);
            }
        } catch (Exception e) {
            System.out.println("解析失败: " + e.getMessage());
        }
    }
}
