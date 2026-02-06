package com.skyeai.jarvis.skills.service;

import com.skyeai.jarvis.skills.model.Skill;
import com.skyeai.jarvis.skills.model.SkillExecution;

import java.util.List;
import java.util.Map;

/**
 * 工具适配器服务
 * 将工具功能适配到Skill服务中
 */
public interface ToolAdapterService {
    
    /**
     * 注册工具为Skill
     * @param toolDefinition 工具定义
     * @return 注册后的Skill
     */
    Skill registerTool(Map<String, Object> toolDefinition);
    
    /**
     * 注销工具
     * @param toolName 工具名称
     */
    void unregisterTool(String toolName);
    
    /**
     * 注销指定版本的工具
     * @param toolName 工具名称
     * @param version 工具版本
     */
    void unregisterTool(String toolName, String version);
    
    /**
     * 获取工具对应的Skill
     * @param toolName 工具名称
     * @return Skill对象
     */
    Skill getToolByName(String toolName);
    
    /**
     * 获取指定版本的工具
     * @param toolName 工具名称
     * @param version 工具版本
     * @return Skill对象
     */
    Skill getToolByName(String toolName, String version);
    
    /**
     * 获取所有工具对应的Skill
     * @return Skill列表
     */
    List<Skill> getAllTools();
    
    /**
     * 执行工具
     * @param toolName 工具名称
     * @param parameters 输入参数
     * @return 执行结果
     */
    SkillExecution executeTool(String toolName, Map<String, Object> parameters);
    
    /**
     * 执行指定版本的工具
     * @param toolName 工具名称
     * @param version 工具版本
     * @param parameters 输入参数
     * @return 执行结果
     */
    SkillExecution executeTool(String toolName, String version, Map<String, Object> parameters);
    
    /**
     * 批量执行工具
     * @param tasks 任务列表
     * @return 执行结果列表
     */
    List<SkillExecution> executeBatch(List<Map<String, Object>> tasks);
    
    /**
     * 从YAML文件加载工具配置
     * @param yamlPath YAML文件路径
     * @return 加载的工具数量
     */
    int loadToolsFromYaml(String yamlPath);
    
    /**
     * 加载默认工具配置
     * @return 加载的工具数量
     */
    int loadDefaultTools();
}
