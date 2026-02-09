package com.skyeai.jarvis.service.chat.impl;

import com.skyeai.jarvis.service.chat.SmartChatService;
import com.skyeai.jarvis.service.nlp.ConversationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class SmartChatServiceImplTest {

    @Autowired
    private SmartChatService smartChatService;

    @Test
    public void testChatWithRealTimeData() {
        // 测试包含实时数据的对话
        String message = "今天北京的天气怎么样？";
        String sessionId = "test-session-1";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试实时数据对话结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithFunctionCall() {
        // 测试包含函数调用的对话
        String message = "计算12345乘以67890的结果";
        String sessionId = "test-session-2";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试函数调用对话结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithStockMarket() {
        // 测试股市实时数据
        String message = "今日大盘走势如何？";
        String sessionId = "test-session-3";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试股市实时数据结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithMiddleEastSituation() {
        // 测试中东局势
        String message = "中东局势最新进展如何？";
        String sessionId = "test-session-4";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试中东局势结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithPhotovoltaicNews() {
        // 测试光伏行业消息
        String message = "光伏行业最新消息有哪些？";
        String sessionId = "test-session-5";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试光伏行业消息结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithTechnologyNews() {
        // 测试科技行业消息
        String message = "科技行业最新动态是什么？";
        String sessionId = "test-session-6";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试科技行业消息结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithRealEstate() {
        // 测试房地产行业
        String message = "近期房地产市场走势如何？";
        String sessionId = "test-session-7";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试房地产行业结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithHealthcare() {
        // 测试医疗健康行业
        String message = "医疗健康行业最新研究成果有哪些？";
        String sessionId = "test-session-8";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试医疗健康行业结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithEducation() {
        // 测试教育行业
        String message = "教育行业最新政策是什么？";
        String sessionId = "test-session-9";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试教育行业结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithEnergy() {
        // 测试能源行业
        String message = "能源行业最新动态是什么？";
        String sessionId = "test-session-10";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试能源行业结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithAutomotive() {
        // 测试汽车行业
        String message = "汽车行业最新发展趋势是什么？";
        String sessionId = "test-session-11";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试汽车行业结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithGeneralQuery() {
        // 测试普通查询对话
        String message = "你好，介绍一下你自己";
        String sessionId = "test-session-12";
        boolean useRealtimeData = false;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试普通查询对话结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }

    @Test
    public void testChatWithIPhone17() {
        // 测试iPhone 17相关信息
        String message = "iPhone 17的最新消息是什么？";
        String sessionId = "test-session-13";
        boolean useRealtimeData = true;

        // 调用方法
        ConversationResult result = smartChatService.chat(message, sessionId, useRealtimeData);

        // 打印结果
        System.out.println("测试iPhone 17实时数据结果:");
        System.out.println("用户消息: " + message);
        System.out.println("AI响应: " + result.getResponse());
        System.out.println("是否完成: " + result.isComplete());
        System.out.println("意图: " + result.getIntent());
        System.out.println("置信度: " + result.getConfidence());
    }
} 
