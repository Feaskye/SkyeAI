package com.skyeai.jarvis.llm.service.nlp;

import com.skyeai.jarvis.llm.service.AliyunAIService;
import com.skyeai.jarvis.llm.service.LlmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 意图增强器，用于增强意图识别
 */
public class IntentEnhancer {

    private final AliyunAIService aliyunAIService;
    private final LlmService llmService;

    public IntentEnhancer(AliyunAIService aliyunAIService, LlmService llmService) {
        this.aliyunAIService = aliyunAIService;
        this.llmService = llmService;
    }

    /**
     * 增强意图识别
     * @param text 输入文本
     * @param context 对话上下文
     * @return 意图识别结果
     * @throws Exception 异常
     */
    public IntentResult enhanceIntentRecognition(String text, List<String> context) throws Exception {
        // 构建意图识别提示
        String prompt = buildIntentRecognitionPrompt(text, context);

        // 使用AI模型进行意图识别
        String result = aliyunAIService.generateResponse(prompt);

        // 解析意图识别结果
        return parseIntentResult(result);
    }

    /**
     * 构建意图识别提示
     * @param text 输入文本
     * @param context 对话上下文
     * @return 意图识别提示
     */
    private String buildIntentRecognitionPrompt(String text, List<String> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请识别以下用户输入的意图，并提取相关实体：\n\n");

        if (!context.isEmpty()) {
            prompt.append("对话上下文：\n");
            for (String contextItem : context) {
                prompt.append("- " + contextItem + "\n");
            }
            prompt.append("\n");
        }

        prompt.append("用户输入：" + text + "\n\n");
        prompt.append("请按照以下格式输出结果：\n");
        prompt.append("意图：[意图名称]\n");
        prompt.append("置信度：[0-1之间的数值]\n");
        prompt.append("实体：\n");
        prompt.append("- [实体类型]：[实体值]\n");
        prompt.append("\n");
        prompt.append("可能的意图包括：\n");
        prompt.append("- 问候\n");
        prompt.append("- 询问天气\n");
        prompt.append("- 询问时间\n");
        prompt.append("- 询问股票\n");
        prompt.append("- 设置提醒\n");
        prompt.append("- 聊天\n");
        prompt.append("- 其他\n");

        return prompt.toString();
    }

    /**
     * 解析意图识别结果
     * @param result 意图识别结果
     * @return 意图识别结果对象
     */
    private IntentResult parseIntentResult(String result) {
        // 这里应该解析AI模型的输出
        // 由于我们没有实际的AI模型输出，这里使用模拟实现
        IntentResult intentResult = new IntentResult();

        // 模拟不同的意图识别结果
        // 实际实现中，应该解析AI模型输出中的意图和实体
        if (result.contains("天气")) {
            intentResult.setIntent("询问天气");
            intentResult.setConfidence(0.95);
            Map<String, String> entities = new HashMap<>();
            entities.put("location", "北京");
            intentResult.setEntities(entities);
        } else if (result.contains("时间")) {
            intentResult.setIntent("询问时间");
            intentResult.setConfidence(0.93);
        } else if (result.contains("股票")) {
            intentResult.setIntent("询问股票");
            intentResult.setConfidence(0.90);
            Map<String, String> entities = new HashMap<>();
            entities.put("stock", "阿里巴巴");
            intentResult.setEntities(entities);
        } else if (result.contains("提醒")) {
            intentResult.setIntent("设置提醒");
            intentResult.setConfidence(0.92);
            Map<String, String> entities = new HashMap<>();
            entities.put("time", "明天早上8点");
            entities.put("event", "开会");
            intentResult.setEntities(entities);
        } else if (result.contains("你好") || result.contains("Hello")) {
            intentResult.setIntent("问候");
            intentResult.setConfidence(0.98);
        } else {
            intentResult.setIntent("聊天");
            intentResult.setConfidence(0.90);
        }

        return intentResult;
    }
}
