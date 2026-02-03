package com.skyeai.jarvis.llm.service.nlp;

import com.skyeai.jarvis.llm.service.AliyunAIService;
import com.skyeai.jarvis.llm.service.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 自然语言增强服务，用于增强意图识别和上下文理解
 */
@Service
public class NaturalLanguageEnhancementService {

    private final AliyunAIService aliyunAIService;
    private final IntentEnhancer intentEnhancer;
    private final ContextManager contextManager;
    private final PromptEngineer promptEngineer;

    @Autowired
    public NaturalLanguageEnhancementService(AliyunAIService aliyunAIService, LlmService llmService) {
        this.aliyunAIService = aliyunAIService;
        this.intentEnhancer = new IntentEnhancer(aliyunAIService, llmService);
        this.contextManager = new ContextManager();
        this.promptEngineer = new PromptEngineer();
    }

    /**
     * 增强自然语言理解
     * @param text 输入文本
     * @param conversationId 对话ID
     * @return 增强后的理解结果
     * @throws Exception 异常
     */
    public EnhancedLanguageResult enhanceLanguageUnderstanding(String text, String conversationId) throws Exception {
        // 获取对话上下文
        List<String> context = contextManager.getContext(conversationId);

        // 增强意图识别
        IntentResult intentResult = intentEnhancer.enhanceIntentRecognition(text, context);

        // 构建增强后的提示
        String enhancedPrompt = promptEngineer.buildEnhancedPrompt(text, context, intentResult);

        // 使用AI模型生成增强后的理解
        String enhancedUnderstanding = aliyunAIService.generateResponse(enhancedPrompt);

        // 更新对话上下文
        contextManager.updateContext(conversationId, text, enhancedUnderstanding);

        // 构建结果
        EnhancedLanguageResult result = new EnhancedLanguageResult();
        result.setOriginalText(text);
        result.setEnhancedUnderstanding(enhancedUnderstanding);
        result.setIntent(intentResult.getIntent());
        result.setConfidence(intentResult.getConfidence());
        result.setEntities(intentResult.getEntities());

        return result;
    }

    /**
     * 处理多轮对话
     * @param text 输入文本
     * @param conversationId 对话ID
     * @return 对话处理结果
     * @throws Exception 异常
     */
    public ConversationResult processConversation(String text, String conversationId) throws Exception {
        // 增强自然语言理解
        EnhancedLanguageResult enhancedResult = enhanceLanguageUnderstanding(text, conversationId);

        // 构建对话提示
        String conversationPrompt = promptEngineer.buildConversationPrompt(
                text, 
                contextManager.getContext(conversationId), 
                enhancedResult.getIntent(),
                enhancedResult.getEntities()
        );

        // 使用AI模型生成对话响应
        String response = aliyunAIService.generateResponse(conversationPrompt);

        // 更新对话上下文
        contextManager.updateContext(conversationId, text, response);

        // 构建对话结果
        ConversationResult result = new ConversationResult();
        result.setInput(text);
        result.setResponse(response);
        result.setIntent(enhancedResult.getIntent());
        result.setConfidence(enhancedResult.getConfidence());

        return result;
    }

    /**
     * 清除对话上下文
     * @param conversationId 对话ID
     */
    public void clearContext(String conversationId) {
        contextManager.clearContext(conversationId);
    }

    /**
     * 增强自然语言处理（单参数版本）
     * @param text 输入文本
     * @return 增强后的语言处理结果
     * @throws Exception 异常
     */
    public EnhancedLanguageResult enhanceLanguage(String text) throws Exception {
        // 使用默认对话ID
        String defaultConversationId = "default";
        return enhanceLanguageUnderstanding(text, defaultConversationId);
    }

    /**
     * 处理多轮对话（兼容ChatController的版本）
     * @param sessionId 会话ID
     * @param message 输入消息
     * @return 对话处理结果
     * @throws Exception 异常
     */
    public ConversationResult processConversationForController(String sessionId, String message) throws Exception {
        return processConversation(message, sessionId);
    }
}
