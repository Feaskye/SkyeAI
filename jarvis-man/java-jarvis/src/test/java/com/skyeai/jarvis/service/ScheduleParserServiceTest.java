package com.skyeai.jarvis.service;

import com.skyeai.jarvis.model.ScheduleEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScheduleParserServiceTest {

    @Autowired
    private ScheduleParserService scheduleParserService;

    @Test
    public void testParseSchedule() {
        // 测试用例1: 明天下午3点提醒我参加团队会议
        String testCase1 = "明天下午3点提醒我参加团队会议";
        System.out.println("测试用例1: " + testCase1);
        ScheduleEvent event1 = scheduleParserService.parseSchedule(testCase1);
        System.out.println("结果1: " + event1);
        System.out.println();

        // 测试用例2: 后天上午10点安排项目评审
        String testCase2 = "后天上午10点安排项目评审";
        System.out.println("测试用例2: " + testCase2);
        ScheduleEvent event2 = scheduleParserService.parseSchedule(testCase2);
        System.out.println("结果2: " + event2);
        System.out.println();

        // 测试用例3: 大后天晚上8点30分预约医生
        String testCase3 = "大后天晚上8点30分预约医生";
        System.out.println("测试用例3: " + testCase3);
        ScheduleEvent event3 = scheduleParserService.parseSchedule(testCase3);
        System.out.println("结果3: " + event3);
        System.out.println();

        // 测试用例4: 2026年2月14日下午2点约会
        String testCase4 = "2026年2月14日下午2点约会";
        System.out.println("测试用例4: " + testCase4);
        ScheduleEvent event4 = scheduleParserService.parseSchedule(testCase4);
        System.out.println("结果4: " + event4);
        System.out.println();

        // 测试用例5: 3月8日上午9点30分开会
        String testCase5 = "3月8日上午9点30分开会";
        System.out.println("测试用例5: " + testCase5);
        ScheduleEvent event5 = scheduleParserService.parseSchedule(testCase5);
        System.out.println("结果5: " + event5);
        System.out.println();

        // 测试用例6: 下周一上午11点设置提醒
        String testCase6 = "下周一上午11点设置提醒";
        System.out.println("测试用例6: " + testCase6);
        ScheduleEvent event6 = scheduleParserService.parseSchedule(testCase6);
        System.out.println("结果6: " + event6);
        System.out.println();

        // 测试用例7: 下周二下午3点30分安排培训
        String testCase7 = "下周二下午3点30分安排培训";
        System.out.println("测试用例7: " + testCase7);
        ScheduleEvent event7 = scheduleParserService.parseSchedule(testCase7);
        System.out.println("结果7: " + event7);
        System.out.println();

        // 测试用例8: 下周三晚上7点开会
        String testCase8 = "下周三晚上7点开会";
        System.out.println("测试用例8: " + testCase8);
        ScheduleEvent event8 = scheduleParserService.parseSchedule(testCase8);
        System.out.println("结果8: " + event8);
        System.out.println();

        // 测试用例9: 下周四上午10点30分预约客户
        String testCase9 = "下周四上午10点30分预约客户";
        System.out.println("测试用例9: " + testCase9);
        ScheduleEvent event9 = scheduleParserService.parseSchedule(testCase9);
        System.out.println("结果9: " + event9);
        System.out.println();

        // 测试用例10: 下周五下午4点设置提醒
        String testCase10 = "下周五下午4点设置提醒";
        System.out.println("测试用例10: " + testCase10);
        ScheduleEvent event10 = scheduleParserService.parseSchedule(testCase10);
        System.out.println("结果10: " + event10);
        System.out.println();
    }
}
