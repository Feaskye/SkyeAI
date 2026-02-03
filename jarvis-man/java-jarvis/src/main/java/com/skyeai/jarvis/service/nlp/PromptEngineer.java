package com.skyeai.jarvis.service.nlp;

import java.util.List;
import java.util.Map;

/**
 * 提示工程师，用于构建增强的提示
 */
public class PromptEngineer {

    /**
     * 构建增强的提示
     * @param text 输入文本
     * @param context 对话上下文
     * @param intentResult 意图识别结果
     * @return 增强的提示
     */
    public String buildEnhancedPrompt(String text, List<String> context, IntentResult intentResult) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是贾维斯（J.A.R.V.I.S.），一个智能AI助手。请基于以下对话上下文和用户输入，生成一个详细的理解：\n\n");
        
        if (!context.isEmpty()) {
            prompt.append("对话上下文：\n");
            for (String contextItem : context) {
                prompt.append("- " + contextItem + "\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("用户输入：" + text + "\n\n");
        prompt.append("识别到的意图：" + intentResult.getIntent() + "\n");
        prompt.append("意图置信度：" + intentResult.getConfidence() + "\n");
        
        if (!intentResult.getEntities().isEmpty()) {
            prompt.append("提取到的实体：\n");
            for (Map.Entry<String, String> entity : intentResult.getEntities().entrySet()) {
                prompt.append("- " + entity.getKey() + "：" + entity.getValue() + "\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("请生成一个详细的理解，包括：\n");
        prompt.append("1. 用户的真实意图和潜在需求\n");
        prompt.append("2. 与对话上下文的关联和连贯性\n");
        prompt.append("3. 需要进一步澄清的信息（如果有）\n");
        prompt.append("4. 可能的下一步响应和行动建议\n");
        prompt.append("5. 基于用户历史行为的个性化考量\n");

        return prompt.toString();
    }

    /**
     * 构建对话提示
     * @param text 输入文本
     * @param context 对话上下文
     * @param intent 意图
     * @param entities 实体
     * @return 对话提示
     */
    public String buildConversationPrompt(String text, List<String> context, String intent, Map<String, String> entities) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下对话上下文和用户输入，生成一个自然、友好的响应：\n\n");
        
        if (!context.isEmpty()) {
            prompt.append("对话上下文：\n");
            for (String contextItem : context) {
                prompt.append("- " + contextItem + "\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("用户输入：" + text + "\n\n");
        prompt.append("识别到的意图：" + intent + "\n");
        
        if (!entities.isEmpty()) {
            prompt.append("提取到的实体：\n");
            for (Map.Entry<String, String> entity : entities.entrySet()) {
                prompt.append("- " + entity.getKey() + "：" + entity.getValue() + "\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("请生成一个响应，要求：\n");
        prompt.append("1. 自然、友好，符合上下文\n");
        prompt.append("2. 直接回答用户问题，不要有任何引言或开场白\n");
        prompt.append("3. 基于识别到的意图和实体进行回应\n");
        prompt.append("4. 如果需要进一步信息，请明确询问\n");
        prompt.append("5. 保持响应简洁明了\n");

        return prompt.toString();
    }
}
