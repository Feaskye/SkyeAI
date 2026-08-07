package com.skyeai.jarvis.sql.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * SAA DataAgent 配置
 * v10 新增：Spring AI Alibaba DataAgent 集成（首选 Text-to-SQL 方案）
 * 开关：jarvis.text-to-sql.engine=dataagent（默认）
 * 注意：DataAgent 在 M1.1 版本可能未发布 GA，若不可用则回退 supersql
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "jarvis.text-to-sql.engine", havingValue = "dataagent", matchIfMissing = true)
public class DataAgentConfig {
    // DataAgent Bean 在 SAA 2.0.0-M1.1 正式发布后在此装配
    // 当前版本使用 SuperSqlTextToSqlService 作为备选实现
}
