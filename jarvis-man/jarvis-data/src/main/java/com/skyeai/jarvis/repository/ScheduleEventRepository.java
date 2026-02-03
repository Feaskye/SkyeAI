package com.skyeai.jarvis.repository;

import com.skyeai.jarvis.model.ScheduleEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 日程事件Repository
 */
@Repository
public interface ScheduleEventRepository extends JpaRepository<ScheduleEvent, Long> {

    /**
     * 获取所有活跃的重复事件
     * @return 活跃的重复事件列表
     */
    List<ScheduleEvent> findByActiveTrueAndRepeatTypeIsNotNullAndRepeatTypeNot(String repeatType);
}