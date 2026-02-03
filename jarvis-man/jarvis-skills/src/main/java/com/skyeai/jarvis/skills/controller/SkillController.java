package com.skyeai.jarvis.skills.controller;

import com.skyeai.jarvis.skills.model.Skill;
import com.skyeai.jarvis.skills.model.SkillExecution;
import com.skyeai.jarvis.skills.model.SkillMetric;
import com.skyeai.jarvis.skills.service.SkillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Skill控制器
 * 暴露REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    /**
     * 注册Skill
     * @param skill Skill对象
     * @return 注册后的Skill
     */
    @PostMapping("/register")
    public ResponseEntity<Skill> registerSkill(@RequestBody Skill skill) {
        log.info("Registering skill: {}", skill.getName());
        try {
            Skill registeredSkill = skillService.registerSkill(skill);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredSkill);
        } catch (Exception e) {
            log.error("Error registering skill: {}", skill.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 注销Skill
     * @param skillId Skill ID
     * @return 响应
     */
    @DeleteMapping("/unregister/{skillId}")
    public ResponseEntity<Void> unregisterSkill(@PathVariable String skillId) {
        log.info("Unregistering skill: {}", skillId);
        try {
            skillService.unregisterSkill(skillId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            log.error("Error unregistering skill: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取Skill
     * @param skillId Skill ID
     * @return Skill对象
     */
    @GetMapping("/{skillId}")
    public ResponseEntity<Skill> getSkillById(@PathVariable String skillId) {
        log.info("Getting skill by ID: {}", skillId);
        try {
            Skill skill = skillService.getSkillById(skillId);
            if (skill == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(skill);
        } catch (Exception e) {
            log.error("Error getting skill: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取Skill
     * @param name Skill名称
     * @param version Skill版本
     * @return Skill对象
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Skill> getSkillByName(@PathVariable String name, 
                                              @RequestParam(required = false) String version) {
        log.info("Getting skill by name: {} version: {}", name, version);
        try {
            Skill skill;
            if (version != null) {
                skill = skillService.getSkillByNameAndVersion(name, version);
            } else {
                skill = skillService.getSkillByName(name);
            }
            if (skill == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(skill);
        } catch (Exception e) {
            log.error("Error getting skill: {} version: {}", name, version, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取所有Skill
     * @return Skill列表
     */
    @GetMapping("/all")
    public ResponseEntity<List<Skill>> getAllSkills() {
        log.info("Getting all skills");
        try {
            List<Skill> skills = skillService.getAllSkills();
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            log.error("Error getting all skills", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取所有活跃的Skill
     * @return Skill列表
     */
    @GetMapping("/active")
    public ResponseEntity<List<Skill>> getAllActiveSkills() {
        log.info("Getting all active skills");
        try {
            List<Skill> skills = skillService.getAllActiveSkills();
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            log.error("Error getting active skills", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 搜索Skill
     * @param keyword 关键词
     * @return Skill列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<Skill>> searchSkills(@RequestParam String keyword) {
        log.info("Searching skills with keyword: {}", keyword);
        try {
            List<Skill> skills = skillService.searchSkills(keyword);
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            log.error("Error searching skills with keyword: {}", keyword, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 执行Skill
     * @param skillId Skill ID
     * @param inputParameters 输入参数
     * @return SkillExecution对象
     */
    @PostMapping("/execute/{skillId}")
    public ResponseEntity<SkillExecution> executeSkill(@PathVariable String skillId, 
                                                    @RequestBody Map<String, Object> inputParameters) {
        log.info("Executing skill: {}", skillId);
        try {
            SkillExecution execution = skillService.executeSkill(skillId, inputParameters);
            return ResponseEntity.ok(execution);
        } catch (IllegalArgumentException e) {
            log.error("Error executing skill: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error executing skill: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 异步执行Skill
     * @param skillId Skill ID
     * @param inputParameters 输入参数
     * @return 执行ID
     */
    @PostMapping("/execute-async/{skillId}")
    public ResponseEntity<Map<String, String>> executeSkillAsync(@PathVariable String skillId, 
                                                             @RequestBody Map<String, Object> inputParameters) {
        log.info("Executing skill asynchronously: {}", skillId);
        try {
            String executionId = skillService.executeSkillAsync(skillId, inputParameters);
            Map<String, String> response = new java.util.HashMap<>();
            response.put("executionId", executionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error executing skill asynchronously: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error executing skill asynchronously: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取执行状态
     * @param executionId 执行ID
     * @return SkillExecution对象
     */
    @GetMapping("/execution/{executionId}")
    public ResponseEntity<SkillExecution> getExecutionStatus(@PathVariable String executionId) {
        log.info("Getting execution status: {}", executionId);
        try {
            SkillExecution execution = skillService.getExecutionStatus(executionId);
            if (execution == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(execution);
        } catch (Exception e) {
            log.error("Error getting execution status: {}", executionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 取消执行
     * @param executionId 执行ID
     * @return 是否取消成功
     */
    @PostMapping("/execution/{executionId}/cancel")
    public ResponseEntity<Map<String, Boolean>> cancelExecution(@PathVariable String executionId) {
        log.info("Cancelling execution: {}", executionId);
        try {
            boolean cancelled = skillService.cancelExecution(executionId);
            Map<String, Boolean> response = new java.util.HashMap<>();
            response.put("cancelled", cancelled);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling execution: {}", executionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取执行统计
     * @param skillId Skill ID
     * @return 统计信息
     */
    @GetMapping("/stats/{skillId}")
    public ResponseEntity<Map<String, Object>> getExecutionStats(@PathVariable String skillId) {
        log.info("Getting execution stats for skill: {}", skillId);
        try {
            Map<String, Object> stats = skillService.getExecutionStats(skillId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Error getting execution stats: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error getting execution stats: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取所有执行统计
     * @return 统计信息
     */
    @GetMapping("/stats/all")
    public ResponseEntity<Map<String, Map<String, Object>>> getAllExecutionStats() {
        log.info("Getting all execution stats");
        try {
            Map<String, Map<String, Object>> stats = skillService.getAllExecutionStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting all execution stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 重置统计
     * @param skillId Skill ID
     * @return 响应
     */
    @PostMapping("/stats/{skillId}/reset")
    public ResponseEntity<Void> resetStats(@PathVariable String skillId) {
        log.info("Resetting stats for skill: {}", skillId);
        try {
            skillService.resetStats(skillId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            log.error("Error resetting stats: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error resetting stats: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 重置所有统计
     * @return 响应
     */
    @PostMapping("/stats/reset-all")
    public ResponseEntity<Void> resetAllStats() {
        log.info("Resetting all stats");
        try {
            skillService.resetAllStats();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            log.error("Error resetting all stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 激活Skill
     * @param skillId Skill ID
     * @return 激活后的Skill
     */
    @PostMapping("/activate/{skillId}")
    public ResponseEntity<Skill> activateSkill(@PathVariable String skillId) {
        log.info("Activating skill: {}", skillId);
        try {
            Skill skill = skillService.activateSkill(skillId);
            return ResponseEntity.ok(skill);
        } catch (IllegalArgumentException e) {
            log.error("Error activating skill: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error activating skill: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 停用Skill
     * @param skillId Skill ID
     * @return 停用后的Skill
     */
    @PostMapping("/deactivate/{skillId}")
    public ResponseEntity<Skill> deactivateSkill(@PathVariable String skillId) {
        log.info("Deactivating skill: {}", skillId);
        try {
            Skill skill = skillService.deactivateSkill(skillId);
            return ResponseEntity.ok(skill);
        } catch (IllegalArgumentException e) {
            log.error("Error deactivating skill: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error deactivating skill: {}", skillId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
