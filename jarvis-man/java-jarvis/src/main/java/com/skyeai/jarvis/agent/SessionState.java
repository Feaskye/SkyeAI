package com.skyeai.jarvis.agent;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话状态 POJO（非 Bean）
 * v10 新增：从 ChatMemory 拆分出的实例字段载体
 * 每个 conversationId 对应一个 SessionState 实例
 */
public class SessionState {

    /** 摘要文本 */
    private String summaryText = "";

    /** 消息历史列表 */
    private List<Message> history = new ArrayList<>();

    /** 是否启用压缩 */
    private boolean compressionEnabled = true;

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public List<Message> getHistory() {
        return history;
    }

    public void setHistory(List<Message> history) {
        this.history = history;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }
}
