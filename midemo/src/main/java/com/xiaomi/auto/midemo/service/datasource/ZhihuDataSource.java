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
public class ZhihuDataSource implements SocialMediaDataSource {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClients.createDefault();
    private final OcrService ocrService;
    private final String accessToken;

    @Autowired
    public ZhihuDataSource(@Value("${zhihu.api.access-token}") String accessToken, OcrService ocrService) {
        this.accessToken = accessToken;
        this.ocrService = ocrService;
    }

    @Override
    public List<SocialMediaPost> searchPosts(String keyword, int limit) {
        List<SocialMediaPost> posts = new ArrayList<>();
        if (!isAvailable()) {
            return posts;
        }

        try {
            // 构建知乎搜索API请求（实际应用需遵循知乎API规范）
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String url = String.format("https://www.zhihu.com/api/v4/search_v3?t=general&q=%s&limit=%d", encodedKeyword, limit);

            HttpGet request = new HttpGet(url);
            request.addHeader("User-Agent", "Mozilla/5.0");
            request.addHeader("Authorization", "oauth " + getAccessToken());

            // 执行请求
            httpClient.execute(request, response -> {
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    JsonNode dataNode = rootNode.get("data");

                    if (dataNode != null && dataNode.isArray()) {
                        for (JsonNode node : dataNode) {
                            // 根据知乎API响应结构解析数据
                            String type = node.get("type").asText();
                            if ("answer".equals(type) || "article".equals(type)) {
                                SocialMediaPost post = new SocialMediaPost();
                                post.setPlatform(getPlatformName());
                                post.setId(node.get("object").get("id").asText());
                                post.setTitle(node.get("object").get("title").asText());
                                post.setContent(node.get("object").get("excerpt").asText());
                                post.setAuthor(node.get("object").get("author").get("name").asText());
                                post.setPublishTime(node.get("object").get("created_time").asText());
                                post.setLikeCount(node.get("object").get("voteup_count").asInt());
                                post.setCommentCount(node.get("object").get("comment_count").asInt());
                                // 提取文章图片URL进行文字识别
                                if (node.get("object").has("thumbnail")) {
                                    String imageUrl = node.get("object").get("thumbnail").asText();
                                    String imageText = ocrService.recognizeText(imageUrl);
                                    post.setVideoText(imageText);
                                }
                                posts.add(post);
                            }
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
        return "知乎";
    }

    @Override
    public boolean isAvailable() {
        return clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty();
    }

    /**
     * 获取知乎API访问令牌（实际应用需实现完整OAuth流程）
     */
    private String getAccessToken() {
        // 此处简化处理，实际需通过知乎OAuth流程获取
        return "dummy_token";
    }
}