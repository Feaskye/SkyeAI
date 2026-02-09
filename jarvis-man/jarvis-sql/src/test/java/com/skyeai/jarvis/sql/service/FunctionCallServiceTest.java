package com.skyeai.jarvis.sql.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class FunctionCallServiceTest {

    @Autowired
    private FunctionCallService functionCallService;

    @Test
    public void testRegisterTool() {
        // 测试注册工具
        FunctionCallService.ToolDefinition tool = new FunctionCallService.ToolDefinition();
        tool.setName("testTool");
        tool.setDescription("测试工具");
        tool.setVersion("1.0");
        tool.setClassName("com.skyeai.jarvis.sql.service.impl.TestToolImpl");
        tool.setParameters(new ArrayList<>());

        functionCallService.registerTool(tool);
        System.out.println("测试注册工具结果: 成功");
    }

    @Test
    public void testGetTool() {
        // 测试获取工具
        FunctionCallService.ToolDefinition tool = functionCallService.getToolByName("testTool", "1.0");
        if (tool != null) {
            System.out.println("测试获取工具结果: " + tool.getName() + " v" + tool.getVersion());
        } else {
            System.out.println("测试获取工具结果: 未找到工具");
        }
    }

    @Test
    public void testListTools() {
        // 测试列出所有工具
        List<FunctionCallService.ToolDefinition> tools = functionCallService.getAllTools();
        System.out.println("测试列出所有工具结果:");
        tools.forEach(tool -> {
            System.out.println("工具: " + tool.getName() + " v" + tool.getVersion() + " - " + tool.getDescription());
        });
    }

    @Test
    public void testUnregisterTool() {
        // 测试注销工具
        functionCallService.unregisterTool("testTool", "1.0");
        System.out.println("测试注销工具结果: 成功");
    }
}
