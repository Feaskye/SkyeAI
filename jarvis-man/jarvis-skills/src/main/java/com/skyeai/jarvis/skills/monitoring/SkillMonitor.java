package com.skyeai.jarvis.skills.monitoring;

import com.skyeai.jarvis.skills.model.Skill;
import com.skyeai.jarvis.skills.model.SkillExecution;
import com.skyeai.jarvis.skills.model.SkillMetric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Skill监控系统
 * 监控Skills的执行状态、性能和错误率
 */
@Slf4j
@Component
public class SkillMonitor {

    @Value("${skills.monitoring.metrics-interval:10}")
    private int metricsInterval;

    // 执行计数器
    private final Map<String, AtomicLong> executionCounter = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> successCounter = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> failureCounter = new ConcurrentHashMap<>();

    // 性能指标
    private final Map<String, AtomicLong> totalExecutionTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> lastExecutionTime = new ConcurrentHashMap<>();

    // 错误统计
    private final Map<String, Map<String, AtomicLong>> errorCounter = new ConcurrentHashMap<>();

    /**
     * 记录执行开始
     * @param skill Skill对象
     */
    public void recordExecutionStart(Skill skill) {
        String skillKey = getSkillKey(skill);
        executionCounter.computeIfAbsent(skillKey, k -> new AtomicLong()).incrementAndGet();
        log.debug("Skill execution started: {}", skillKey);
    }

    /**
     * 记录执行完成
     * @param execution SkillExecution对象
     */
    public void recordExecutionComplete(SkillExecution execution) {
        if (execution.getSkill() == null) {
            return;
        }

        String skillKey = getSkillKey(execution.getSkill());

        if ("SUCCESS".equals(execution.getStatus())) {
            successCounter.computeIfAbsent(skillKey, k -> new AtomicLong()).incrementAndGet();
        } else if ("FAILED".equals(execution.getStatus()) || "ERROR".equals(execution.getStatus())) {
            failureCounter.computeIfAbsent(skillKey, k -> new AtomicLong()).incrementAndGet();

            // 记录错误
            if (execution.getErrorMessage() != null) {
                String errorType = getErrorType(execution.getErrorMessage());
                errorCounter.computeIfAbsent(skillKey, k -> new ConcurrentHashMap<>())
                        .computeIfAbsent(errorType, k -> new AtomicLong())
                        .incrementAndGet();
            }
        }

        // 记录执行时间
        if (execution.getExecutionTimeMs() != null) {
            totalExecutionTime.computeIfAbsent(skillKey, k -> new AtomicLong())
                    .addAndGet(execution.getExecutionTimeMs());
            lastExecutionTime.computeIfAbsent(skillKey, k -> new AtomicLong())
                    .set(execution.getExecutionTimeMs());
        }

        log.debug("Skill execution completed: {} status: {}", skillKey, execution.getStatus());
    }

    /**
     * 获取执行统计
     * @param skill Skill对象
     * @return 统计信息
     */
    public Map<String, Object> getExecutionStats(Skill skill) {
        String skillKey = getSkillKey(skill);

        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalExecutions", executionCounter.getOrDefault(skillKey, new AtomicLong()).get());
        stats.put("successfulExecutions", successCounter.getOrDefault(skillKey, new AtomicLong()).get());
        stats.put("failedExecutions", failureCounter.getOrDefault(skillKey, new AtomicLong()).get());

        long totalExec = executionCounter.getOrDefault(skillKey, new AtomicLong()).get();
        long successExec = successCounter.getOrDefault(skillKey, new AtomicLong()).get();
        double successRate = totalExec > 0 ? (double) successExec / totalExec * 100 : 0;
        stats.put("successRate", successRate);

        long totalTime = totalExecutionTime.getOrDefault(skillKey, new AtomicLong()).get();
        double avgExecutionTime = totalExec > 0 ? (double) totalTime / totalExec : 0;
        stats.put("averageExecutionTimeMs", avgExecutionTime);
        stats.put("lastExecutionTimeMs", lastExecutionTime.getOrDefault(skillKey, new AtomicLong()).get());

        // 错误统计
        Map<String, Long> errorStats = new ConcurrentHashMap<>();
        Map<String, AtomicLong> skillErrors = errorCounter.get(skillKey);
        if (skillErrors != null) {
            for (Map.Entry<String, AtomicLong> entry : skillErrors.entrySet()) {
                errorStats.put(entry.getKey(), entry.getValue().get());
            }
        }
        stats.put("errors", errorStats);

        return stats;
    }

