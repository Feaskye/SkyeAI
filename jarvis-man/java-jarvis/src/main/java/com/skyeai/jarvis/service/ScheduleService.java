package com.skyeai.jarvis.service;

import com.skyeai.jarvis.model.ScheduleEvent;
import com.skyeai.jarvis.protobuf.*;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 日程管理服务，用于处理日历事件
 */
@Service
public class ScheduleService {

    @GrpcClient("data")
    private DataServiceGrpc.DataServiceBlockingStub dataServiceStub;
    
    // 日期时间格式化器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * 创建日程事件
     * @param title 事件标题
     * @param dateTime 事件日期时间
     * @param description 事件描述
     * @return 创建的事件
     */
    public ScheduleEvent createEvent(String title, String dateTime, String description) {
        LocalDateTime eventDateTime = parseDateTime(dateTime);
        
        ScheduleEvent event = new ScheduleEvent();
        event.setTitle(title);
        event.setDateTime(eventDateTime);
        event.setDescription(description);
        event.setCreatedAt(LocalDateTime.now());
        
        return saveScheduleEvent(event);
    }
    
    /**
     * 获取所有日程事件
     * @return 所有事件列表
     */
    public List<ScheduleEvent> getAllEvents() {
        // 注意：这个方法在gRPC接口中没有直接对应，需要扩展gRPC接口或者使用其他方式实现
        // 目前返回空列表，实际项目中需要根据需求实现
        return new ArrayList<>();
    }
    
    /**
     * 根据ID获取事件
     * @param id 事件ID
     * @return 事件
     */
    public ScheduleEvent getEventById(long id) {
        // 注意：这个方法在gRPC接口中没有直接对应，需要扩展gRPC接口或者使用其他方式实现
        return null;
    }
    
    /**
     * 删除事件
     * @param id 事件ID
     * @return 是否删除成功
     */
    public boolean deleteEvent(long id) {
        try {
            DeleteScheduleEventRequest request = DeleteScheduleEventRequest.newBuilder()
                    .setId(id)
                    .build();
            DeleteScheduleEventResponse response = dataServiceStub.deleteScheduleEvent(request);
            return response.getSuccess();
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("删除日程事件失败: " + e.getStatus().getDescription(), e);
        }
    }
    
    /**
     * 创建重复日程事件
     * @param title 事件标题
     * @param description 事件描述
     * @param repeatType 重复类型（daily, weekly, monthly）
     * @param stockSymbols 股票代码列表，逗号分隔
     * @param priceChangeThreshold 价格变动阈值（百分比）
     * @return 创建的事件
     */
    public ScheduleEvent createRepeatEvent(String title, String description, String repeatType, String stockSymbols, double priceChangeThreshold) {
        ScheduleEvent event = new ScheduleEvent();
        event.setTitle(title);
        event.setDateTime(LocalDateTime.now()); // 立即开始
        event.setDescription(description);
        event.setCreatedAt(LocalDateTime.now());
        event.setRepeatType(repeatType);
        event.setStockSymbols(stockSymbols);
        event.setPriceChangeThreshold(priceChangeThreshold);
        event.setActive(true);
        
        return saveScheduleEvent(event);
    }
    
    /**
     * 获取所有活跃的重复事件
     * @return 活跃的重复事件列表
     */
    public List<ScheduleEvent> getActiveRepeatEvents() {
        try {
            GetAllActiveScheduleEventsRequest request = GetAllActiveScheduleEventsRequest.newBuilder().build();
            GetAllActiveScheduleEventsResponse response = dataServiceStub.getAllActiveScheduleEvents(request);
            List<ScheduleEventProto> protoList = response.getScheduleEventsList();
            List<ScheduleEvent> events = new ArrayList<>();
            for (ScheduleEventProto proto : protoList) {
                events.add(convertToScheduleEvent(proto));
            }
            return events;
        } catch (StatusRuntimeException e) {
            // 当DataService不可用时，返回空列表，实现优雅降级
            System.err.println("警告: DataService不可用，返回空日程事件列表: " + e.getStatus().getDescription());
            return new ArrayList<>();
        }
    }
    
    /**
     * 保存日程事件
     * @param event 日程事件对象
     * @return 保存后的日程事件
     */
    private ScheduleEvent saveScheduleEvent(ScheduleEvent event) {
        try {
            ScheduleEventProto proto = convertToScheduleEventProto(event);
            SaveScheduleEventRequest request = SaveScheduleEventRequest.newBuilder()
                    .setScheduleEvent(proto)
                    .build();
            SaveScheduleEventResponse response = dataServiceStub.saveScheduleEvent(request);
            return convertToScheduleEvent(response.getScheduleEvent());
        } catch (StatusRuntimeException e) {
            // 当DataService不可用时，返回原始事件，实现优雅降级
            System.err.println("警告: DataService不可用，无法保存日程事件: " + e.getStatus().getDescription());
            return event;
        }
    }
    
