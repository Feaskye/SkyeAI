package com.skyeai.jarvis.skills.service;

import com.skyeai.jarvis.skills.model.Skill;
import com.skyeai.jarvis.skills.model.SkillExecution;
import com.skyeai.jarvis.skills.model.SkillMetric;

import java.util.List;
import java.util.Map;

/**
 * Skill服务接口
 * 定义Skills的核心功能
 */
public interface SkillService {

    /**
     * 注册Skill
     * @param skill Skill对象
     * @return 注册后的Skill
     */
    Skill registerSkill(Skill skill);

    /**
     * 注销Skill
     * @param skillId Skill ID
     */
    void unregisterSkill(String skillId);

    /**
     * 获取Skill
     * @param skillId Skill ID
     * @return Skill对象
     */
    Skill getSkillById(String skillId);

    /**
     * 获取Skill
     * @param name Skill名称
     * @return Skill对象
     */
    Skill getSkillByName(String name);

    /**
     * 获取Skill
     * @param name Skill名称
     * @param version Skill版本
     * @return Skill对象
     */
    Skill getSkillByNameAndVersion(String name, String version);

    /**
     * 获取所有Skill
     * @return Skill列表
     */
    List<Skill> getAllSkills();

    /**
     * 获取所有活跃的Skill
     * @return Skill列表
     */
    List<Skill> getAllActiveSkills();

    /**
     * 搜索Skill
     * @param keyword 关键词
     * @return Skill列表
     */
    List<Skill> searchSkills(String keyword);

    /**
     * 执行Skill
     * @param skillId Skill ID
     * @param inputParameters 输入参数
     * @return SkillExecution对象
     */
    SkillExecution executeSkill(String skillId, Map<String, Object> inputParameters);

    /**
     * 执行Skill
     * @param name Skill名称
     * @param version Skill版本
     * @param inputParameters 输入参数
     * @return SkillExecution对象
     */
    SkillExecution executeSkill(String name, String version, Map<String, Object> inputParameters);

    /**
     * 异步执行Skill
     * @param skillId Skill ID
     * @param inputParameters 输入参数
     * @return 执行ID
     */
    String executeSkillAsync(String skillId, Map<String, Object> inputParameters);

    /**
     * 获取执行状态
     * @param executionId 执行ID
     * @return SkillExecution对象
     */
    SkillExecution getExecutionStatus(String executionId);

    /**
     * 取消执行
     * @param executionId 执行ID
     * @return 是否取消成功
     */
    boolean cancelExecution(String executionId);

    /**
     * 获取执行统计
     * @param skillId Skill ID
     * @return 统计信息
     */
    Map<String, Object> getExecutionStats(String skillId);

    /**
     * 获取所有执行统计
     * @return 统计信息
     */
    Map<String, Map<String, Object>> getAllExecutionStats();

    /**
     * 重置统计
     * @param skillId Skill ID
     */
    void resetStats(String skillId);

    /**
     * 重置所有统计
     */
    void resetAllStats();

    /**
     * 获取性能指标
     * @param skillId Skill ID
     * @return SkillMetric对象
     */
    SkillMetric getMetric(String skillId);

    /**
     * 获取所有性能指标
     * @return SkillMetric列表
     */
    List<SkillMetric> getAllMetrics();

    /**
     * 更新Skill
     * @param skill Skill对象
     * @return 更新后的Skill
     */
    Skill updateSkill(Skill skill);

    /**
     * 激活Skill
     * @param skillId Skill ID
     * @return 激活后的Skill
     */
    Skill activateSkill(String skillId);

    /**
     * 停用Skill
     * @param skillId Skill ID
     * @return 停用后的Skill
     */
    Skill deactivateSkill(String skillId);

}