    /**
     * 获取所有Skill的统计信息
     * @return 统计信息
     */
    public Map<String, Map<String, Object>> getAllExecutionStats() {
        Map<String, Map<String, Object>> allStats = new ConcurrentHashMap<>();

        for (String skillKey : executionCounter.keySet()) {
            Map<String, Object> stats = new ConcurrentHashMap<>();
            stats.put("totalExecutions", executionCounter.getOrDefault(skillKey, new AtomicLong()).get());
            stats.put("successfulExecutions", successCounter.getOrDefault(skillKey, new AtomicLong()).get());
            stats.put("failedExecutions", failureCounter.getOrDefault(skillKey, new AtomicLong()).get());

            long totalExec = executionCounter.getOrDefault(skillKey, new AtomicLong()).get();
            long successExec = successCounter.getOrDefault(skillKey, new AtomicLong()).get();
            double successRate = totalExec > 0 ? (double) successExec / totalExec * 100 : 0;
            stats.put("successRate", successRate);

            long totalTime = totalExecutionTime.getOrDefault(skillKey, new AtomicLong()).get();
            double avgExecutionTime = totalExec > 0 ? (double) totalTime / totalExec : 0;
            stats.put("averageExecutionTimeMs", avgExecutionTime);
            stats.put("lastExecutionTimeMs", lastExecutionTime.getOrDefault(skillKey, new AtomicLong()).get());

            allStats.put(skillKey, stats);
        }

        return allStats;
    }

    /**
     * 生成性能指标
     * @param skill Skill对象
     * @return SkillMetric对象
     */
    public SkillMetric generateMetric(Skill skill) {
        Map<String, Object> stats = getExecutionStats(skill);

        SkillMetric metric = new SkillMetric();
        metric.setSkill(skill);
        metric.setMetricName("execution_rate");
        metric.setMetricValue((double) stats.getOrDefault("totalExecutions", 0L));
        metric.setMetricUnit("executions");
        metric.setMetricType("counter");
        metric.setCollectedAt(LocalDateTime.now());

        return metric;
    }

    /**
     * 定期收集指标
     */
    @Scheduled(fixedRateString = "${skills.monitoring.metrics-interval:10000}")
    public void collectMetrics() {
        log.debug("Collecting skill metrics");
        // 这里可以实现指标的持久化或发送到监控系统
    }

    /**
     * 重置统计
     * @param skill Skill对象
     */
    public void resetStats(Skill skill) {
        String skillKey = getSkillKey(skill);
        executionCounter.remove(skillKey);
        successCounter.remove(skillKey);
        failureCounter.remove(skillKey);
        totalExecutionTime.remove(skillKey);
        lastExecutionTime.remove(skillKey);
        errorCounter.remove(skillKey);
        log.info("Skill stats reset: {}", skillKey);
    }

    /**
     * 重置所有统计
     */
    public void resetAllStats() {
        executionCounter.clear();
        successCounter.clear();
        failureCounter.clear();
        totalExecutionTime.clear();
        lastExecutionTime.clear();
        errorCounter.clear();
        log.info("All skill stats reset");
    }

    /**
     * 获取Skill键
     * @param skill Skill对象
     * @return 键
     */
    private String getSkillKey(Skill skill) {
        return skill.getName() + ":" + skill.getVersion();
    }

    /**
     * 获取错误类型
     * @param errorMessage 错误信息
     * @return 错误类型
     */
    private String getErrorType(String errorMessage) {
        if (errorMessage == null) {
            return "unknown";
        }

        if (errorMessage.contains("timeout")) {
            return "timeout";
        } else if (errorMessage.contains("invalid")) {
            return "invalid_input";
        } else if (errorMessage.contains("not found")) {
            return "not_found";
        } else if (errorMessage.contains("permission")) {
            return "permission_denied";
        } else if (errorMessage.contains("connection")) {
            return "connection_error";
        } else {
            return "other";
        }
    }

}
