package com.skyeai.jarvis.llm.service.nlp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 上下文管理器，用于管理对话上下文
 */
public class ContextManager {

    // 最大上下文长度
    private static final int MAX_CONTEXT_LENGTH = 10;

    // 对话上下文映射
    private Map<String, LinkedList<String>> contextMap;

    public ContextManager() {
        this.contextMap = new HashMap<>();
    }

    /**
     * 获取对话上下文
     * @param conversationId 对话ID
     * @return 对话上下文列表
     */
    public List<String> getContext(String conversationId) {
        if (!contextMap.containsKey(conversationId)) {
            contextMap.put(conversationId, new LinkedList<>());
        }
        return new LinkedList<>(contextMap.get(conversationId));
    }

    /**
     * 更新对话上下文
     * @param conversationId 对话ID
     * @param userInput 用户输入
     * @param assistantResponse 助手响应
     */
    public void updateContext(String conversationId, String userInput, String assistantResponse) {
        if (!contextMap.containsKey(conversationId)) {
            contextMap.put(conversationId, new LinkedList<>());
        }

        LinkedList<String> context = contextMap.get(conversationId);

        // 添加用户输入和助手响应到上下文
        context.add("用户: " + userInput);
        context.add("助手: " + assistantResponse);

        // 保持上下文长度不超过最大值
        while (context.size() > MAX_CONTEXT_LENGTH) {
            context.removeFirst();
        }
    }

    /**
     * 清除对话上下文
     * @param conversationId 对话ID
     */
    public void clearContext(String conversationId) {
        contextMap.remove(conversationId);
    }

    /**
     * 获取上下文大小
     * @param conversationId 对话ID
     * @return 上下文大小
     */
    public int getContextSize(String conversationId) {
        if (!contextMap.containsKey(conversationId)) {
            return 0;
        }
        return contextMap.get(conversationId).size();
    }

    /**
     * 检查对话是否存在
     * @param conversationId 对话ID
     * @return 是否存在
     */
    public boolean hasConversation(String conversationId) {
        return contextMap.containsKey(conversationId);
    }

    /**
     * 清理所有上下文
     */
    public void clearAllContexts() {
        contextMap.clear();
    }
}
