package com.skyeai.jarvis.agent;

import com.skyeai.jarvis.agent.tool.ToolCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 异步工具执行器
 * 实现非阻塞工具调用，支持并行执行和超时控制
 */
@Slf4j
@Component
public class AsyncToolExecutor {
    
    /**
     * 线程池
     */
    private final ExecutorService executorService;
    
    /**
     * 默认超时时间（秒）
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    
    public AsyncToolExecutor() {
        // 创建固定大小的线程池
        this.executorService = Executors.newFixedThreadPool(10);
    }
    
    /**
     * 异步执行工具
     * @param tool 工具回调
     * @param params 调用参数
     * @return 工具执行结果的Future
     */
    public CompletableFuture<String> executeAsync(ToolCallback tool, Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("异步执行工具: {}", tool.getName());
                String result = (String) tool.getFunction().apply(params);
                log.debug("工具执行完成: {}, 结果长度: {}", tool.getName(), 
                        result != null ? result.length() : 0);
                return result;
            } catch (Exception e) {
                log.error("工具执行失败: {}", tool.getName(), e);
                return "工具执行失败: " + e.getMessage();
            }
        }, executorService);
    }
    
    /**
     * 异步执行工具（带超时）
     * @param tool 工具回调
     * @param params 调用参数
     * @param timeoutSeconds 超时时间（秒）
     * @return 工具执行结果的Future
     */
    public CompletableFuture<String> executeAsync(ToolCallback tool, Map<String, Object> params, 
                                                   int timeoutSeconds) {
        return executeAsync(tool, params).orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("工具执行超时: {}", tool.getName());
                    return "工具执行超时: " + tool.getName();
                });
    }
    
    /**
     * 并行执行多个工具调用
     * @param toolCalls 工具调用列表
     * @return 所有工具执行结果
     */
    public Map<String, String> executeParallel(List<ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            return Map.of();
        }
        
        log.debug("并行执行 {} 个工具调用", toolCalls.size());
        
        List<CompletableFuture<Map.Entry<String, String>>> futures = toolCalls.stream()
                .map(call -> executeAsync(call.getTool(), call.getParams(), DEFAULT_TIMEOUT_SECONDS)
                        .thenApply(result -> Map.entry(call.getTool().getName(), result)))
                .collect(Collectors.toList());
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 收集结果
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * 关闭执行器
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 工具调用类
     */
    public static class ToolCall {
        private final ToolCallback tool;
        private final Map<String, Object> params;
        
        public ToolCall(ToolCallback tool, Map<String, Object> params) {
            this.tool = tool;
            this.params = params;
        }
        
        public ToolCallback getTool() {
            return tool;
        }
        
        public Map<String, Object> getParams() {
            return params;
        }
    }
}