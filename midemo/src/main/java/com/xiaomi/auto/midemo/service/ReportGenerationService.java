package com.xiaomi.auto.midemo.service;

import com.xiaomi.auto.midemo.dto.NewsArticle;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ReportGenerationService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateMarkdownReport(Map<NewsArticle, String> sentimentResults) {
        StringBuilder markdown = new StringBuilder();

        // 添加报告标题和生成时间
        markdown.append("# 小米汽车舆情分析报告\n\n");
        markdown.append("**生成时间:** ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n\n");

        // 添加情感分析摘要统计
        long positiveCount = sentimentResults.values().stream().filter("正面"::equals).count();
        long negativeCount = sentimentResults.values().stream().filter("负面"::equals).count();
        long neutralCount = sentimentResults.values().stream().filter("中性"::equals).count();

        markdown.append("## 情感分析摘要\n");
        markdown.append("| 情感倾向 | 新闻数量 | 占比 |\n");
        markdown.append("|----------|----------|------|\n");
        markdown.append(String.format("| 正面     | %d        | %.2f%% |\n", positiveCount, calculatePercentage(positiveCount, sentimentResults.size())));
        markdown.append(String.format("| 负面     | %d        | %.2f%% |\n", negativeCount, calculatePercentage(negativeCount, sentimentResults.size())));
        markdown.append(String.format("| 中性     | %d        | %.2f%% |\n\n", neutralCount, calculatePercentage(neutralCount, sentimentResults.size())));

        // 添加详细新闻列表
        markdown.append("## 新闻详情及情感分析\n");
        int index = 1;
        for (Map.Entry<NewsArticle, String> entry : sentimentResults.entrySet()) {
            NewsArticle article = entry.getKey();
            String sentiment = entry.getValue();

            markdown.append(String.format("### %d. %s\n", index++, article.getTitle()));
            markdown.append("**来源:** [链接](