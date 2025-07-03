package com.example.mcpdemo.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/service")
public class McpClientController {

    private ChatClient chatClient;

    public McpClientController(ChatClient.Builder builder, ToolCallbackProvider tools){
        this.chatClient=builder.defaultToolCallbacks(tools).build();
    }


    Flux<String> queryCurrentTime(@PathVariable String country){
        return this.chatClient.prompt(new PromptTemplate("调用本地工具查询国家{country}当前时间")
                  .create(Map.of("country",country)))
                .stream()
                .content();
    }



}
