package com.example.mcpdemo;

import com.example.mcpdemo.common.DateTimeTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

//    https://mp.weixin.qq.com/s/1TVkxKTzd8TwF1W78NCt6g
    @Bean
    public ToolCallbackProvider tools(DateTimeTool tool){
        return MethodToolCallbackProvider.builder()
                .toolObjects(tool)
                .build();
    }
}
