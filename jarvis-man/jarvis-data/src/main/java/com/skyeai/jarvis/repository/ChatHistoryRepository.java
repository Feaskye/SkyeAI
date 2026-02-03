package com.skyeai.jarvis.repository;

import com.skyeai.jarvis.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天历史Repository
 */
@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    /**
     * 获取指定用户的最近聊天历史
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 聊天历史列表
     */
    List<ChatHistory> findTop50ByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 获取指定会话的聊天历史
     * @param sessionId 会话ID
     * @return 聊天历史列表
     */
    List<ChatHistory> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    /**
     * 删除指定时间之前的聊天历史
     * @param cutoffDate 截止日期
     * @return 删除的记录数
     */
    int deleteByCreatedAtBefore(java.time.LocalDateTime cutoffDate);

    /**
     * 删除指定用户的所有聊天历史
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);

    /**
     * 获取指定用户在指定时间之后的聊天历史
     * @param userId 用户ID
     * @param cutoffTime 截止时间
     * @return 聊天历史列表
     */
    List<ChatHistory> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(String userId, java.time.LocalDateTime cutoffTime);
}