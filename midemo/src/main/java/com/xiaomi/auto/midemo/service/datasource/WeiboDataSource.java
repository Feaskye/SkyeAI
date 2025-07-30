package com.xiaomi.auto.midemo.service.datasource;

import com.github.scribejava.apis.WeiboApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.xiaomi.auto.midemo.dto.SocialMediaPost;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.xiaomi.auto.midemo.service.OcrService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class WeiboDataSource implements SocialMediaDataSource {
    @Value("${weibo.api.key}")
    private String apiKey;

    @Value("${weibo.api.secret}")
    private String apiSecret;

    @Value("${weibo.redirect.uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;
    private final OcrService ocrService;

    @Autowired
    public WeiboDataSource(@Value("${weibo.api.key}") String apiKey,
                           @Value("${weibo.api.secret}") String apiSecret,
                           @Value("${weibo.redirect.uri}") String redirectUri,
                           @Value("${weibo.api.access-token}") String accessToken,
                           OcrService ocrService) {
        this.service = new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .callback(redirectUri)
                .build(WeiboApi20.instance());
        this.accessToken = new OAuth2AccessToken(accessToken);
        this.ocrService = ocrService;
    }

    @Override
    public List<SocialMediaPost> searchPosts(String keyword, int limit) {
        List<SocialMediaPost> posts = new ArrayList<>();
        if (!isAvailable()) {
            return posts;
        }

        try {
            // 创建OAuth服务
            service = new ServiceBuilder(apiKey)
                    .apiSecret(apiSecret)
                    .callback(redirectUri)
                    .build(WeiboApi20.instance());

            // 获取访问令牌（实际应用中应实现完整OAuth流程）
            if (accessToken == null) {
                // 此处简化处理，实际需通过授权码流程获取token
                accessToken = new OAuth2AccessToken("dummy_token");
            }

            // 构建搜索请求
            OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.weibo.com/2/search/statuses.json");
            request.addQuerystringParameter("q", keyword);
            request.addQuerystringParameter("count", String.valueOf(limit));
            request.addQuerystringParameter("sort", "hot");
            service.signRequest(accessToken, request);

            // 执行请求
            Response response = service.execute(request);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode statusesNode = rootNode.get("statuses");

            if (statusesNode != null && statusesNode.isArray()) {
                for (JsonNode node : statusesNode) {
                    SocialMediaPost post = new SocialMediaPost();
                    post.setPlatform(getPlatformName());
                    post.setId(node.get("idstr").asText());
                    post.setContent(node.get("text").asText());
                    post.setAuthor(node.get("user").get("screen_name").asText());
                    post.setPublishTime(node.get("created_at").asText());
                    post.setLikeCount(node.get("attitudes_count").asInt());
                    post.setCommentCount(node.get("comments_count").asInt());
                    post.setShareCount(node.get("reposts_count").asInt());
                    // 提取图片URL进行文字识别
                    if (node.has("pic_urls") && !node.get("pic_urls").isEmpty()) {
                        String imageUrl = node.get("pic_urls").get(0).get("thumbnail_pic").asText();
                        String imageText = ocrService.recognizeText(imageUrl);
                        post.setVideoText(imageText);
                    }
                    posts.add(post);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return posts;
    }

    @Override
    public String getPlatformName() {
        return "微博";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty() && apiSecret != null && !apiSecret.isEmpty();
    }
}