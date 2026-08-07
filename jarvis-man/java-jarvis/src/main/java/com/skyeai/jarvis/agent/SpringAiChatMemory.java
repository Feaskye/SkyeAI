package com.skyeai.jarvis.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring AI 2.0 ChatMemory 实现（无状态单例 Bean）
 * v10 新增：三级存储联动（工作记忆 + Redis 短期记忆 + 向量库长期记忆）
 *
 * 架构说明：
 * - 工作记忆：内部 Map<String, SessionState> 维护当前会话上下文
 * - 短期记忆：委托 PersistentChatMemory 落 Redis（24h TTL）
 * - 长期记忆：委托 VectorMemoryStore 落向量库（永久，跨会话检索）
 * - 压缩算法：委托 ChatMemoryCompressor（保留 v9 三层压缩业务沉淀）
 */
@Slf4j
@Component("springAiChatMemory")
public class SpringAiChatMemory implements org.springframework.ai.chat.memory.ChatMemory {

    /** 工作记忆：conversationId → SessionState */
    private final Map<String, SessionState> sessions = new ConcurrentHashMap<>();

    /** 三层压缩器（无状态 Bean） */
    @Autowired
    private ChatMemoryCompressor compressor;

    /** Redis 短期记忆（保留 v9 实现） */
    @Autowired
    private PersistentChatMemory persistentMemory;

    /** 向量库长期记忆（v10 新增） */
    @Autowired
    private VectorMemoryStore vectorMemoryStore;

    @Value("${jarvis.memory.mode:hybrid}")
    private String memoryMode;

    @Override
    public void add(String conversationId, List<org.springframework.ai.chat.messages.Message> messages) {
        if (conversationId == null || messages == null || messages.isEmpty()) {
            return;
        }

        SessionState state = sessions.computeIfAbsent(conversationId, k -> new SessionState());

        // 1. 写工作记忆（转换 Spring AI Message → 项目 Message）
        for (org.springframework.ai.chat.messages.Message springMsg : messages) {
            Message internalMsg = convertFromSpringAi(springMsg);
            if (internalMsg != null) {
                state.getHistory().add(internalMsg);
            }
        }

        // 2. 触发三层压缩（委托给无状态压缩器）
        try {
            compressor.compressIfNeeded(state);
        } catch (Exception e) {
            log.error("记忆压缩失败 - conversationId: {}", conversationId, e);
        }

        // 3. 写 Redis 短期记忆（hybrid/redis 模式）
        if (isRedisEnabled()) {
            for (org.springframework.ai.chat.messages.Message springMsg : messages) {
                Message internalMsg = convertFromSpringAi(springMsg);
                if (internalMsg != null) {
                    persistentMemory.addMessage(conversationId, internalMsg);
                }
            }
        }

        // 4. 写向量库长期记忆（hybrid/vector 模式）
        if (isVectorEnabled()) {
            for (org.springframework.ai.chat.messages.Message springMsg : messages) {
                if (springMsg instanceof AssistantMessage || springMsg instanceof UserMessage) {
                    vectorMemoryStore.store(conversationId, springMsg);
                }
            }
        }
    }

    @Override
    public List<org.springframework.ai.chat.messages.Message> get(String conversationId) {
        if (conversationId == null) {
            return Collections.emptyList();
        }

        SessionState state = sessions.get(conversationId);
        if (state != null && !state.getHistory().isEmpty()) {
            return convertToSpringAi(state.getHistory());
        }

        // 回退 Redis
        if (isRedisEnabled()) {
            List<Message> redisMessages = persistentMemory.getMessages(conversationId);
            if (!redisMessages.isEmpty()) {
                // 重建工作记忆
                SessionState newState = new SessionState();
                newState.setHistory(redisMessages);
                sessions.put(conversationId, newState);
                return convertToSpringAi(redisMessages);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void clear(String conversationId) {
        sessions.remove(conversationId);
        if (isRedisEnabled()) {
            persistentMemory.deleteMemory(conversationId);
        }
        // 注意：向量库长期记忆不随会话清除，保留为永久记忆
        log.info("清除会话记忆 - conversationId: {}（向量库长期记忆保留）", conversationId);
    }

    /**
     * 获取或创建 SessionState（供 AgentCore 使用）
     */
    public SessionState getOrCreateState(String conversationId) {
        return sessions.computeIfAbsent(conversationId, k -> new SessionState());
    }

    /**
     * Spring AI Message → 项目 Message 转换
     */
    private Message convertFromSpringAi(org.springframework.ai.chat.messages.Message springMsg) {
        if (springMsg == null) return null;
        String text = springMsg.getText();
        if (springMsg instanceof UserMessage) {
            return Message.user(text);
        } else if (springMsg instanceof AssistantMessage) {
            return Message.assistant(text);
        } else if (springMsg instanceof SystemMessage) {
            return Message.system(text);
        } else if (springMsg instanceof ToolResponseMessage) {
            // 工具结果消息
            ToolResponseMessage trm = (ToolResponseMessage) springMsg;
            if (!trm.getResponses().isEmpty()) {
                String toolName = trm.getResponses().get(0).name();
                String result = trm.getResponses().get(0).responseData();
                return Message.toolResult(toolName, result);
            }
            return Message.toolResult("unknown", text);
        }
        // 默认按用户消息处理
        return Message.user(text);
    }

    /**
     * 项目 Message → Spring AI Message 转换
     */
    private List<org.springframework.ai.chat.messages.Message> convertToSpringAi(List<Message> messages) {
        List<org.springframework.ai.chat.messages.Message> result = new ArrayList<>();
        for (Message msg : messages) {
            switch (msg.getMessageType()) {
                case USER -> result.add(new UserMessage(msg.getContent()));
                case ASSISTANT -> result.add(new AssistantMessage(msg.getContent()));
                case SYSTEM -> result.add(new SystemMessage(msg.getContent()));
                case TOOL, TOOL_RESULT -> {
                    // 工具消息转换为 AssistantMessage（简化处理）
                    String content = msg.getToolResult() != null ? msg.getToolResult() : msg.getContent();
                    result.add(new AssistantMessage(content != null ? content : ""));
                }
            }
        }
        return result;
    }

    private boolean isRedisEnabled() {
        return "hybrid".equals(memoryMode) || "redis".equals(memoryMode);
    }

    private boolean isVectorEnabled() {
        return "hybrid".equals(memoryMode) || "vector".equals(memoryMode);
    }
}
