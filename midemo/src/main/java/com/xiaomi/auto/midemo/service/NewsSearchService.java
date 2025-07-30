package com.xiaomi.auto.midemo.service;

import com.xiaomi.auto.midemo.dto.NewsArticle;
import com.xiaomi.auto.midemo.dto.SocialMediaPost;
import com.xiaomi.auto.midemo.service.datasource.DataSourceAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsSearchService {
    private final DataSourceAggregator dataSourceAggregator;

    @Autowired
    public NewsSearchService(DataSourceAggregator dataSourceAggregator) {
        this.dataSourceAggregator = dataSourceAggregator;
    }
    @Value("${news.api.endpoint}")
    private String newsApiEndpoint;

    @Value("${news.api.key}")
    private String apiKey;

    @Value("${news.search.keyword}")
    private String searchKeyword;

    private final HttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 搜索所有平台的相关信息（新闻+社交媒体）
     */
    public List<NewsArticle> searchXiaomiAutoNews() throws Exception {
        // 从所有可用平台搜索信息（每个平台最多返回20条）
        List<SocialMediaPost> socialPosts = dataSourceAggregator.searchAllPlatforms(searchKeyword, 20);

        // 转换为NewsArticle格式返回
        return socialPosts.stream()
                .map(this::convertToNewsArticle)
                .collect(Collectors.toList());
    }

    /**
     * 将SocialMediaPost转换为NewsArticle格式
     */
    private NewsArticle convertToNewsArticle(SocialMediaPost post) {
        NewsArticle article = new NewsArticle();
        article.setTitle(post.getTitle() != null ? post.getTitle() : post.getContent().substring(0, Math.min(post.getContent().length(), 50)));
        article.setContent(post.getContent());
        article.setUrl(post.getUrl());
        article.setPublishedAt(post.getPublishTime());
        article.setSource(post.getPlatform() + " - " + post.getAuthor());
        return article;
    }
}