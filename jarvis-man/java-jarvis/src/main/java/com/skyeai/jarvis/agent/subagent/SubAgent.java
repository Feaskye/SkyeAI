package com.skyeai.jarvis.agent.subagent;

import com.skyeai.jarvis.agent.ChatMemory;
import com.skyeai.jarvis.agent.Message;
import com.skyeai.jarvis.agent.client.LlmClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 子代理类
 * 每个子代理拥有独立的ChatMemory实例，用于处理特定领域的任务
 */
@Slf4j
@Getter
public class SubAgent {
    
    /**
     * 子代理ID
     */
    private String id;
    
    /**
     * 子代理名称
     */
    private String name;
    
    /**
     * 系统提示（定义子代理的角色和行为）
     */
    private String systemPrompt;
    
    /**
     * 独立的记忆实例
     */
    private ChatMemory memory;
    
    /**
     * 子代理状态
     */
    private AgentState state;
    
    /**
     * 创建时间
     */
    private long createdAt;
    
    /**
     * 最后活跃时间
     */
    private long lastActiveTime;
    
    /**
     * LLM服务客户端
     */
    private LlmClient llmClient;
    
    /**
     * 状态枚举
     */
    public enum AgentState {
        IDLE,       // 空闲
        WORKING,    // 工作中
        COMPLETED,  // 已完成
        ERROR       // 错误
    }
    
    /**
     * 构造函数
     * @param id 子代理ID
     * @param name 子代理名称
     * @param systemPrompt 系统提示
     */
    public SubAgent(String id, String name, String systemPrompt) {
        this.id = id;
        this.name = name;
        this.systemPrompt = systemPrompt;
        this.memory = ChatMemory.forSubAgent(); // 创建独立的记忆实例
        this.memory.setSystemPrompt(systemPrompt);
        this.state = AgentState.IDLE;
        this.createdAt = System.currentTimeMillis();
        this.lastActiveTime = this.createdAt;
        log.debug("创建子代理: {} ({})", name, id);
    }
    
    /**
     * 构造函数（带LlmClient）
     * @param id 子代理ID
     * @param name 子代理名称
     * @param systemPrompt 系统提示
     * @param llmClient LLM服务客户端
     */
    public SubAgent(String id, String name, String systemPrompt, LlmClient llmClient) {
        this(id, name, systemPrompt);
        this.llmClient = llmClient;
    }
    
    /**
     * 设置LLM服务客户端
     * @param llmClient LLM服务客户端
     */
    public void setLlmClient(LlmClient llmClient) {
        this.llmClient = llmClient;
    }
    
    /**
     * 执行对话
     * @param message 用户消息
     * @return 子代理响应
     */
    public String chat(String message) {
        updateLastActiveTime();
        state = AgentState.WORKING;
        
        try {
            // 添加用户消息到记忆
            memory.addMessage(Message.user(message));
            
            // 获取消息列表
            List<Message> messages = memory.getMessages();
            
            // 模拟调用LLM生成响应（实际应用中应调用真实的LLM服务）
            String response = generateResponse(messages);
            
            // 添加助手消息到记忆
            memory.addMessage(Message.assistant(response));
            
            state = AgentState.IDLE;
            return response;
        } catch (Exception e) {
            log.error("子代理对话失败: {}", name, e);
            state = AgentState.ERROR;
            return "子代理执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取对话摘要
     * @return 摘要文本
     */
    public String getSummary() {
        return memory.getSummary();
    }
    
    /**
     * 获取消息历史
     * @return 消息列表
     */
    public List<Message> getHistory() {
        return memory.getMessages();
    }
    
    /**
     * 重置子代理（清空记忆）
     */
    public void reset() {
        memory.clear();
        memory.setSystemPrompt(systemPrompt);
        state = AgentState.IDLE;
        log.debug("重置子代理: {}", name);
    }
    
    /**
     * 更新最后活跃时间
     */
    private void updateLastActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }
    
    /**
     * 生成响应（通过LLM服务）
     * 如果LlmClient可用，调用真实LLM；否则使用降级策略
     */
    private String generateResponse(List<Message> messages) {
        // 如果LlmClient可用，调用真实LLM
        if (llmClient != null) {
            try {
                // 转换消息格式
                List<Map<String, String>> llmMessages = new ArrayList<>();
                for (Message msg : messages) {
                    Map<String, String> llmMsg = Map.of(
                        "role", msg.getMessageType().name().toLowerCase(),
                        "content", msg.getContent() != null ? msg.getContent() : ""
                    );
                    llmMessages.add(llmMsg);
                }
                
                // 调用LLM服务
                LlmClient.AgentResponse response = llmClient.chat(systemPrompt, llmMessages, List.of(), false);
                
                if (response != null && response.getContent() != null && !response.getContent().isBlank()) {
                    log.debug("子代理LLM调用成功，响应长度: {}", response.getContent().length());
                    return response.getContent();
                } else {
                    log.warn("子代理LLM返回空响应，使用降级策略");
                    return generateFallbackResponse(messages);
                }
            } catch (Exception e) {
                log.warn("子代理LLM调用失败，使用降级策略: {}", e.getMessage());
                return generateFallbackResponse(messages);
            }
        } else {
            // 使用降级策略
            log.debug("子代理未配置LlmClient，使用降级响应");
            return generateFallbackResponse(messages);
        }
    }
    
    /**
     * 降级响应生成（当LlmClient不可用时使用）
     */
    private String generateFallbackResponse(List<Message> messages) {
        StringBuilder response = new StringBuilder();
        response.append("[子代理: ").append(name).append("] ");
        
        // 简单回应用户消息
        if (!messages.isEmpty()) {
            Message lastMsg = messages.get(messages.size() - 1);
            if (lastMsg.getMessageType() == Message.MessageType.USER) {
                response.append("收到您的请求：\"").append(lastMsg.getContent()).append("\"");
                response.append("\n\n这是子代理的响应。我正在处理您的请求...");
            }
        }
        
        return response.toString();
    }
}