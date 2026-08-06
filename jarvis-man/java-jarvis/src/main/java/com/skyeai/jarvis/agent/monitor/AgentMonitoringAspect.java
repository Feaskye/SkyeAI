package com.skyeai.jarvis.agent.monitor;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent监控AOP
 * 监控AgentCore的关键方法执行，提供性能指标和日志记录
 */
@Slf4j
@Aspect
@Component
public class AgentMonitoringAspect {
    
    /**
     * 对话调用计数器
     */
    private static final AtomicLong chatCallCount = new AtomicLong(0);
    
    /**
     * 对话错误计数器
     */
    private static final AtomicLong chatErrorCount = new AtomicLong(0);
    
    /**
     * 总对话时间（毫秒）
     */
    private static final AtomicLong totalChatTime = new AtomicLong(0);
    
    /**
     * 定义切点：AgentCore的chat方法
     */
    @Pointcut("execution(* com.skyeai.jarvis.agent.AgentCore.chat(..))")
    public void agentChatPointcut() {
    }
    
    /**
     * 定义切点：AgentCore的工具调用方法
     */
    @Pointcut("execution(* com.skyeai.jarvis.agent.AgentCore.chatWithTools(..))")
    public void agentChatWithToolsPointcut() {
    }
    
    /**
     * 监控chat方法
     */
    @Around("agentChatPointcut() || agentChatWithToolsPointcut()")
    public Object monitorChat(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String sessionId = "unknown";
        String userInput = "";
        
        // 获取参数
        Object[] args = joinPoint.getArgs();
        if (args.length >= 2) {
            sessionId = args[0] != null ? args[0].toString() : "null";
            userInput = args[1] != null ? args[1].toString() : "null";
        }
        
        chatCallCount.incrementAndGet();
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 记录成功日志
            long duration = System.currentTimeMillis() - startTime;
            totalChatTime.addAndGet(duration);
            
            log.info("对话处理完成 - sessionId: {}, duration: {}ms, input: {}", 
                    sessionId, duration, truncateInput(userInput));
            
            // 记录指标（实际应用中应发送到监控系统）
            recordMetric("agent.chat.calls", 1);
            recordMetric("agent.chat.duration", duration);
            
            return result;
        } catch (Exception e) {
            // 记录错误日志
            chatErrorCount.incrementAndGet();
            long duration = System.currentTimeMillis() - startTime;
            
            log.error("对话处理失败 - sessionId: {}, duration: {}ms, error: {}", 
                    sessionId, duration, e.getMessage());
            
            // 记录错误指标
            recordMetric("agent.chat.errors", 1);
            
            throw e;
        }
    }
    
    /**
     * 截断输入日志
     */
    private String truncateInput(String input) {
        if (input == null) {
            return "null";
        }
        if (input.length() <= 100) {
            return input;
        }
        return input.substring(0, 100) + "...(truncated)";
    }
    
    /**
     * 记录指标（实际应用中应发送到Prometheus等监控系统）
     */
    private void recordMetric(String metricName, long value) {
        // 实际应用中应将指标发送到监控系统
        log.debug("指标记录: {} = {}", metricName, value);
    }
    
    /**
     * 获取对话调用次数
     */
    public static long getChatCallCount() {
        return chatCallCount.get();
    }
    
    /**
     * 获取对话错误次数
     */
    public static long getChatErrorCount() {
        return chatErrorCount.get();
    }
    
    /**
     * 获取平均对话时间
     */
    public static long getAverageChatTime() {
        long count = chatCallCount.get();
        if (count == 0) {
            return 0;
        }
        return totalChatTime.get() / count;
    }
    
    /**
     * 获取监控摘要
     */
    public static java.util.Map<String, Object> getMonitoringSummary() {
        return java.util.Map.of(
            "chatCallCount", chatCallCount.get(),
            "chatErrorCount", chatErrorCount.get(),
            "averageChatTime", getAverageChatTime(),
            "errorRate", chatCallCount.get() > 0 ? 
                (double) chatErrorCount.get() / chatCallCount.get() : 0
        );
    }
}