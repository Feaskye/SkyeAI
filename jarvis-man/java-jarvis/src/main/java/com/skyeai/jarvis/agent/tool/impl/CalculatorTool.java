package com.skyeai.jarvis.agent.tool.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * 计算器工具
 * v10 新增：使用 Spring AI 2.0 @Tool 注解声明
 * 支持基本四则运算和表达式求值
 */
@Slf4j
@Component
public class CalculatorTool {

    private final ScriptEngine scriptEngine;

    public CalculatorTool() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.scriptEngine = manager.getEngineByName("js");
    }

    @Tool(description = "计算数学表达式的值，支持加减乘除、括号等运算，如 (3+5)*2/4")
    public String calculate(
            @ToolParam(description = "数学表达式，如 3+5*2 或 (10-3)/2") String expression) {
        if (expression == null || expression.isBlank()) {
            return "表达式不能为空";
        }

        // 安全检查：只允许数字和运算符
        String sanitized = expression.replaceAll("[^0-9+\\-*/().\\s]", "");
        if (!sanitized.equals(expression.trim())) {
            log.warn("表达式包含非法字符，已过滤: {} -> {}", expression, sanitized);
        }

        try {
            Object result = scriptEngine.eval(sanitized);
            log.debug("计算结果: {} = {}", sanitized, result);
            return String.format("%s = %s", sanitized, result);
        } catch (Exception e) {
            log.error("计算失败: {}", expression, e);
            return "计算失败，请检查表达式格式: " + expression;
        }
    }

    @Tool(description = "计算百分比，如 80 的 15% 是多少")
    public String percentage(
            @ToolParam(description = "基数") double base,
            @ToolParam(description = "百分比，如 15 表示 15%") double percent) {
        double result = base * percent / 100;
        log.debug("百分比计算: {} 的 {}% = {}", base, percent, result);
        return String.format("%.2f 的 %.2f%% = %.2f", base, percent, result);
    }
}
