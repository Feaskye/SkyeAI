package com.skyeai.jarvis.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Component;

/**
 * 日志 Advisor
 * v10 新增：Spring AI 2.0 Advisor 链，结构化 JSON 日志
 * v10 修正：Spring AI 2.0 API 迁移 CallAroundAdvisor → CallAdvisor
 */
@Slf4j
@Component
public class LoggingAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long start = System.currentTimeMillis();
        String userText = request.prompt().getInstructions().stream()
                .filter(m -> m.getMessageType() == MessageType.USER)
                .map(Message::getText)
                .findFirst()
                .orElse("");
        log.info("{\"event\":\"chat_request\",\"user_text_length\":{}}",
                userText != null ? userText.length() : 0);
        try {
            ChatClientResponse response = chain.nextCall(request);
            long duration = System.currentTimeMillis() - start;
            log.info("{\"event\":\"chat_response\",\"duration\":{}}", duration);
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("{\"event\":\"chat_error\",\"duration\":{},\"error\":\"{}\"}",
                    duration, e.getMessage());
            throw e;
        }
    }

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
