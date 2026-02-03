package com.skyeai.jarvis.user.repository;

import com.skyeai.jarvis.user.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByUserId(Long userId);

    List<ChatHistory> findBySessionId(String sessionId);

    List<ChatHistory> findByUserIdAndSessionId(Long userId, String sessionId);

    @Query("SELECT ch FROM ChatHistory ch WHERE ch.userId = ?1 ORDER BY ch.timestamp DESC LIMIT ?2")
    List<ChatHistory> findRecentByUserId(Long userId, int limit);

    @Query("SELECT ch FROM ChatHistory ch WHERE ch.userId = ?1 AND ch.timestamp BETWEEN ?2 AND ?3 ORDER BY ch.timestamp DESC")
    List<ChatHistory> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT ch FROM ChatHistory ch WHERE ch.userId = ?1 AND ch.modelUsed = ?2 ORDER BY ch.timestamp DESC")
    List<ChatHistory> findByUserIdAndModelUsed(Long userId, String modelUsed);

    @Query("SELECT ch FROM ChatHistory ch WHERE ch.userId = ?1 AND ch.promptType = ?2 ORDER BY ch.timestamp DESC")
    List<ChatHistory> findByUserIdAndPromptType(Long userId, String promptType);

    @Query("SELECT COUNT(ch) FROM ChatHistory ch WHERE ch.userId = ?1")
    long countByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteBySessionId(String sessionId);
}
