package com.skyeai.jarvis.service.nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上下文管理器，用于管理对话上下文
 */
public class ContextManager {

    private final Map<String, List<String>> contextMap;
    private final int maxContextSize = 10; // 最大上下文大小

    public ContextManager() {
        this.contextMap = new HashMap<>();
    }

    /**
     * 获取对话上下文
     * @param conversationId 对话ID
     * @return 对话上下文
     */
    public List<String> getContext(String conversationId) {
        if (!contextMap.containsKey(conversationId)) {
            contextMap.put(conversationId, new ArrayList<>());
        }
        return contextMap.get(conversationId);
    }

    /**
     * 更新对话上下文
     * @param conversationId 对话ID
     * @param userInput 用户输入
     * @param systemResponse 系统响应
     */
    public void updateContext(String conversationId, String userInput, String systemResponse) {
        List<String> context = getContext(conversationId);
        
        // 添加用户输入和系统响应到上下文
        context.add("用户: " + userInput);
        context.add("系统: " + systemResponse);
        
        // 限制上下文大小
        while (context.size() > maxContextSize) {
            context.remove(0);
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
        return getContext(conversationId).size();
    }
}
