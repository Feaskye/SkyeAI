package com.skyeai.jarvis.service;

import com.skyeai.jarvis.model.ScheduleEvent;
import com.skyeai.jarvis.repository.ScheduleEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程事件服务
 */
@Service
public class ScheduleEventService {

    private final ScheduleEventRepository scheduleEventRepository;

    @Autowired
    public ScheduleEventService(ScheduleEventRepository scheduleEventRepository) {
        this.scheduleEventRepository = scheduleEventRepository;
    }

    /**
     * 保存日程事件
     * @param scheduleEvent 日程事件对象
     * @return 保存后的日程事件
     */
    public ScheduleEvent saveScheduleEvent(ScheduleEvent scheduleEvent) {
        if (scheduleEvent.getCreatedAt() == null) {
            scheduleEvent.setCreatedAt(LocalDateTime.now());
        }
        return scheduleEventRepository.save(scheduleEvent);
    }

    /**
     * 获取所有日程事件
     * @return 日程事件列表
     */
    public List<ScheduleEvent> getAllScheduleEvents() {
        return scheduleEventRepository.findAll();
    }

    /**
     * 根据ID获取日程事件
     * @param id 日程事件ID
     * @return 日程事件对象
     */
    public ScheduleEvent getScheduleEventById(Long id) {
        return scheduleEventRepository.findById(id).orElse(null);
    }

    /**
     * 更新日程事件
     * @param scheduleEvent 日程事件对象
     * @return 更新后的日程事件
     */
    public ScheduleEvent updateScheduleEvent(ScheduleEvent scheduleEvent) {
        return scheduleEventRepository.save(scheduleEvent);
    }

    /**
     * 删除日程事件
     * @param id 日程事件ID
     * @return 是否删除成功
     */
    public boolean deleteScheduleEvent(Long id) {
        if (scheduleEventRepository.existsById(id)) {
            scheduleEventRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 获取所有活跃的日程事件
     * @return 活跃的日程事件列表
     */
    public List<ScheduleEvent> getAllActiveScheduleEvents() {
        return scheduleEventRepository.findByActiveTrueAndRepeatTypeIsNotNullAndRepeatTypeNot("");
    }
}
