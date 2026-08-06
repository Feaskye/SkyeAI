package com.skyeai.jarvis.agent.subagent;

import com.skyeai.jarvis.agent.client.LlmClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 子代理管理器
 * 负责创建、销毁和管理所有子代理
 * 支持动态创建和管理子代理的协作
 */
@Slf4j
@Component
public class SubAgentManager {
    
    /**
     * 子代理缓存
     */
    private final Map<String, SubAgent> subAgents = new ConcurrentHashMap<>();
    
    /**
     * LLM服务客户端
     */
    @Autowired
    private LlmClient llmClient;
    
    /**
     * 创建子代理
     * @param id 子代理ID
     * @param name 子代理名称
     * @param systemPrompt 系统提示
     * @return 创建的子代理实例
     */
    public SubAgent createSubAgent(String id, String name, String systemPrompt) {
        if (subAgents.containsKey(id)) {
            log.warn("子代理已存在: {}", id);
            return subAgents.get(id);
        }
        
        SubAgent subAgent = new SubAgent(id, name, systemPrompt, llmClient);
        subAgents.put(id, subAgent);
        log.info("创建子代理: {} ({})", name, id);
        return subAgent;
    }
    
    /**
     * 获取子代理
     * @param agentId 子代理ID
     * @return 子代理实例，如果不存在返回null
     */
    public SubAgent getSubAgent(String agentId) {
        return subAgents.get(agentId);
    }
    
    /**
     * 与子代理对话
     * @param agentId 子代理ID
     * @param message 消息内容
     * @return 子代理响应
     */
    public String chatWithSubAgent(String agentId, String message) {
        SubAgent subAgent = subAgents.get(agentId);
        if (subAgent == null) {
            log.warn("子代理不存在: {}", agentId);
            return "子代理不存在: " + agentId;
        }
        return subAgent.chat(message);
    }
    
    /**
     * 销毁子代理
     * @param agentId 子代理ID
     */
    public void destroySubAgent(String agentId) {
        SubAgent removed = subAgents.remove(agentId);
        if (removed != null) {
            log.info("销毁子代理: {} ({})", removed.getName(), agentId);
        }
    }
    
    /**
     * 检查子代理是否存在
     * @param agentId 子代理ID
     * @return 是否存在
     */
    public boolean hasSubAgent(String agentId) {
        return subAgents.containsKey(agentId);
    }
    
    /**
     * 获取所有子代理
     * @return 子代理列表
     */
    public List<SubAgent> getAllSubAgents() {
        return subAgents.values().stream().collect(Collectors.toList());
    }
    
    /**
     * 获取所有子代理ID
     * @return ID列表
     */
    public List<String> getAllSubAgentIds() {
        return subAgents.keySet().stream().collect(Collectors.toList());
    }
    
    /**
     * 获取子代理数量
     * @return 子代理数量
     */
    public int getSubAgentCount() {
        return subAgents.size();
    }
    
    /**
     * 销毁所有子代理
     */
    public void destroyAllSubAgents() {
        int count = subAgents.size();
        subAgents.clear();
        log.info("销毁所有子代理，数量: {}", count);
    }
    
    /**
     * 获取子代理摘要信息
     * @param agentId 子代理ID
     * @return 摘要信息
     */
    public Map<String, Object> getSubAgentSummary(String agentId) {
        SubAgent agent = subAgents.get(agentId);
        if (agent == null) {
            return Map.of("exists", false);
        }
        
        return Map.of(
            "exists", true,
            "id", agent.getId(),
            "name", agent.getName(),
            "state", agent.getState().name(),
            "messageCount", agent.getMemory().getMessageCount(),
            "createdAt", agent.getCreatedAt(),
            "lastActiveTime", agent.getLastActiveTime()
        );
    }
}