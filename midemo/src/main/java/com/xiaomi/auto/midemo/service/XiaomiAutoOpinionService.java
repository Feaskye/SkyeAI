package com.xiaomi.auto.midemo.service;

import com.xiaomi.auto.midemo.dto.NewsArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class XiaomiAutoOpinionService {
    private final NewsSearchService newsSearchService;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final ReportGenerationService reportGenerationService;
    private final EmailService emailService;

    @Autowired
    public XiaomiAutoOpinionService(NewsSearchService newsSearchService,
                                   SentimentAnalysisService sentimentAnalysisService,
                                   ReportGenerationService reportGenerationService,
                                   EmailService emailService) {
        this.newsSearchService = newsSearchService;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.reportGenerationService = reportGenerationService;
        this.emailService = emailService;
    }

    public void executeOpinionAnalysis(String recipientEmail) throws Exception {
        // 1. 搜索小米汽车相关新闻
        List<NewsArticle> newsArticles = newsSearchService.searchXiaomiAutoNews();
        if (newsArticles.isEmpty()) {
            throw new RuntimeException("未找到相关新闻，无法生成舆情分析报告");
        }

        // 2. 分析新闻情感倾向
        Map<NewsArticle, String> sentimentResults = sentimentAnalysisService.analyzeSentiments(newsArticles);

        // 3. 生成Markdown报告
        String markdownReport = reportGenerationService.generateMarkdownReport(sentimentResults);

        // 4. 发送邮件报告
        emailService.sendReportEmail(recipientEmail, "小米汽车舆情分析报告", markdownReport);
    }
}