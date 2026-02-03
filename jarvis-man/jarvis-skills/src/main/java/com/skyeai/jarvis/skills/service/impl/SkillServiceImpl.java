package com.skyeai.jarvis.skills.service.impl;

import com.skyeai.jarvis.skills.execution.SkillExecutor;
import com.skyeai.jarvis.skills.model.Skill;
import com.skyeai.jarvis.skills.model.SkillExecution;
import com.skyeai.jarvis.skills.model.SkillMetric;
import com.skyeai.jarvis.skills.monitoring.SkillMonitor;
import com.skyeai.jarvis.skills.registry.SkillRegistry;
import com.skyeai.jarvis.skills.service.SkillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Skill服务实现
 * 实现Skills的核心功能
 */
@Slf4j
@Service
public class SkillServiceImpl implements SkillService {

    @Autowired
    private SkillRegistry skillRegistry;

    @Autowired
    private SkillExecutor skillExecutor;

    @Autowired
    private SkillMonitor skillMonitor;

    @Override
    public Skill registerSkill(Skill skill) {
        log.info("Registering skill: {} version: {}", skill.getName(), skill.getVersion());
        return skillRegistry.registerSkill(skill);
    }

    @Override
    public void unregisterSkill(String skillId) {
        log.info("Unregistering skill: {}", skillId);
        skillRegistry.unregisterSkill(skillId);
    }

    @Override
    public Skill getSkillById(String skillId) {
        return skillRegistry.getSkillById(skillId);
    }

    @Override
    public Skill getSkillByName(String name) {
        return skillRegistry.getSkillByName(name);
    }

    @Override
    public Skill getSkillByNameAndVersion(String name, String version) {
        return skillRegistry.getSkillByNameAndVersion(name, version);
    }

    @Override
    public List<Skill> getAllSkills() {
        return skillRegistry.getAllSkills();
    }

    @Override
    public List<Skill> getAllActiveSkills() {
        return skillRegistry.getAllActiveSkills();
    }

    @Override
    public List<Skill> searchSkills(String keyword) {
        return skillRegistry.searchSkills(keyword);
    }

    @Override
    public SkillExecution executeSkill(String skillId, Map<String, Object> inputParameters) {
        Skill skill = getSkillById(skillId);
        if (skill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillId);
        }

        return executeSkillInternal(skill, inputParameters);
    }

    @Override
    public SkillExecution executeSkill(String name, String version, Map<String, Object> inputParameters) {
        Skill skill = getSkillByNameAndVersion(name, version);
        if (skill == null) {
            throw new IllegalArgumentException("Skill not found: " + name + " version: " + version);
        }

        return executeSkillInternal(skill, inputParameters);
    }

    @Override
    public String executeSkillAsync(String skillId, Map<String, Object> inputParameters) {
        Skill skill = getSkillById(skillId);
        if (skill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillId);
        }

        // 记录执行开始
        skillMonitor.recordExecutionStart(skill);

        // 创建执行函数
        Function<Map<String, Object>, Map<String, Object>> executionFunction = params -> {
            // 这里是Skill的具体执行逻辑
            // 实际应用中，这里会调用具体的Skill实现
            log.info("Executing skill: {} with parameters: {}", skill.getName(), params);

            // 模拟执行结果
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("status", "success");
            result.put("message", "Skill executed successfully");
            result.put("output", "Execution result");
            result.put("skillName", skill.getName());
            result.put("skillVersion", skill.getVersion());

            return result;
        };

        // 异步执行
        String executionId = skillExecutor.executeSkillAsync(skill, inputParameters, executionFunction);

        return executionId;
    }

    @Override
    public SkillExecution getExecutionStatus(String executionId) {
        return skillExecutor.getExecutionStatus(executionId);
    }

    @Override
    public boolean cancelExecution(String executionId) {
        return skillExecutor.cancelExecution(executionId);
    }

    @Override
    public Map<String, Object> getExecutionStats(String skillId) {
        Skill skill = getSkillById(skillId);
        if (skill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillId);
        }

        return skillMonitor.getExecutionStats(skill);
    }

    @Override
    public Map<String, Map<String, Object>> getAllExecutionStats() {
        return skillMonitor.getAllExecutionStats();
    }

    @Override
    public void resetStats(String skillId) {
        Skill skill = getSkillById(skillId);
        if (skill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillId);
        }

        skillMonitor.resetStats(skill);
    }

    @Override
    public void resetAllStats() {
        skillMonitor.resetAllStats();
    }

    @Override
    public SkillMetric getMetric(String skillId) {
        Skill skill = getSkillById(skillId);
        if (skill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillId);
        }

        return skillMonitor.generateMetric(skill);
    }

    @Override
    public List<SkillMetric> getAllMetrics() {
        // 实际应用中，这里会从数据库或监控系统获取所有指标
        return List.of();
    }

    @Override
    public Skill updateSkill(Skill skill) {
        log.info("Updating skill: {} version: {}", skill.getName(), skill.getVersion());
        return skillRegistry.registerSkill(skill); // 利用注册方法更新
    }

    @Override
    public Skill activateSkill(String skillId) {
        Skill skill = getSkillById(skillId);
        if (skill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillId);
        }

        skill.setStatus("ACTIVE");
        return skillRegistry.registerSkill(skill);
    }

    @Override
    public Skill deactivateSkill(String skillId) {
        Skill skill = getSkillById(skillId);
        if (skill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillId);
        }

        skill.setStatus("INACTIVE");
        return skillRegistry.registerSkill(skill);
    }

    /**
     * 内部执行Skill
     * @param skill Skill对象
     * @param inputParameters 输入参数
     * @return SkillExecution对象
     */
    private SkillExecution executeSkillInternal(Skill skill, Map<String, Object> inputParameters) {
        // 检查Skill状态
        if (!"ACTIVE".equals(skill.getStatus())) {
            throw new IllegalStateException("Skill is not active: " + skill.getName());
        }

        // 记录执行开始
        skillMonitor.recordExecutionStart(skill);

        // 创建执行函数
        Function<Map<String, Object>, Map<String, Object>> executionFunction = params -> {
            // 这里是Skill的具体执行逻辑
            // 实际应用中，这里会调用具体的Skill实现
            log.info("Executing skill: {} with parameters: {}", skill.getName(), params);

            // 模拟执行结果
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("status", "success");
            result.put("message", "Skill executed successfully");
            result.put("output", "Execution result");
            result.put("skillName", skill.getName());
            result.put("skillVersion", skill.getVersion());

            return result;
        };

        // 执行Skill
        SkillExecution execution = skillExecutor.executeSkill(skill, inputParameters, executionFunction);

        // 记录执行完成
        skillMonitor.recordExecutionComplete(execution);

        return execution;
    }

}
