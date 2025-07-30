package com.xiaomi.auto.midemo.service;

import com.xiaomi.auto.midemo.dto.NewsArticle;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.xiaomi.auto.midemo.service.McpClientService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SentimentAnalysisService {
    private final McpClientService mcpClientService;

    @Autowired
    public SentimentAnalysisService(McpClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    public Map<NewsArticle, String> analyzeSentiments(List<NewsArticle> articles) {
        Map<NewsArticle, String> sentimentResults = new HashMap<>();

        for (NewsArticle article : articles) {
            String sentiment = mcpClientService.analyzeSentimentWithMcp(article.getContent());
        sentimentResults.put(article, sentiment);
        }

        return sentimentResults;
    }
}