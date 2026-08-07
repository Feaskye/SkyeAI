package com.skyeai.jarvis.agent;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 对话记忆 POJO（v10 改造：移除 @Component，仅作为会话级实例被 ChatMemoryManager/SubAgent/PersistentChatMemory 通过 new 创建）
 *
 * 实现三层记忆压缩机制：
 * 1. 摘要压缩：当 SummaryCompressor 可用时调用 LLM 压缩历史（非 Spring 管理实例该字段为 null，自动跳过）
 * 2. Assistant消息裁剪：保留最近的 Assistant 消息，保护 TOOL/TOOL_RESULT 配对
 * 3. 滑动窗口：控制总消息数量（addMessage 兜底）
 *
 * 注意：v10 新链路使用 SpringAiChatMemory + SessionState + ChatMemoryCompressor，
 *      本类保留为自研 AgentCore/SubAgent 旧链路兼容。
 */
@Data
public class ChatMemory {

    /** 触发摘要压缩的消息阈值（默认 20） */
    private int compressThresholdMessages = 20;

    /** 压缩时保留最近消息的条数（默认 5） */
    private int preserveRecentMessages = 5;

    /** 滑动窗口：最大消息数量（默认 50） */
    private int maxMessages = 50;

    /** Assistant 消息裁剪：保留最近的条数（默认 10） */
    private int keepAssistantCount = 10;

    /** Assistant 消息裁剪触发阈值（默认 30） */
    private int assistantTrimThreshold = 30;

    /**
     * 摘要压缩器（可选）
     * - 通过 Spring 注入的 ChatMemory Bean 有该字段（v10 已移除 @Component，不再存在）
     * - 通过 new 创建的会话级实例该字段为 null，摘要压缩自动跳过
     */
    private SummaryCompressor summaryCompressor;

    /** 会话ID */
    private String sessionId;

    /** 摘要文本 */
    private String summaryText = "";

    /** 消息历史列表 */
    private List<Message> history = new ArrayList<>();

    /** 是否启用压缩 */
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

        // 滑动窗口兜底：超过最大消息数时移除最旧的
        while (history.size() > maxMessages) {
            history.remove(0);
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

        // 第一层：Assistant 消息裁剪（当消息数超过阈值时执行）
        if (history.size() > assistantTrimThreshold) {
            trimOldAssistantMessages();
        }

        // 第二层：摘要压缩（需 SummaryCompressor 可用；new 创建的 POJO 实例该字段为 null，自动跳过）
        if (summaryCompressor == null) {
            // 无压缩器时，依赖 addMessage 的滑动窗口兜底
            return;
        }

        // 计算压缩结束索引（保留最近的 preserveRecentMessages 条消息）
        int compressEndIndex = history.size() - preserveRecentMessages;

        // 保护 TOOL/TOOL_RESULT 配对完整性：不把配对的工具消息分开压缩
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
            return;
        }

        List<Message> messagesToCompress = new ArrayList<>(history.subList(0, compressEndIndex));
        String newSummary = summaryCompressor.compress(messagesToCompress, summaryText);

        if (newSummary != null && !newSummary.isBlank()) {
            this.summaryText = newSummary;
            history.subList(0, compressEndIndex).clear();
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

        // 从后往前遍历，标记需要移除的Assistant消息索引（保护TOOL/TOOL_RESULT配对）
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