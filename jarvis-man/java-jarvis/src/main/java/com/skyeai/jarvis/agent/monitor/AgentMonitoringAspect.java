package com.skyeai.jarvis.agent.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent监控AOP
 * 监控AgentCore的关键方法执行，提供性能指标和日志记录
 * v10 改造：纯文本日志改为结构化 JSON；recordMetric 改用 Micrometer MeterRegistry
 */
@Slf4j
@Aspect
@Component
public class AgentMonitoringAspect {

    /**
     * 对话调用计数器（静态摘要用）
     */
    private static final AtomicLong chatCallCount = new AtomicLong(0);

    /**
     * 对话错误计数器（静态摘要用）
     */
    private static final AtomicLong chatErrorCount = new AtomicLong(0);

    /**
     * 总对话时间（毫秒，静态摘要用）
     */
    private static final AtomicLong totalChatTime = new AtomicLong(0);

    /**
     * Micrometer 指标注册表
     * 注：需 spring-boot-starter-actuator 才会自动装配 MeterRegistry Bean；
     * 当前未引入 actuator 时该字段为 null，recordCounter/recordTimer 会优雅降级。
     */
    @Autowired(required = false)
    private MeterRegistry meterRegistry;

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

            // 记录成功日志（结构化 JSON）
            long duration = System.currentTimeMillis() - startTime;
            totalChatTime.addAndGet(duration);

            log.info("{\"event\":\"agent_chat_complete\",\"session_id\":\"{}\",\"duration\":{},\"input\":\"{}\"}",
                    escape(sessionId), duration, escape(truncateInput(userInput)));

            // 记录指标：调用计数 + 耗时
            recordCounter("jarvis.agent.chat.calls", 1);
            recordTimer("jarvis.agent.chat.duration", duration);

            return result;
        } catch (Exception e) {
            // 记录错误日志（结构化 JSON）
            chatErrorCount.incrementAndGet();
            long duration = System.currentTimeMillis() - startTime;

            log.error("{\"event\":\"agent_chat_error\",\"session_id\":\"{}\",\"duration\":{},\"error\":\"{}\"}",
                    escape(sessionId), duration, escape(e.getMessage()));

            // 记录错误指标
            recordCounter("jarvis.agent.chat.errors", 1);

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
     * 记录计数器指标（Micrometer）
     */
    private void recordCounter(String name, long value) {
        if (meterRegistry == null) {
            log.debug("MeterRegistry 未注入，跳过指标记录: {}", name);
            return;
        }
        meterRegistry.counter(name).increment(value);
    }

    /**
     * 记录耗时指标（Micrometer Timer）
     */
    private void recordTimer(String name, long durationMillis) {
        if (meterRegistry == null) {
            log.debug("MeterRegistry 未注入，跳过指标记录: {}", name);
            return;
        }
        Timer timer = meterRegistry.timer(name);
        timer.record(durationMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 简单转义日志中的双引号与反斜杠，避免破坏 JSON 结构
     */
    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
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
    public static Map<String, Object> getMonitoringSummary() {
        return Map.of(
            "chatCallCount", chatCallCount.get(),
            "chatErrorCount", chatErrorCount.get(),
            "averageChatTime", getAverageChatTime(),
            "errorRate", chatCallCount.get() > 0 ?
                (double) chatErrorCount.get() / chatCallCount.get() : 0
        );
    }
}
