package com.skyeai.jarvis.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.skyeai.jarvis.agent.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * SAA ReactAgent 配置
 * v10 新增：Spring AI Alibaba Agent Framework 集成
 * 开关：jarvis.agent.framework=saa-react（默认）
 *
 * v10 修正：原 List<Object> toolBeans 参数导致 Spring 尝试创建所有 bean（包括 dataSource 等），
 *          任何 bean 创建失败都会阻断 ReactAgent 初始化。改用 ApplicationContext 按需扫描
 *          带 @Tool 注解方法的 bean，避免强制创建无关 bean。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "jarvis.agent.framework", havingValue = "saa-react", matchIfMissing = true)
public class ReactAgentConfig {

    @Bean
    public ReactAgent jarvisReactAgent(
            ChatClient.Builder chatClientBuilder,
            ToolRegistry toolRegistry,
            ApplicationContext applicationContext) {

        ChatClient chatClient = chatClientBuilder.build();

        // 按需扫描带 @Tool 注解方法的 bean，跳过无法创建的 bean（如依赖外部服务的 bean）
        List<Object> toolBeans = scanToolBeans(applicationContext);

        // 合并 Spring AI @Tool 工具 + 自研 InnerTool 工具
        ToolCallback[] springAiTools = ToolCallbacks.from(toolBeans.toArray());
        ToolCallback[] customTools = toolRegistry.getAllToolCallbacks().toArray(new ToolCallback[0]);
        ToolCallback[] allTools = Stream.concat(
                Arrays.stream(springAiTools), Arrays.stream(customTools))
                .distinct().toArray(ToolCallback[]::new);

        log.info("ReactAgent 初始化完成，工具总数: {}（Spring AI: {}, 自研: {}）",
                allTools.length, springAiTools.length, customTools.length);

        return ReactAgent.builder()
                .name("Jarvis")
                .chatClient(chatClient)
                .tools(allTools)
                .build();
    }

    /**
     * 扫描 ApplicationContext 中带 @Tool 注解方法的 bean。
     * 先通过 getType() 检查类型（不触发 bean 创建），再仅对匹配的 bean 调用 getBean()。
     */
    private List<Object> scanToolBeans(ApplicationContext applicationContext) {
        List<Object> toolBeans = new ArrayList<>();
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            try {
                Class<?> beanType = applicationContext.getType(beanName);
                if (beanType == null) {
                    continue;
                }
                if (hasToolAnnotation(beanType)) {
                    toolBeans.add(applicationContext.getBean(beanName));
                }
            } catch (Exception e) {
                // 跳过无法解析类型或创建失败的 bean（如依赖外部基础设施的 bean）
                log.debug("跳过 bean [{}] 的工具扫描: {}", beanName, e.getMessage());
            }
        }
        return toolBeans;
    }

    private boolean hasToolAnnotation(Class<?> beanType) {
        return Arrays.stream(beanType.getMethods())
                .anyMatch(m -> m.isAnnotationPresent(Tool.class));
    }
}
