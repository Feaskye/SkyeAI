package com.xiaomi.auto.midemo.service.datasource;

import com.xiaomi.auto.midemo.dto.SocialMediaPost;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.xiaomi.auto.midemo.service.OcrService;

import java.util.ArrayList;
import java.util.List;

@Service
public class DouyinDataSource implements SocialMediaDataSource {
    @Value("${douyin.api.access-token}")
    private String accessToken;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClients.createDefault();
    private final OcrService ocrService;

    @Autowired
    public DouyinDataSource(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public List<SocialMediaPost> searchPosts(String keyword, int limit) {
        List<SocialMediaPost> posts = new ArrayList<>();
        if (!isAvailable()) {
            return posts;
        }

        try {
            // 构建抖音搜索API请求
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String url = String.format("https://open.douyin.com/oauth/aweme/v1/search/item/?keyword=%s&count=%d&access_token=%s",
                    encodedKeyword, limit, accessToken);

            HttpGet request = new HttpGet(url);
            request.addHeader("Accept", "application/json");

            // 执行请求
            httpClient.execute(request, response -> {
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    JsonNode itemsNode = rootNode.get("data").get("items");

                    if (itemsNode != null && itemsNode.isArray()) {
                        for (JsonNode node : itemsNode) {
                            SocialMediaPost post = new SocialMediaPost();
                            post.setPlatform(getPlatformName());
                            post.setId(node.get("aweme_id").asText());
                            post.setTitle(node.get("desc").asText());
                            post.setContent(node.get("desc").asText());
                            post.setAuthor(node.get("author").get("nickname").asText());
                            post.setPublishTime(node.get("create_time").asText());
                            post.setLikeCount(node.get("statistics").get("digg_count").asInt());
                            post.setCommentCount(node.get("statistics").get("comment_count").asInt());
                            post.setShareCount(node.get("statistics").get("share_count").asInt());
                            // 提取视频封面图URL进行文字识别
                            if (node.has("video") && node.get("video").has("cover")) {
                                String coverUrl = node.get("video").get("cover").get("url_list").get(0).asText();
                                String videoText = ocrService.recognizeText(coverUrl);
                                post.setVideoText(videoText);
                            }
                            posts.add(post);
                        }
                    }
                    return posts;
                } finally {
                    response.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return posts;
    }

    @Override
    public String getPlatformName() {
        return "抖音";
    }

    @Override
    public boolean isAvailable() {
        return accessToken != null && !accessToken.isEmpty();
    }
}