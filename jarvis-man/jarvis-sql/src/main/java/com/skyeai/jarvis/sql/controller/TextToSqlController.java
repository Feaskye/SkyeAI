package com.skyeai.jarvis.sql.controller;

import com.skyeai.jarvis.sql.service.FunctionCallService;
import com.skyeai.jarvis.sql.service.TextToSqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sql")
public class TextToSqlController {

    @Autowired
    private TextToSqlService textToSqlService;

    @Autowired
    private FunctionCallService functionCallService;

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
     * 执行SQL语句
     * @param sql SQL语句
     * @return 执行结果
     */
    @PostMapping("/execute")
    public List<Map<String, Object>> executeSql(@RequestParam String sql) {
        return textToSqlService.executeSql(sql);
    }

    /**
     * 执行自然语言查询
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
