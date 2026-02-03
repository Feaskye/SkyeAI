package com.skyeai.jarvis.service;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleParserRegexTest {

    // 简单日程表达的正则表达式
    private static final Pattern SIMPLE_SCHEDULE_PATTERN = Pattern.compile(
            "(?:(明天|后天|大后天|下周一|下周二|下周三|下周四|下周五|下周六|下周日|\\d{4}-\\d{2}-\\d{2}|\\d{1,2}月\\d{1,2}日)\\s*)" +
            "(?:(上午|下午|晚上)?\\s*)" +
            "(?:(\\d{1,2})点(?:(\\d{1,2})分)?)\\s*" +
            "(?:(提醒|安排|预约|设置|开会|会议|约会|培训)\\s*)" +
            "(.+)?",
            Pattern.CASE_INSENSITIVE
    );

    @Test
    public void testRegexPattern() {
        // 测试用例1: 明天下午3点提醒我参加团队会议
        testCase("明天下午3点提醒我参加团队会议");

        // 测试用例2: 后天上午10点安排项目评审
        testCase("后天上午10点安排项目评审");

        // 测试用例3: 大后天晚上8点30分预约医生
        testCase("大后天晚上8点30分预约医生");

        // 测试用例4: 2026年2月14日下午2点约会
        testCase("2026年2月14日下午2点约会");

        // 测试用例5: 3月8日上午9点30分开会
        testCase("3月8日上午9点30分开会");

        // 测试用例6: 下周一上午11点设置提醒
        testCase("下周一上午11点设置提醒");

        // 测试用例7: 下周二下午3点30分安排培训
        testCase("下周二下午3点30分安排培训");

        // 测试用例8: 下周三晚上7点开会
        testCase("下周三晚上7点开会");

        // 测试用例9: 下周四上午10点30分预约客户
        testCase("下周四上午10点30分预约客户");

        // 测试用例10: 下周五下午4点设置提醒
        testCase("下周五下午4点设置提醒");

        // 测试用例11: 明天上午9点开会
        testCase("明天上午9点开会");

        // 测试用例12: 后天下午2点30分约会
        testCase("后天下午2点30分约会");
    }

    private void testCase(String input) {
        System.out.println("测试用例: " + input);
        Matcher matcher = SIMPLE_SCHEDULE_PATTERN.matcher(input);
        if (matcher.find()) {
            System.out.println("匹配成功！");
            String dateStr = matcher.group(1);
            String timeOfDay = matcher.group(2);
            String hourStr = matcher.group(3);
            String minuteStr = matcher.group(4);
            String action = matcher.group(5);
            String title = matcher.group(6);

            System.out.println("解析结果: 日期=" + dateStr + ", 时间段=" + timeOfDay + ", 小时=" + hourStr + ", 分钟=" + minuteStr + ", 动作=" + action + ", 标题=" + title);
        } else {
            System.out.println("匹配失败！");
        }
        System.out.println();
    }
}
