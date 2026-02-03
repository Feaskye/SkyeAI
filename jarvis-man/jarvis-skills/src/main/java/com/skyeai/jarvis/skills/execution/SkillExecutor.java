package com.skyeai.jarvis.skills.execution;

import com.skyeai.jarvis.skills.model.Skill;
import com.skyeai.jarvis.skills.model.SkillExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Skill执行引擎
 * 管理Skills的执行过程和生命周期
 */
@Slf4j
@Component
public class SkillExecutor {

    @Value("${skills.execution.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${skills.execution.max-concurrent:10}")
    private int maxConcurrent;

    // 线程池
    private ExecutorService executorService;

    // 执行中的任务
    private final Map<String, CompletableFuture<SkillExecution>> runningTasks = new ConcurrentHashMap<>();

    /**
     * 初始化方法，在 @Value 注入后执行
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        // 创建线程池
        this.executorService = new ThreadPoolExecutor(
                5, // 核心线程数
                Math.max(5, maxConcurrent), // 确保最大线程数不小于核心线程数
                60, TimeUnit.SECONDS, // 空闲线程存活时间
                new LinkedBlockingQueue<>(), // 队列
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
    }

    public SkillExecutor() {
        // 空构造函数
    }

    /**
     * 执行Skill
     * @param skill Skill对象
     * @param inputParameters 输入参数
     * @param executionFunction 执行函数
     * @return SkillExecution对象
     */
    public SkillExecution executeSkill(Skill skill, Map<String, Object> inputParameters, 
                                      Function<Map<String, Object>, Map<String, Object>> executionFunction) {
        log.info("Executing skill: {} version: {}", skill.getName(), skill.getVersion());

        // 创建执行记录
        SkillExecution execution = createExecutionRecord(skill, inputParameters);

        // 提交任务
        CompletableFuture<SkillExecution> future = CompletableFuture.supplyAsync(() -> {
            try {
                // 执行Skill
                Map<String, Object> result = executionFunction.apply(inputParameters);

                // 更新执行记录
                execution.setStatus("SUCCESS");
                execution.setOutputResult(mapToJson(result));
                execution.setEndTime(LocalDateTime.now());
                execution.setExecutionTimeMs(calculateExecutionTime(execution));

                log.info("Skill executed successfully: {} version: {}", skill.getName(), skill.getVersion());
            } catch (Exception e) {
                log.error("Error executing skill: {} version: {}", skill.getName(), skill.getVersion(), e);

                // 更新执行记录
                execution.setStatus("FAILED");
                execution.setErrorMessage(e.getMessage());
                execution.setEndTime(LocalDateTime.now());
                execution.setExecutionTimeMs(calculateExecutionTime(execution));
            }

            return execution;
        }, executorService);

        // 添加到运行任务
        runningTasks.put(execution.getExecutionId(), future);

        // 处理任务完成
        future.thenAccept(completedExecution -> {
            runningTasks.remove(execution.getExecutionId());
            log.debug("Skill execution completed: {} status: {}", 
                    completedExecution.getExecutionId(), completedExecution.getStatus());
        });

        try {
            // 等待执行完成
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("Skill execution timeout: {} version: {}", skill.getName(), skill.getVersion());

            // 更新执行记录
            execution.setStatus("TIMEOUT");
            execution.setErrorMessage("Execution timeout");
            execution.setEndTime(LocalDateTime.now());
            execution.setExecutionTimeMs(calculateExecutionTime(execution));

            // 从运行任务移除
            runningTasks.remove(execution.getExecutionId());

            return execution;
        } catch (Exception e) {
            log.error("Error waiting for skill execution: {} version: {}", skill.getName(), skill.getVersion(), e);

            // 更新执行记录
            execution.setStatus("ERROR");
            execution.setErrorMessage(e.getMessage());
            execution.setEndTime(LocalDateTime.now());
            execution.setExecutionTimeMs(calculateExecutionTime(execution));

            // 从运行任务移除
            runningTasks.remove(execution.getExecutionId());

            return execution;
        }
    }

