package com.xiaomi.auto.midemo.service.notification;

import com.tencent.wework.Finance;import com.tencent.wework.service.externalcontact.ExternalContactService;
import com.tencent.wework.service.message.MessageService;
import com.xiaomi.auto.midemo.dto.SocialMediaPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeWorkNotificationService {
    @Value("${wework.corpid}")
    private String corpId;

    @Value("${wework.agentid}")
    private int agentId;

    @Value("${wework.secret}")
    private String secret;

    private MessageService messageService;

    public WeWorkNotificationService() {
        // 初始化企业微信服务
        com.tencent.wework.Config config = new com.tencent.wework.Config();
        config.setCorpId(corpId);
        config.setAgentSecret(secret);
        this.messageService = new MessageService(config);
    }

    /**
     * 发送文本消息给企业微信用户
     * @param userId 接收用户ID
     * @param content 消息内容
     * @return 是否发送成功
     */
    public boolean sendTextMessage(String userId, String content) {
        if (!isConfigured()) {
            return false;
        }

        try {
            // 构建文本消息
            com.tencent.wework.model.message.TextMessage message = new com.tencent.wework.model.message.TextMessage();
            message.setTouser(userId);
            message.setAgentid(agentId);
            message.setText(new com.tencent.wework.model.message.TextMessage.Text(content));
            message.setSafe(0);

            // 发送消息
            String result = messageService.sendMessage(message);
            return result.contains("\"errcode\":0");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送图文消息给企业微信用户
     * @param userId 接收用户ID
     * @param title 标题
     * @param description 描述
     * @param url 跳转链接
     * @param picUrl 图片链接
     * @return 是否发送成功
     */
    public boolean sendNewsMessage(String userId, String title, String description, String url, String picUrl) {
        if (!isConfigured()) {
            return false;
        }

        try {
            // 构建图文消息
            com.tencent.wework.model.message.NewsMessage message = new com.tencent.wework.model.message.NewsMessage();
            message.setTouser(userId);
            message.setAgentid(agentId);

            com.tencent.wework.model.message.NewsMessage.News news = new com.tencent.wework.model.message.NewsMessage.News();
            news.setTitle(title);
            news.setDescription(description);
            news.setUrl(url);
            news.setPicurl(picUrl);
            message.setNews(new com.tencent.wework.model.message.NewsMessage.News[]{news});

            // 发送消息
            String result = messageService.sendMessage(message);
            return result.contains("\"errcode\":0");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送舆情分析报告到企业微信
     */
    public boolean sendReport(String userId, String title, String reportUrl, String keywordCloudUrl, String sentimentTimelineUrl) {
        String content = String.format("%s\n\n报告链接: %s\n关键词云: %s\n情感时间线: %s",
                title, reportUrl, keywordCloudUrl, sentimentTimelineUrl);
        return sendTextMessage(userId, content);
    }

    /**
     * 检查是否已配置企业微信
     */
    public boolean isConfigured() {
        return corpId != null && !corpId.isEmpty() && secret != null && !secret.isEmpty() && agentId > 0;
    }
}