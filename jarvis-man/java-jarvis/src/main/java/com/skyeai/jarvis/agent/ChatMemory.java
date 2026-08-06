package com.skyeai.jarvis.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 对话记忆类
 * 实现三层记忆压缩机制：
 * 1. 摘要压缩：调用LLM对历史对话进行摘要
 * 2. Assistant消息裁剪：保留最近的Assistant消息
 * 3. 滑动窗口：控制总消息数量
 */
@Slf4j
@Component
public class ChatMemory {
    
    /**
     * 触发压缩的消息阈值
     */
    @Value("${chat.memory.compress.threshold:20}")
    private int compressThresholdMessages;
    
    /**
     * 保留最近消息的数量
     */
    @Value("${chat.memory.preserve-recent:5}")
    private int preserveRecentMessages;
    
    /**
     * 最大消息数量
     */
    @Value("${chat.memory.max-messages:50}")
    private int maxMessages;
    
    /**
     * 保留最近的Assistant消息数量（用于Assistant消息裁剪层）
     */
    @Value("${chat.memory.assistant.keep-count:10}")
    private int keepAssistantCount;
    
    /**
     * Assistant消息裁剪触发阈值
     */
    @Value("${chat.memory.assistant.trim-threshold:30}")
    private int assistantTrimThreshold;
    
    /**
     * 摘要压缩器
     */
    @Autowired
    private SummaryCompressor summaryCompressor;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 摘要文本
     */
    private String summaryText = "";
    
    /**
     * 消息历史列表
     */
    private List<Message> history = new ArrayList<>();
    
    /**
     * 是否启用压缩
     */
    private boolean compressionEnabled = true;
    
    /**
     * 默认构造函数
     */
    public ChatMemory() {
    }
    
    /**
     * 创建子代理专用的记忆实例
     * @return 子代理记忆实例
     */
    public static ChatMemory forSubAgent() {
        ChatMemory memory = new ChatMemory();
        memory.setCompressionEnabled(false); // 子代理记忆默认不压缩
        return memory;
    }
    
    /**
     * 获取所有消息（自动触发压缩）
     * @return 消息列表
     */
    public List<Message> getMessages() {
        if (compressionEnabled) {
            compressIfNeeded();
        }
        return Collections.unmodifiableList(history);
    }
    
    /**
     * 获取消息数量
     * @return 消息数量
     */
    public int getMessageCount() {
        return history.size();
    }
    
    /**
     * 获取摘要文本
     * @return 摘要文本
     */
    public String getSummary() {
        return summaryText;
    }
    
    /**
     * 设置摘要文本
     * @param summary 摘要文本
     */
    public void setSummary(String summary) {
        this.summaryText = summary;
    }
    
    /**
     * 添加消息
     * @param message 消息
     */
    public void addMessage(Message message) {
        history.add(message);
        
        // 滑动窗口控制
        while (history.size() > maxMessages) {
            history.remove(0);
            log.debug("滑动窗口移除了最早的消息");
        }
    }
    
    /**
     * 设置系统提示
     * @param systemPrompt 系统提示
     */
    public void setSystemPrompt(String systemPrompt) {
        // 移除已存在的系统消息
        history.removeIf(m -> m.getMessageType() == Message.MessageType.SYSTEM);
        
        // 添加新的系统消息到最前面
        Message systemMsg = Message.system(systemPrompt);
        history.add(0, systemMsg);
    }
    
