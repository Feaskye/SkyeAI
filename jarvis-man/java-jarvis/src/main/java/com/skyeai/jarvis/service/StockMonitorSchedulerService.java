package com.skyeai.jarvis.service;

import com.skyeai.jarvis.model.PriceChangeInfo;
import com.skyeai.jarvis.model.ScheduleEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 股票监测定时任务调度服务
 */
@Service
public class StockMonitorSchedulerService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ServiceClient serviceClient;
    
    @Autowired
    private DynamicTaskScheduler dynamicTaskScheduler;

    /**
     * 服务启动时初始化任务
     */
    @PostConstruct
    public void init() {
        // 初始化所有活跃的重复事件任务
        reloadTasks();
    }
    
    /**
     * 重新加载所有任务
     */
    public void reloadTasks() {
        System.out.println("重新加载股票监测任务: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
        
        // 获取所有活跃的重复事件
        List<ScheduleEvent> events = scheduleService.getActiveRepeatEvents();
        
        for (ScheduleEvent event : events) {
            // 根据任务类型添加对应的定时任务
            addTask(event);
        }
    }
    
    /**
     * 添加单个任务
     * @param event 日程事件
     */
    public void addTask(ScheduleEvent event) {
        if (event == null || !event.isActive() || event.getStockSymbols() == null || event.getStockSymbols().isEmpty()) {
            return;
        }
        
        // 根据重复类型设置执行间隔
        long interval = getIntervalByRepeatType(event.getRepeatType());
        
        // 创建任务
        Runnable task = () -> {
            System.out.println("执行股票监测任务: " + LocalDateTime.now().format(DATE_TIME_FORMATTER) + 
                    " (任务ID: " + event.getId() + ", 标题: " + event.getTitle() + ")");
            checkStockPriceChanges(event);
        };
        
        // 调度任务
        dynamicTaskScheduler.schedulePeriodicTask(event.getId(), task, interval);
        System.out.println("已添加股票监测任务: " + event.getTitle() + 
                " (ID: " + event.getId() + ", 间隔: " + interval + "秒)");
    }
    
    /**
     * 根据重复类型获取执行间隔
     * @param repeatType 重复类型
     * @return 执行间隔（秒）
     */
    private long getIntervalByRepeatType(String repeatType) {
        switch (repeatType) {
            case "daily":
                return 24 * 60 * 60; // 每天一次
            case "weekly":
                return 7 * 24 * 60 * 60; // 每周一次
            case "monthly":
                return 30 * 24 * 60 * 60; // 每月一次
            case "hourly":
                return 60 * 60; // 每小时一次
            case "minutely":
                return 60; // 每分钟一次
            default:
                return 5; // 默认每5秒一次
        }
    }
    
    /**
     * 移除任务
     * @param taskId 任务ID
     */
    public void removeTask(Long taskId) {
        dynamicTaskScheduler.cancelTask(taskId);
        System.out.println("已移除股票监测任务: " + taskId);
    }

    /**
     * 检查股票价格变动
     * @param event 日程事件
     */
    private void checkStockPriceChanges(ScheduleEvent event) {
        String[] stockSymbols = event.getStockSymbols().split(",");
        double threshold = event.getPriceChangeThreshold();

        for (String stockSymbol : stockSymbols) {
            stockSymbol = stockSymbol.trim();
            if (stockSymbol.isEmpty()) {
                continue;
            }

            // 检测价格变动
            Map<String, Object> request = Map.of(
                "stockSymbol", stockSymbol,
                "threshold", threshold
            );
            Map<String, Object> response = serviceClient.callFinanceService("/stock/detect-change", request);

            // 转换响应为PriceChangeInfo
            PriceChangeInfo changeInfo = new PriceChangeInfo();
            changeInfo.setStockSymbol((String) response.get("stockSymbol"));
            changeInfo.setCurrentPrice(((Number) response.get("currentPrice")).doubleValue());
            changeInfo.setPreviousPrice(((Number) response.get("previousPrice")).doubleValue());
            changeInfo.setChangePercent(((Number) response.get("changePercent")).doubleValue());
            changeInfo.setChangeDirection((String) response.get("changeDirection"));
            changeInfo.setSignificantChange((Boolean) response.get("significantChange"));
            changeInfo.setRecommendation((String) response.get("recommendation"));

            if (changeInfo.isSignificantChange()) {
                // 发送邮件提醒（实际项目中应该调用邮件服务）
                sendStockAlertEmail(event, changeInfo);

                System.out.println("检测到股票价格异常变动:");
                System.out.println(changeInfo);
            }
        }

        // 更新最后检查时间
        event.setLastCheckTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }

    /**
     * 生成买卖建议
     * @param changeInfo 价格变动信息
     */
    private void generateRecommendation(PriceChangeInfo changeInfo) {
        if (changeInfo.getChangeDirection().equals("急拉")) {
            // 急拉超过阈值，建议考虑卖出
            changeInfo.setRecommendation("建议：股票" + changeInfo.getStockSymbol() + "出现急拉" + 
                    String.format("%.2f", Math.abs(changeInfo.getChangePercent())) + "%，" +
                    "建议考虑卖出以锁定利润。");
        } else if (changeInfo.getChangeDirection().equals("急跌")) {
            // 急跌超过阈值，建议考虑买入
            changeInfo.setRecommendation("建议：股票" + changeInfo.getStockSymbol() + "出现急跌" + 
                    String.format("%.2f", Math.abs(changeInfo.getChangePercent())) + "%，" +
                    "建议考虑买入以逢低吸纳。");
        }
    }

    /**
     * 发送股票预警邮件
     * @param event 日程事件
     * @param changeInfo 价格变动信息
     */
    private void sendStockAlertEmail(ScheduleEvent event, PriceChangeInfo changeInfo) {
        // 实际项目中应该调用邮件服务发送邮件
        System.out.println("发送股票预警邮件：");
        System.out.println("收件人：用户");
        System.out.println("主题：【股票预警】" + changeInfo.getStockSymbol() + 
                changeInfo.getChangeDirection() + String.format("%.2f", Math.abs(changeInfo.getChangePercent())) + "%");
        System.out.println("内容：");
        System.out.println("尊敬的用户：");
        System.out.println("  您关注的股票" + changeInfo.getStockSymbol() + "于" + 
                LocalDateTime.now().format(DATE_TIME_FORMATTER) + "出现异常价格变动：");
        System.out.println("  当前价格：" + String.format("%.2f", changeInfo.getCurrentPrice()));
        System.out.println("  变动幅度：" + changeInfo.getChangeDirection() + 
                String.format("%.2f", Math.abs(changeInfo.getChangePercent())) + "%");
        System.out.println("  " + changeInfo.getRecommendation());
        System.out.println("  感谢您使用贾维斯AI助手！");
        System.out.println("\n");
    }
}
