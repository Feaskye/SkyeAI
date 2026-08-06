package com.skyeai.jarvis.agent.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.function.Function;

/**
 * 工具回调类
 * 封装工具调用的相关信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallback {
    
    /**
     * 工具名称（用于LLM识别）
     */
    private String name;
    
    /**
     * 工具描述（用于LLM理解工具用途）
     */
    private String description;
    
    /**
     * 输入参数JSON Schema
     * 用于告诉LLM如何调用该工具
     */
    private String inputSchema;
    
    /**
     * 工具执行函数
     * 参数是Map<String, Object>类型，包含调用参数
     * 返回值是工具执行结果（字符串格式）
     */
    private Function<Map<String, Object>, String> function;
    
    /**
     * 工具类型
     */
    private String type;
    
    /**
     * 创建简单的工具回调
     * @param name 工具名称
     * @param description 工具描述
     * @param function 执行函数
     * @return ToolCallback实例
     */
    public static ToolCallback of(String name, String description, 
                                  Function<Map<String, Object>, String> function) {
        return ToolCallback.builder()
                .name(name)
                .description(description)
                .function(function)
                .build();
    }
    
    /**
     * 创建带Schema的工具回调
     * @param name 工具名称
     * @param description 工具描述
     * @param inputSchema 输入Schema
     * @param function 执行函数
     * @return ToolCallback实例
     */
    public static ToolCallback of(String name, String description, 
                                  String inputSchema, 
                                  Function<Map<String, Object>, String> function) {
        return ToolCallback.builder()
                .name(name)
                .description(description)
                .inputSchema(inputSchema)
                .function(function)
                .build();
    }
}