    /**
     * 解析日期时间字符串
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime对象
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            // 首先尝试标准格式
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // 尝试ISO格式
                return LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
            } catch (DateTimeParseException e2) {
                // 解析自然语言时间格式
                try {
                    return parseNaturalLanguageDateTime(dateTimeStr);
                } catch (Exception e3) {
                    System.err.println("日期时间解析失败: " + dateTimeStr + ", 使用默认时间");
                    return LocalDateTime.now().plusHours(1);
                }
            }
        }
    }
    
    /**
     * 解析自然语言时间格式
     * @param dateTimeStr 自然语言时间字符串
     * @return LocalDateTime对象
     */
    private LocalDateTime parseNaturalLanguageDateTime(String dateTimeStr) {
        LocalDateTime now = LocalDateTime.now();
        int hour = 0;
        int minute = 0;
        
        // 提取小时
        java.util.regex.Pattern hourPattern = java.util.regex.Pattern.compile("(\\d{1,2})点");
        java.util.regex.Matcher hourMatcher = hourPattern.matcher(dateTimeStr);
        if (hourMatcher.find()) {
            hour = Integer.parseInt(hourMatcher.group(1));
        }
        
        // 提取分钟（可选）
        java.util.regex.Pattern minutePattern = java.util.regex.Pattern.compile("(\\d{1,2})分");
        java.util.regex.Matcher minuteMatcher = minutePattern.matcher(dateTimeStr);
        if (minuteMatcher.find()) {
            minute = Integer.parseInt(minuteMatcher.group(1));
        }
        
        // 确定日期
        if (dateTimeStr.contains("明天")) {
            return now.plusDays(1).withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        } else if (dateTimeStr.contains("后天")) {
            return now.plusDays(2).withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        } else if (dateTimeStr.contains("大后天")) {
            return now.plusDays(3).withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        } else if (dateTimeStr.contains("今天")) {
            return now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        } else {
            // 默认明天
            return now.plusDays(1).withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        }
    }
    
    /**
     * 将ScheduleEvent转换为ScheduleEventProto
     * @param event ScheduleEvent对象
     * @return ScheduleEventProto对象
     */
    private ScheduleEventProto convertToScheduleEventProto(ScheduleEvent event) {
        ScheduleEventProto.Builder builder = ScheduleEventProto.newBuilder()
                .setTitle(event.getTitle() != null ? event.getTitle() : "")
                .setDateTime(event.getDateTime() != null ? event.getDateTime().format(ISO_FORMATTER) : "")
                .setDescription(event.getDescription() != null ? event.getDescription() : "")
                .setCreatedAt(event.getCreatedAt() != null ? event.getCreatedAt().format(ISO_FORMATTER) : LocalDateTime.now().format(ISO_FORMATTER))
                .setPriceChangeThreshold(event.getPriceChangeThreshold())
                .setActive(event.isActive());
        
        // 处理可能为null的字段
        if (event.getRepeatType() != null) {
            builder.setRepeatType(event.getRepeatType());
        }
        
        if (event.getStockSymbols() != null) {
            builder.setStockSymbols(event.getStockSymbols());
        }
        
        if (event.getLastCheckTime() != null) {
            builder.setLastCheckTime(event.getLastCheckTime());
        }
        
        return builder.build();
    }
    
    /**
     * 将ScheduleEventProto转换为ScheduleEvent
     * @param proto ScheduleEventProto对象
     * @return ScheduleEvent对象
     */
    private ScheduleEvent convertToScheduleEvent(ScheduleEventProto proto) {
        ScheduleEvent event = new ScheduleEvent();
        event.setId(proto.getId());
        event.setTitle(proto.getTitle());
        event.setDateTime(LocalDateTime.parse(proto.getDateTime(), ISO_FORMATTER));
        event.setDescription(proto.getDescription());
        event.setCreatedAt(LocalDateTime.parse(proto.getCreatedAt(), ISO_FORMATTER));
        event.setRepeatType(proto.getRepeatType());
        event.setStockSymbols(proto.getStockSymbols());
        event.setPriceChangeThreshold(proto.getPriceChangeThreshold());
        event.setActive(proto.getActive());
        event.setLastCheckTime(proto.getLastCheckTime());
        return event;
    }
}