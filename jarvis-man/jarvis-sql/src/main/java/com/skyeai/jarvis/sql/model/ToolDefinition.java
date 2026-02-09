package com.skyeai.jarvis.sql.model;

import lombok.Data;

import java.util.Map;

/**
 * 工具定义模型
 */
@Data
public class ToolDefinition {
    private String name;                 // 工具名称
    private String description;          // 工具描述
    private String version;              // 工具版本
    private Map<String, String> parameters; // 参数定义
    private String implementationClass;  // 实现类
    private String returnType;           // 返回类型
    private Map<String, String> metadata; // 元数据
    private boolean enabled;             // 是否启用
}