    /**
     * 异步执行Skill
     * @param skill Skill对象
     * @param inputParameters 输入参数
     * @param executionFunction 执行函数
     * @return 执行ID
     */
    public String executeSkillAsync(Skill skill, Map<String, Object> inputParameters, 
                                   Function<Map<String, Object>, Map<String, Object>> executionFunction) {
        log.info("Executing skill asynchronously: {} version: {}", skill.getName(), skill.getVersion());

        // 创建执行记录
        SkillExecution execution = createExecutionRecord(skill, inputParameters);

        // 提交任务
        CompletableFuture<SkillExecution> future = CompletableFuture.supplyAsync(() -> {
            try {
                // 执行Skill
                Map<String, Object> result = executionFunction.apply(inputParameters);

                // 更新执行记录
                execution.setStatus("SUCCESS");
                execution.setOutputResult(mapToJson(result));
                execution.setEndTime(LocalDateTime.now());
                execution.setExecutionTimeMs(calculateExecutionTime(execution));

                log.info("Skill executed successfully: {} version: {}", skill.getName(), skill.getVersion());
            } catch (Exception e) {
                log.error("Error executing skill: {} version: {}", skill.getName(), skill.getVersion(), e);

                // 更新执行记录
                execution.setStatus("FAILED");
                execution.setErrorMessage(e.getMessage());
                execution.setEndTime(LocalDateTime.now());
                execution.setExecutionTimeMs(calculateExecutionTime(execution));
            }

            return execution;
        }, executorService);

        // 添加到运行任务
        runningTasks.put(execution.getExecutionId(), future);

        // 处理任务完成
        future.thenAccept(completedExecution -> {
            runningTasks.remove(execution.getExecutionId());
            log.debug("Skill execution completed: {} status: {}", 
                    completedExecution.getExecutionId(), completedExecution.getStatus());
        });

        return execution.getExecutionId();
    }

    /**
     * 获取执行状态
     * @param executionId 执行ID
     * @return SkillExecution对象
     */
    public SkillExecution getExecutionStatus(String executionId) {
        CompletableFuture<SkillExecution> future = runningTasks.get(executionId);
        if (future != null) {
            if (future.isDone()) {
                try {
                    return future.get();
                } catch (Exception e) {
                    log.error("Error getting execution status: {}", executionId, e);
                    return null;
                }
            } else {
                // 任务仍在执行
                SkillExecution execution = new SkillExecution();
                execution.setExecutionId(executionId);
                execution.setStatus("RUNNING");
                return execution;
            }
        }
        return null;
    }

    /**
     * 取消执行
     * @param executionId 执行ID
     * @return 是否取消成功
     */
    public boolean cancelExecution(String executionId) {
        CompletableFuture<SkillExecution> future = runningTasks.get(executionId);
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(true);
            if (cancelled) {
                runningTasks.remove(executionId);
                log.info("Skill execution cancelled: {}", executionId);
            }
            return cancelled;
        }
        return false;
    }

    /**
     * 创建执行记录
     * @param skill Skill对象
     * @param inputParameters 输入参数
     * @return SkillExecution对象
     */
    private SkillExecution createExecutionRecord(Skill skill, Map<String, Object> inputParameters) {
        SkillExecution execution = new SkillExecution();
        execution.setSkill(skill);
        execution.setStatus("PENDING");
        execution.setInputParameters(mapToJson(inputParameters));
        execution.setExecutionId(UUID.randomUUID().toString());
        execution.setStartTime(LocalDateTime.now());
        return execution;
    }

    /**
     * 计算执行时间
     * @param execution SkillExecution对象
     * @return 执行时间（毫秒）
     */
    private long calculateExecutionTime(SkillExecution execution) {
        if (execution.getStartTime() != null && execution.getEndTime() != null) {
            Duration duration = Duration.between(execution.getStartTime(), execution.getEndTime());
            return duration.toMillis();
        }
        return 0;
    }

    /**
     * Map转JSON
     * @param map Map对象
     * @return JSON字符串
     */
    private String mapToJson(Map<String, Object> map) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("Error converting map to JSON", e);
            return "{}";
        }
    }

    /**
     * 关闭执行引擎
     */
    public void shutdown() {
        log.info("Shutting down skill executor");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Skill executor shutdown");
    }

}
