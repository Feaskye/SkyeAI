package com.skyeai.jarvis.skills.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Skill性能指标模型类
 * 存储Skill的性能监控数据
 */
@Data
@EqualsAndHashCode(of = {"id"})
public class SkillMetric {

    private String id;

    /**
     * 关联的Skill
     */
    private Skill skill;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标值
     */
    private Double metricValue;

    /**
     * 指标单位
     */
    private String metricUnit;

    /**
     * 指标类型
     */
    private String metricType;

    /**
     * 采集时间
     */
    private LocalDateTime collectedAt;

    /**
     * 指标标签
     */
    private String tags;

    /**
     * 执行环境
     */
    private String executionEnvironment;

    /**
     * 构造方法
     */
    public SkillMetric() {
        this.collectedAt = LocalDateTime.now();
    }

}
