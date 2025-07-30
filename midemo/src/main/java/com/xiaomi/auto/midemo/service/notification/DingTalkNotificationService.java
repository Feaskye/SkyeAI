package com.xiaomi.auto.midemo.service.notification;

import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOHeaders;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOBody;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.xiaomi.auto.midemo.service.VisualizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class DingTalkNotificationService {
    @Value("${钉钉.webhook.url}")
    private String webhookUrl;

    @Value("${钉钉.secret}")
    private String secret;

    private com.aliyun.dingtalkrobot_1_0.Client client;

    public DingTalkNotificationService() throws Exception {
        // 初始化钉钉机器人客户端
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        this.client = new com.aliyun.dingtalkrobot_1_0.Client(config);
    }

    /**
     * 发送文本消息到钉钉
     */
    public boolean sendTextMessage(String content, String... userIds) throws Exception {
        if (!isConfigured()) {
            return false;
        }

        BatchSendOTOBody batchSendOTOBody = new BatchSendOTOBody();
        batchSendOTOBody.setMsgKey("sampleText");
        batchSendOTOBody.setUserIds(java.util.Arrays.asList(userIds));

        Map<String, Object> textContent = new HashMap<>();
        textContent.put("content", content);

        Map<String, Object> msgParam = new HashMap<>();
        msgParam.put("text", textContent);
        batchSendOTOBody.setMsgParam(new com.aliyun.teautil.Common.JsonMapper().writeValueAsString(msgParam));

        BatchSendOTOHeaders batchSendOTOHeaders = new BatchSendOTOHeaders();
        batchSendOTOHeaders.xAcsDingtalkAccessToken = getAccessToken();

        BatchSendOTOResponse response = client.batchSendOTOWithOptions(batchSendOTOBody, batchSendOTOHeaders, new RuntimeOptions());
        return response.getBody().getSuccess();
    }

    /**
     * 发送舆情分析报告到钉钉
     */
    public boolean sendReportMessage(String title, String summary, String keywordCloudPath, String sentimentTimelinePath, String... userIds) throws Exception {
        if (!isConfigured()) {
            return false;
        }

        // 构建富文本消息内容
        StringBuilder content = new StringBuilder();
        content.append("### ").append(title).append("\n");
        content.append("#### 摘要\n");
        content.append(summary).append("\n");
        content.append("#### 关键词云\n");
        content.append("![关键词云](file://").append(keywordCloudPath).append(")\n");
        content.append("#### 情感时间线\n");
        content.append("![情感时间线](file://").append(sentimentTimelinePath).append(")\n");

        return sendTextMessage(content.toString(), userIds);
    }

    /**
     * 检查是否已配置钉钉机器人
     */
    public boolean isConfigured() {
        return webhookUrl != null && !webhookUrl.isEmpty() && secret != null && !secret.isEmpty();
    }

    /**
     * 获取钉钉访问令牌（实际应用需实现完整的token获取逻辑）
     */
    private String getAccessToken() {
        // 此处简化处理，实际需通过钉钉API获取
        return "dummy_token";
    }
}