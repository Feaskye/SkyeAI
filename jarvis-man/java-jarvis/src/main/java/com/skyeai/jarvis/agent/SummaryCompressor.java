package com.skyeai.jarvis.agent;

import com.skyeai.jarvis.agent.client.LlmClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 摘要压缩器
 * 调用LLM对历史对话进行摘要总结
 * 通过LlmClient调用jarvis-llm服务的memory-summary端点
 */
@Slf4j
@Component
public class SummaryCompressor {

    @Value("${chat.memory.compress.prompt-template:请对以下对话进行简洁的总结，保留关键信息：\n\n%s}")
    private String compressPromptTemplate;

    @Value("${chat.memory.compress.max-length:500}")
    private int maxLength;

    @Autowired
    private LlmClient llmClient;

    /**
     * 压缩对话历史为摘要
     * @param messages 待压缩的消息列表
     * @param existingSummary 已存在的摘要（用于增量更新）
     * @return 压缩后的摘要
     */
    public String compress(List<Message> messages, String existingSummary) {
        if (messages == null || messages.isEmpty()) {
            return existingSummary;
        }

        // 构建消息列表转换为LLM服务格式
        List<Map<String, String>> llmMessages = new ArrayList<>();

        // 如果已有摘要，作为系统上下文添加
        if (existingSummary != null && !existingSummary.isBlank()) {
            llmMessages.add(Map.of("role", "system", "content", "已有的对话摘要：" + existingSummary));
        }

        // 将消息转换为LLM格式
        for (Message msg : messages) {
            String role = switch (msg.getMessageType()) {
                case USER -> "user";
                case ASSISTANT -> "assistant";
                case SYSTEM -> "system";
                case TOOL -> "user";
                case TOOL_RESULT -> "assistant";
            };

            String content = msg.getContent();
            if (msg.getMessageType() == Message.MessageType.TOOL) {
                content = "工具调用: " + msg.getToolName() + ", 参数: " + msg.getToolParameters();
                role = "user";
            } else if (msg.getMessageType() == Message.MessageType.TOOL_RESULT) {
                content = "工具[" + msg.getToolName() + "]结果: " + msg.getToolResult();
                role = "assistant";
            }

            llmMessages.add(Map.of("role", role, "content", content));
        }

        // 调用jarvis-llm服务生成摘要
        try {
            String summary = llmClient.generateMemorySummary(llmMessages, maxLength);
            if (summary != null && !summary.isBlank()) {
                log.debug("摘要压缩完成，原消息数: {}, 摘要长度: {}", messages.size(), summary.length());
                return summary;
            } else {
                log.warn("LLM返回空摘要，使用降级策略");
                return fallbackCompress(messages, existingSummary);
            }
        } catch (Exception e) {
            log.error("摘要压缩失败，使用降级策略", e);
            return fallbackCompress(messages, existingSummary);
        }
    }

    /**
     * 降级压缩策略（LLM调用失败时使用）
     * 简单提取用户和助手的关键内容
     */
    private String fallbackCompress(List<Message> messages, String existingSummary) {
        StringBuilder summary = new StringBuilder();
        if (existingSummary != null && !existingSummary.isBlank()) {
            summary.append(existingSummary).append("\n");
        }

        summary.append("对话摘要：");
        for (Message msg : messages) {
            if (msg.getMessageType() == Message.MessageType.USER ||
                msg.getMessageType() == Message.MessageType.ASSISTANT) {
                String content = msg.getContent();
                if (content != null && content.length() > 0) {
                    String prefix = msg.getMessageType() == Message.MessageType.USER ? "用户" : "助手";
                    summary.append(prefix).append(": ")
                           .append(content.length() > 100 ? content.substring(0, 100) + "..." : content)
                           .append("；");
                    if (summary.length() > 400) {
                        break;
                    }
                }
            }
        }

        return summary.toString();
    }
}