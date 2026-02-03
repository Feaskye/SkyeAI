package com.skyeai.jarvis.service;

import com.skyeai.jarvis.model.ScheduleEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScheduleParserIntegrationTest {

    @Autowired
    private ScheduleParserService scheduleParserService;

    @Test
    public void testScheduleParsing() {
        // 测试用例1: 明天下午3点提醒我参加团队会议
        testParsing("明天下午3点提醒我参加团队会议");

        // 测试用例2: 后天上午10点安排项目评审
        testParsing("后天上午10点安排项目评审");

        // 测试用例3: 大后天晚上8点30分预约医生
        testParsing("大后天晚上8点30分预约医生");

        // 测试用例4: 2026年2月14日下午2点约会
        testParsing("2026年2月14日下午2点约会");

        // 测试用例5: 3月8日上午9点30分开会
        testParsing("3月8日上午9点30分开会");

        // 测试用例6: 下周一上午11点设置提醒
        testParsing("下周一上午11点设置提醒");

        // 测试用例7: 下周二下午3点30分安排培训
        testParsing("下周二下午3点30分安排培训");

        // 测试用例8: 下周三晚上7点开会
        testParsing("下周三晚上7点开会");

        // 测试用例9: 下周四上午10点30分预约客户
        testParsing("下周四上午10点30分预约客户");

        // 测试用例10: 下周五下午4点设置提醒
        testParsing("下周五下午4点设置提醒");
    }

    private void testParsing(String input) {
        System.out.println("\n测试用例: " + input);
        try {
            ScheduleEvent event = scheduleParserService.parseSchedule(input);
            if (event != null) {
                System.out.println("解析成功！");
                System.out.println("标题: " + event.getTitle());
                System.out.println("时间: " + event.getDateTime());
                System.out.println("描述: " + event.getDescription());
            } else {
                System.out.println("解析失败！");
            }
        } catch (Exception e) {
            System.err.println("解析时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
