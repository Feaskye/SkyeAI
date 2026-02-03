package com.skyeai.jarvis.skills.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Skill模型类
 * 存储Skill的元数据信息
 */
@Data
@EqualsAndHashCode(of = {"id"})
public class Skill {

    private String id;

    /**
     * Skill名称
     */
    private String name;

    /**
     * Skill版本
     */
    private String version;

    /**
     * Skill描述
     */
    private String description;

    /**
     * Skill类型
     */
    private String type;

    /**
     * Skill状态
     */
    private String status;

    /**
     * 输入参数定义
     */
    private String inputSchema;

    /**
     * 输出参数定义
     */
    private String outputSchema;

    /**
     * 依赖关系
     */
    private String dependencies;

    /**
     * 配置信息
     */
    private String configuration;

    /**
     * 执行环境
     */
    private String executionEnvironment;

    /**
     * 资源限制
     */
    private String resourceLimits;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 执行记录
     */
    private List<SkillExecution> executions;

    /**
     * 性能指标
     */
    private List<SkillMetric> metrics;

    /**
     * 构造方法
     */
    public Skill() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

} 