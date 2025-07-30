package com.xiaomi.auto.midemo.dto;

import java.time.LocalDateTime;

/**
 * 社交媒体帖子数据传输对象，统一多平台帖子数据格式
 */
public class SocialMediaPost {
    private String platform;       // 平台名称（微博、知乎、抖音等）
    private String id;             // 帖子ID
    private String title;          // 标题（可为空）
    private String content;        // 内容
    private String author;         // 作者
    private String publishTime;    // 发布时间字符串
    private LocalDateTime parsedPublishTime; // 解析后的发布时间
    private int likeCount;         // 点赞数
    private int commentCount;      // 评论数
    private int shareCount;        // 分享数
    private String url;            // 帖子链接
    private String sentiment;      // 情感分析结果
    private String videoText;      // 视频文字识别结果

    // Getters and Setters
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
    public LocalDateTime getParsedPublishTime() { return parsedPublishTime; }
    public void setParsedPublishTime(LocalDateTime parsedPublishTime) { this.parsedPublishTime = parsedPublishTime; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public int getShareCount() { return shareCount; }
    public void setShareCount(int shareCount) { this.shareCount = shareCount; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public String getVideoText() { return videoText; }
    public void setVideoText(String videoText) { this.videoText = videoText; }
}