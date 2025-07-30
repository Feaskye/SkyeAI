package com.xiaomi.auto.midemo.agent;

import dev.langchain4j.agent.Agent;import dev.langchain4j.agent.Tool;import dev.langchain4j.memory.chat.MessageWindowChatMemory;import dev.langchain4j.model.openai.OpenAiChatModel;import dev.langchain4j.spring.agent.AgentBean;import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Component;import com.xiaomi.auto.midemo.service.NewsSearchService;import com.xiaomi.auto.midemo.service.McpClientService;import com.xiaomi.auto.midemo.service.ReportGenerationService;import com.xiaomi.auto.midemo.service.EmailService;import com.xiaomi.auto.midemo.service.VisualizationService;import com.xiaomi.auto.midemo.service.notification.DingTalkNotificationService;import com.xiaomi.auto.midemo.service.notification.WeWorkNotificationService;import com.xiaomi.auto.midemo.dto.SocialMediaPost;

@Component@AgentBeanpublic class OpinionAnalysisAgent {
    private final NewsSearchService newsSearchService;    private final McpClientService mcpClientService;    private final ReportGenerationService reportGenerationService;    private final EmailService emailService;    private final VisualizationService visualizationService;    private final DingTalkNotificationService dingTalkNotificationService;    private final WeWorkNotificationService weWorkNotificationService;

    @Value("${openai.api.key}")    private String openAiApiKey;

    public OpinionAnalysisAgent(NewsSearchService newsSearchService,                                McpClientService mcpClientService,                                ReportGenerationService reportGenerationService,                                EmailService emailService,                                VisualizationService visualizationService,                                DingTalkNotificationService dingTalkNotificationService,                                WeWorkNotificationService weWorkNotificationService) {
        this.newsSearchService = newsSearchService;        this.mcpClientService = mcpClientService;        this.reportGenerationService = reportGenerationService;        this.emailService = emailService;        this.visualizationService = visualizationService;        this.dingTalkNotificationService = dingTalkNotificationService;        this.weWorkNotificationService = weWorkNotificationService;    }

    @Agent    public String analyzeOpinion(String userInput, String recipientEmail) {        String fullPrompt = String.format("%s\n请执行以下步骤:\n1. 搜索相关新闻\n2. 分析文本情感倾向\n3. 生成舆情可视化报告\n4. 通过邮箱发送报告至: %s\n5. 通过钉钉和企业微信推送报告摘要", userInput, recipientEmail);        return Agent.builder()                .chatLanguageModel(OpenAiChatModel.withApiKey(openAiApiKey))                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))                .tools(searchNewsTool(), analyzeSentimentTool(), generateVisualizationTool(), sendDingTalkReportTool(), sendWeWorkReportTool(), sendReportTool())                .build()                .execute(fullPrompt);    }

    @Tool("搜索指定关键词的新闻，返回相关新闻标题列表")
    private String searchNewsTool(String keyword) {
        try {            // 动态设置搜索关键词            System.setProperty("news.search.keyword", keyword);            var news = newsSearchService.searchXiaomiAutoNews();            return "找到" + news.size() + "条相关新闻: " + news.stream()                    .map(article -> article.getTitle())                    .limit(5)                    .reduce((a, b) -> a + ", " + b)                    .orElse("无相关新闻");        } catch (Exception e) {            return "新闻搜索失败: " + e.getMessage();        }    }

    @Tool("分析文本情感倾向")    private String analyzeSentimentTool(String text) {
        return mcpClientService.analyzeSentimentWithMcp(text);    }

    @Tool("生成舆情可视化报告，包括关键词云和情感时间线")
    private String generateVisualizationTool(String keyword) {
        try {
            // 获取多平台数据
            var news = newsSearchService.searchXiaomiAutoNews();
            List<SocialMediaPost> posts = news.stream()
                    .map(article -> {
                        SocialMediaPost post = new SocialMediaPost();
                        post.setContent(article.getContent());
                        post.setPublishTime(article.getPublishedAt());
                        post.setSentiment(mcpClientService.analyzeSentimentWithMcp(article.getContent()));
                        return post;
                    }).collect(java.util.stream.Collectors.toList());

            // 生成可视化图表
            String keywordCloudPath = visualizationService.generateKeywordCloud(posts, keyword + "关键词云");
            String sentimentTimelinePath = visualizationService.generateSentimentTimeline(posts);

            return String.format("可视化报告生成成功:\n关键词云: %s\n情感时间线: %s", keywordCloudPath, sentimentTimelinePath);
        } catch (Exception e) {
            return "可视化报告生成失败: " + e.getMessage();
        }
    }

    @Tool("通过钉钉发送舆情分析报告")
    private String sendDingTalkReportTool(String recipientId, String reportSummary) {
        try {
            if (dingTalkNotificationService.isConfigured()) {
                boolean success = dingTalkNotificationService.sendTextMessage(reportSummary, recipientId);
                return success ? "钉钉报告发送成功" : "钉钉报告发送失败";
            } else {
                return "钉钉未配置，无法发送报告";
            }
        } catch (Exception e) {
            return "钉钉报告发送异常: " + e.getMessage();
        }
    }

    @Tool("通过企业微信发送舆情分析报告")
    private String sendWeWorkReportTool(String userId, String reportUrl) {
        try {
            if (weWorkNotificationService.isConfigured()) {
                boolean success = weWorkNotificationService.sendNewsMessage(userId, "舆情分析报告", "点击查看详细报告", reportUrl, "");
                return success ? "企业微信报告发送成功" : "企业微信报告发送失败";
            } else {
                return "企业微信未配置，无法发送报告";
            }
        } catch (Exception e) {
            return "企业微信报告发送异常: " + e.getMessage();
        }
    }

    @Tool("生成并发送舆情分析报告到指定邮箱")
    private String sendReportTool(String recipientEmail) {
        try {            var news = newsSearchService.searchXiaomiAutoNews();            var sentimentResults = news.stream()                    .collect(java.util.stream.Collectors.toMap(                            article -> article,                            article -> mcpClientService.analyzeSentimentWithMcp(article.getContent())                    ));            String report = reportGenerationService.generateMarkdownReport(sentimentResults);            emailService.sendReportEmail(recipientEmail, "小米汽车舆情分析报告", report);
            return "报告已成功发送至: " + recipientEmail;        } catch (Exception e) {            return "报告生成或发送失败: " + e.getMessage();        }    }
}