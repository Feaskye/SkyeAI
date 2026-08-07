package com.skyeai.jarvis.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 记忆压缩器（无状态 Bean）
 * v10 新增：从 ChatMemory 拆分出的三层压缩算法
 * 保留 v9 的 TOOL/TOOL_RESULT 配对保护逻辑（业务沉淀）
 */
@Slf4j
@Component
public class ChatMemoryCompressor {

    @Autowired
    private SummaryCompressor summaryCompressor;

    @Value("${chat.memory.compress.threshold:20}")
    private int compressThresholdMessages;

    @Value("${chat.memory.preserve-recent:5}")
    private int preserveRecentMessages;

    @Value("${chat.memory.assistant.keep-count:10}")
    private int keepAssistantCount;

    @Value("${chat.memory.assistant.trim-threshold:30}")
    private int assistantTrimThreshold;

    @Value("${chat.memory.max-messages:50}")
    private int maxMessages;

    /**
     * 对 SessionState 执行三层压缩
     * 第一层：Assistant 消息裁剪 → 第二层：摘要压缩 → 第三层：滑动窗口兜底
     */
    public void compressIfNeeded(SessionState state) {
        if (!state.isCompressionEnabled()) {
            return;
        }

        List<Message> history = state.getHistory();
        if (history.size() <= compressThresholdMessages) {
            return;
        }

        log.debug("触发记忆压缩，当前消息数: {}", history.size());

        // 第一层：Assistant 消息裁剪
        if (history.size() > assistantTrimThreshold) {
            trimOldAssistantMessages(history);
        }

        // 第二层：摘要压缩（保护 TOOL/TOOL_RESULT 配对）
        int compressEndIndex = history.size() - preserveRecentMessages;
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

        List<Message> messagesToCompress = new ArrayList<>(history.subList(0, compressEndIndex));
        String newSummary = summaryCompressor.compress(messagesToCompress, state.getSummaryText());

        if (newSummary != null && !newSummary.isBlank()) {
            state.setSummaryText(newSummary);
            history.subList(0, compressEndIndex).clear();
            log.debug("记忆压缩完成，压缩后消息数: {}, 摘要长度: {}",
                    history.size(), state.getSummaryText().length());
        }

        // 第三层：滑动窗口兜底
        while (history.size() > maxMessages) {
            history.remove(0);
            log.debug("滑动窗口移除了最早的消息");
        }
    }

    /**
     * v9 沉淀：裁剪旧 Assistant 消息时保护配对的 TOOL/TOOL_RESULT
     */
    private void trimOldAssistantMessages(List<Message> history) {
        int assistantCount = 0;
        for (Message msg : history) {
            if (msg.getMessageType() == Message.MessageType.ASSISTANT) {
                assistantCount++;
            }
        }

        if (assistantCount <= keepAssistantCount) {
            return;
        }

        log.debug("开始裁剪Assistant消息，当前数量: {}, 保留: {}", assistantCount, keepAssistantCount);

        int keptCount = 0;
        List<Integer> toRemoveIndices = new ArrayList<>();
        List<Message> messages = new ArrayList<>(history);

        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if (msg.getMessageType() == Message.MessageType.ASSISTANT) {
                keptCount++;
                if (keptCount > keepAssistantCount) {
                    toRemoveIndices.add(i);
                    // 保护配对的 TOOL/TOOL_RESULT
                    int j = i - 1;
                    while (j >= 0) {
                        Message prevMsg = messages.get(j);
                        if (prevMsg.getMessageType() == Message.MessageType.TOOL ||
                            prevMsg.getMessageType() == Message.MessageType.TOOL_RESULT) {
                            toRemoveIndices.add(j);
                            j--;
                        } else if (prevMsg.getMessageType() == Message.MessageType.USER) {
                            toRemoveIndices.add(j);
                            break;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        toRemoveIndices.sort((a, b) -> b - a);
        for (int index : toRemoveIndices) {
            if (index >= 0 && index < history.size()) {
                history.remove(index);
            }
        }

        log.debug("Assistant消息裁剪完成，移除消息数: {}, 剩余消息数: {}",
                toRemoveIndices.size(), history.size());
    }
}
