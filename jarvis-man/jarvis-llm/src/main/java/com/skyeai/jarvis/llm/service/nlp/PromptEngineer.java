package com.skyeai.jarvis.llm.service.nlp;

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
        prompt.append("你是一个智能助手，需要根据用户输入、对话上下文和意图识别结果，生成一个增强的理解。\n\n");

        if (!context.isEmpty()) {
            prompt.append("对话上下文：\n");
            for (String contextItem : context) {
                prompt.append("- " + contextItem + "\n");
            }
            prompt.append("\n");
        }

        prompt.append("用户输入：" + text + "\n\n");

        if (intentResult != null) {
            prompt.append("意图识别结果：\n");
            prompt.append("- 意图：" + intentResult.getIntent() + "\n");
            prompt.append("- 置信度：" + intentResult.getConfidence() + "\n");
            if (intentResult.getEntities() != null && !intentResult.getEntities().isEmpty()) {
                prompt.append("- 实体：\n");
                for (Map.Entry<String, String> entity : intentResult.getEntities().entrySet()) {
                    prompt.append("  * " + entity.getKey() + "：" + entity.getValue() + "\n");
                }
            }
            prompt.append("\n");
        }

        prompt.append("请生成一个增强的理解，包括：\n");
        prompt.append("1. 对用户输入的准确理解\n");
        prompt.append("2. 对对话上下文的综合考虑\n");
        prompt.append("3. 基于意图识别结果的针对性处理\n");
        prompt.append("4. 对可能的实体和关键信息的提取\n");
        prompt.append("\n增强理解：");

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
        prompt.append("你是一个智能助手，需要根据用户输入、对话上下文、意图和实体信息，生成一个自然、准确的响应。\n\n");

        if (!context.isEmpty()) {
            prompt.append("对话上下文：\n");
            for (String contextItem : context) {
                prompt.append("- " + contextItem + "\n");
            }
            prompt.append("\n");
        }

        prompt.append("用户输入：" + text + "\n\n");

        if (intent != null) {
            prompt.append("意图：" + intent + "\n");
        }

        if (entities != null && !entities.isEmpty()) {
            prompt.append("实体：\n");
            for (Map.Entry<String, String> entity : entities.entrySet()) {
                prompt.append("- " + entity.getKey() + "：" + entity.getValue() + "\n");
            }
            prompt.append("\n");
        }

        prompt.append("请生成一个响应，要求：\n");
        prompt.append("1. 自然、流畅，符合对话上下文\n");
        prompt.append("2. 针对识别的意图提供有针对性的回应\n");
        prompt.append("3. 考虑提取的实体信息\n");
        prompt.append("4. 避免重复用户输入\n");
        prompt.append("5. 使用友好、专业的语言\n");
        prompt.append("\n响应：");

        return prompt.toString();
    }

    /**
     * 构建意图识别提示
     * @param text 输入文本
     * @param context 对话上下文
     * @return 意图识别提示
     */
    public String buildIntentRecognitionPrompt(String text, List<String> context) {
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

        return prompt.toString();
    }
}
