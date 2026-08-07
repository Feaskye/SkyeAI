package com.skyeai.jarvis.sql.controller;

import com.skyeai.jarvis.sql.service.FunctionCallService;
import com.skyeai.jarvis.sql.service.SuperSqlTextToSqlService;
import com.skyeai.jarvis.sql.service.TextToSqlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sql")
public class TextToSqlController {

    @Autowired
    private TextToSqlService textToSqlService;

    @Autowired
    private FunctionCallService functionCallService;

    /**
     * SuperSQL 备选实现（仅当 jarvis.text-to-sql.engine=supersql 时存在）
     */
    @Autowired(required = false)
    private SuperSqlTextToSqlService superSqlTextToSqlService;

    /**
     * 生成SQL语句
     * @param query 自然语言查询
     * @param databaseType 数据库类型
     * @return SQL语句
     */
    @GetMapping("/generate")
    public String generateSql(
            @RequestParam String query,
            @RequestParam(defaultValue = "postgresql") String databaseType) {
        return textToSqlService.generateSql(query, databaseType);
    }

    /**
     * 执行自然语言查询（v10 改造：移除直接执行 SQL 端点以规避 SQL 注入风险）
     * 根据开关 jarvis.text-to-sql.engine 路由：
     *   - supersql：使用 SuperSqlTextToSqlService（ChatClient 生成 + 安全校验 + 仅 SELECT）
     *   - 其他/缺失：回退到 TextToSqlService 历史实现
     * @param query 自然语言查询
     * @param databaseType 数据库类型（仅回退实现使用）
     * @return 查询结果（supersql 引擎返回 JSON 字符串；回退实现返回行列表）
     */
    @PostMapping("/natural-language-query")
    public Object naturalLanguageQuery(
            @RequestParam String query,
            @RequestParam(defaultValue = "postgresql") String databaseType) {
        if (superSqlTextToSqlService != null) {
            log.info("使用 SuperSQL 引擎执行自然语言查询：{}", query);
            return superSqlTextToSqlService.naturalLanguageQuery(query);
        }
        log.info("SuperSQL 引擎未启用，回退到历史实现执行自然语言查询：{}", query);
        return textToSqlService.executeNaturalLanguageQuery(query, databaseType);
    }

    /**
     * 执行自然语言查询（历史端点，保留兼容）
     * @param query 自然语言查询
     * @param databaseType 数据库类型
     * @return 查询结果
     */
    @PostMapping("/natural-language")
    public List<Map<String, Object>> executeNaturalLanguageQuery(
            @RequestParam String query,
            @RequestParam(defaultValue = "postgresql") String databaseType) {
        return textToSqlService.executeNaturalLanguageQuery(query, databaseType);
    }

    /**
     * 获取数据库表结构
     * @return 表结构信息
     */
    @GetMapping("/schema")
    public List<TextToSqlService.TableInfo> getDatabaseSchema() {
        return textToSqlService.getDatabaseSchema();
    }

    /**
     * 获取所有工具
     * @return 工具列表
     */
    @GetMapping("/tools")
    public List<FunctionCallService.ToolDefinition> getAllTools() {
        return functionCallService.getAllTools();
    }

    /**
     * 注册工具
     * @param tool 工具定义
     * @return 是否成功
     */
    @PostMapping("/tools/register")
    public boolean registerTool(@RequestBody FunctionCallService.ToolDefinition tool) {
        functionCallService.registerTool(tool);
        return true;
    }

    /**
     * 健康检查
     * @return 健康状态
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "Text-to-SQL Service is healthy";
    }
}
