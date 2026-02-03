package com.skyeai.jarvis.skills.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Skill执行记录模型类
 * 存储Skill的执行历史和结果
 */
@Data
@EqualsAndHashCode(of = {"id"})
public class SkillExecution {

    private String id;

    /**
     * 关联的Skill
     */
    private Skill skill;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 输入参数
     */
    private String inputParameters;

    /**
     * 输出结果
     */
    private String outputResult;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行开始时间
     */
    private LocalDateTime startTime;

    /**
     * 执行结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTimeMs;

    /**
     * 执行环境信息
     */
    private String executionEnvironment;

    /**
     * 调用方信息
     */
    private String callerInfo;

    /**
     * 执行ID
     */
    private String executionId;

    /**
     * 构造方法
     */
    public SkillExecution() {
        this.startTime = LocalDateTime.now();
        this.status = "PENDING";
    }

}
