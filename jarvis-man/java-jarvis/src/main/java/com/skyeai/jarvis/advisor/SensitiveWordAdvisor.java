package com.skyeai.jarvis.advisor;

import com.skyeai.jarvis.agent.security.ContentSecurityFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 敏感词拦截 Advisor
 * v10 新增：Spring AI 2.0 Advisor 链
 * v10 修正：Spring AI 2.0 API 迁移 CallAroundAdvisor → CallAdvisor
 * - 输入拦截：命中敏感词则抛异常，阻断请求
 * - 输出拦截：命中敏感词则替换为脱敏文本（***）
 */
@Slf4j
@Component
public class SensitiveWordAdvisor implements CallAdvisor {

    private final ContentSecurityFilter contentSecurityFilter;

    public SensitiveWordAdvisor(ContentSecurityFilter contentSecurityFilter) {
        this.contentSecurityFilter = contentSecurityFilter;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // 输入拦截：检查用户输入，命中敏感词则抛异常
        String userInput = request.prompt().getInstructions().stream()
                .filter(m -> m.getMessageType() == MessageType.USER)
                .map(Message::getText)
                .findFirst()
                .orElse("");
        ContentSecurityFilter.SecurityResult inputResult = contentSecurityFilter.filterInput(userInput);
        if (inputResult.isBlocked()) {
            log.warn("{\"event\":\"sensitive_input_blocked\",\"reason\":\"{}\"}",
                    escape(inputResult.getReason()));
            throw new IllegalStateException("输入被安全过滤拦截: " + inputResult.getReason());
        }

        // 输出拦截：检查响应文本，命中敏感词则替换为脱敏文本
        ChatClientResponse response = chain.nextCall(request);
        String outputText = extractOutputText(response);
        ContentSecurityFilter.SecurityResult outputResult = contentSecurityFilter.filterOutput(outputText);
        if (outputResult.isBlocked()) {
            log.warn("{\"event\":\"sensitive_output_masked\",\"reason\":\"{}\"}",
                    escape(outputResult.getReason()));
            String masked = contentSecurityFilter.maskWord(outputText);
            return replaceOutputText(response, masked);
        }
        return response;
    }

    @Override
    public String getName() {
        return "SensitiveWordAdvisor";
    }

    @Override
    public int getOrder() {
        return 2;
    }

    /**
     * 从 ChatClientResponse 中提取响应文本
     * Spring AI 2.0：chatResponse() -> ChatResponse -> getResult() -> Generation -> getOutput() -> AssistantMessage.getText()
     */
    private String extractOutputText(ChatClientResponse response) {
        try {
            if (response == null || response.chatResponse() == null
                    || response.chatResponse().getResult() == null
                    || response.chatResponse().getResult().getOutput() == null) {
                return "";
            }
            return response.chatResponse().getResult().getOutput().getText();
        } catch (Exception e) {
            log.warn("{\"event\":\"extract_output_text_failed\",\"error\":\"{}\"}", escape(e.getMessage()));
            return "";
        }
    }

    /**
     * 用脱敏后的文本重建 ChatClientResponse
     * Spring AI 2.0 API：保留原 context，仅替换 AssistantMessage 文本
     */
    private ChatClientResponse replaceOutputText(ChatClientResponse response, String maskedText) {
        ChatResponse original = response.chatResponse();
        AssistantMessage newMessage = new AssistantMessage(maskedText);
        Generation newGeneration = new Generation(newMessage);
        ChatResponse newResponse = new ChatResponse(List.of(newGeneration), original.getMetadata());
        return new ChatClientResponse(newResponse, response.context());
    }

    /**
     * 简单转义日志中的双引号与反斜杠，避免破坏 JSON 结构
     */
    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