    /**
     * 检查是否需要压缩
     * 三层压缩机制：
     * 1. 摘要压缩：调用LLM对历史对话进行摘要
     * 2. Assistant消息裁剪：当消息过多时裁剪旧的Assistant消息
     * 3. 滑动窗口：控制总消息数量（在addMessage中处理）
     */
    private void compressIfNeeded() {
        if (history.size() <= compressThresholdMessages) {
            return;
        }
        
        log.debug("触发记忆压缩，当前消息数: {}", history.size());
        
        // 第一层：Assistant消息裁剪
        // 当消息数超过assistantTrimThreshold时，裁剪旧的Assistant消息
        if (history.size() > assistantTrimThreshold) {
            trimOldAssistantMessages();
        }
        
        // 第二层：摘要压缩
        // 计算压缩结束索引（保留最近的消息）
        int compressEndIndex = history.size() - preserveRecentMessages;
        
        // 保护TOOL消息的上下文完整性
        // 确保工具调用和工具结果不被分开压缩
        while (compressEndIndex > 0) {
            Message msg = history.get(compressEndIndex);
            if (msg.getMessageType() == Message.MessageType.TOOL || 
                msg.getMessageType() == Message.MessageType.TOOL_RESULT) {
                compressEndIndex--;
            } else {
                break;
            }
        }
        
        if (compressEndIndex <= 0) {
            log.debug("没有可压缩的消息");
            return;
        }
        
        // 获取需要压缩的消息
        List<Message> messagesToCompress = new ArrayList<>(history.subList(0, compressEndIndex));
        
        // 调用摘要压缩器
        String newSummary = summaryCompressor.compress(messagesToCompress, summaryText);
        
        if (newSummary != null && !newSummary.isBlank()) {
            this.summaryText = newSummary;
            // 移除已压缩的消息
            history.subList(0, compressEndIndex).clear();
            log.debug("记忆压缩完成，压缩后消息数: {}, 摘要长度: {}", 
                    history.size(), summaryText.length());
        }
    }
    
    /**
     * 裁剪旧的Assistant消息
     * 保留最近的keepAssistantCount条Assistant消息，裁剪更早的Assistant消息
     * 同时保护TOOL和TOOL_RESULT消息的完整性
     */
    private void trimOldAssistantMessages() {
        // 统计Assistant消息数量
        int assistantCount = 0;
        for (Message msg : history) {
            if (msg.getMessageType() == Message.MessageType.ASSISTANT) {
                assistantCount++;
            }
        }
        
        // 如果Assistant消息数量未超过限制，不需要裁剪
        if (assistantCount <= keepAssistantCount) {
            return;
        }
        
        log.debug("开始裁剪Assistant消息，当前数量: {}, 保留: {}", assistantCount, keepAssistantCount);
        
        // 从后往前遍历，标记需要保留的Assistant消息
        int keptCount = 0;
        List<Integer> toRemoveIndices = new ArrayList<>();
        List<Message> messages = new ArrayList<>(history);
        
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if (msg.getMessageType() == Message.MessageType.ASSISTANT) {
                keptCount++;
                if (keptCount > keepAssistantCount) {
                    // 需要裁剪这条Assistant消息
                    toRemoveIndices.add(i);
                    
                    // 同时需要保护与该Assistant消息配对的上下文
                    // 查找前面的USER消息和TOOL/TOOL_RESULT消息
                    int j = i - 1;
                    while (j >= 0) {
                        Message prevMsg = messages.get(j);
                        if (prevMsg.getMessageType() == Message.MessageType.USER ||
                            prevMsg.getMessageType() == Message.MessageType.TOOL ||
                            prevMsg.getMessageType() == Message.MessageType.TOOL_RESULT) {
                            // 如果前面是TOOL/TOOL_RESULT，也需要一并裁剪
                            if (prevMsg.getMessageType() == Message.MessageType.TOOL ||
                                prevMsg.getMessageType() == Message.MessageType.TOOL_RESULT) {
                                toRemoveIndices.add(j);
                                j--;
                            } else {
                                // 遇到USER消息就停止
                                break;
                            }
                        } else if (prevMsg.getMessageType() == Message.MessageType.ASSISTANT) {
                            // 遇到另一个ASSISTANT消息就停止
                            break;
                        } else {
                            j--;
                        }
                    }
                }
            }
        }
        
        // 从后往前移除标记的消息（避免索引变化）
        toRemoveIndices.sort((a, b) -> b - a);
        for (int index : toRemoveIndices) {
            if (index >= 0 && index < history.size()) {
                history.remove(index);
            }
        }
        
        log.debug("Assistant消息裁剪完成，移除消息数: {}, 剩余消息数: {}", 
                toRemoveIndices.size(), history.size());
    }
    
    /**
     * 清空记忆
     */
    public void clear() {
        history.clear();
        summaryText = "";
    }
    
    /**
     * 获取会话ID
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * 设置会话ID
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * 是否启用压缩
     */
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }
    
    /**
     * 设置是否启用压缩
     */
    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }
}