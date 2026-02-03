package com.skyeai.jarvis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态任务调度服务，用于管理定时任务的创建、删除和更新
 */
@Service
public class DynamicTaskScheduler {

    private final TaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks;

    @Autowired
    public DynamicTaskScheduler() {
        this.taskScheduler = new ConcurrentTaskScheduler();
        this.scheduledTasks = new ConcurrentHashMap<>();
    }

    /**
     * 注册周期性任务
     * @param taskId 任务ID
     * @param task 任务内容
     * @param interval 执行间隔（秒）
     */
    public void schedulePeriodicTask(Long taskId, Runnable task, long interval) {
        // 先取消已存在的任务
        cancelTask(taskId);
        
        // 调度新任务
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
                task,
                Instant.now(),
                Duration.ofSeconds(interval)
        );
        
        scheduledTasks.put(taskId, future);
    }

    /**
     * 注册一次性任务
     * @param taskId 任务ID
     * @param task 任务内容
     * @param delay 延迟执行时间（秒）
     */
    public void scheduleOneTimeTask(Long taskId, Runnable task, long delay) {
        // 先取消已存在的任务
        cancelTask(taskId);
        
        // 调度新任务
        ScheduledFuture<?> future = taskScheduler.schedule(
                task,
                Instant.now().plusSeconds(delay)
        );
        
        scheduledTasks.put(taskId, future);
    }

    /**
     * 取消任务
     * @param taskId 任务ID
     */
    public void cancelTask(Long taskId) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * 取消所有任务
     */
    public void cancelAllTasks() {
        for (ScheduledFuture<?> future : scheduledTasks.values()) {
            future.cancel(false);
        }
        scheduledTasks.clear();
    }

    /**
     * 检查任务是否存在
     * @param taskId 任务ID
     * @return 是否存在
     */
    public boolean hasTask(Long taskId) {
        return scheduledTasks.containsKey(taskId);
    }

    /**
     * 获取当前任务数量
     * @return 任务数量
     */
    public int getTaskCount() {
        return scheduledTasks.size();
    }
}
