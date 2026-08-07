package com.skyeai.jarvis.sql.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyeai.jarvis.sql.service.TextToSqlService.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SuperSQL 思路备选 Text-to-SQL 服务
 * v10 新增：使用 Spring AI ChatClient 生成 SQL 并执行（仅 SELECT）
 * 开关：jarvis.text-to-sql.engine=supersql
 * 流程：拉取 schema → 调 ChatClient 生成 SQL → SqlSafetyValidator 校验 → 执行查询 → 返回结果
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "jarvis.text-to-sql.engine", havingValue = "supersql")
public class SuperSqlTextToSqlService {

    @Autowired(required = false)
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TextToSqlService textToSqlService;

    @Autowired
    private ObjectMapper objectMapper;

    private ChatClient chatClient;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostConstruct
    public void init() {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        if (chatClientBuilder != null) {
            this.chatClient = chatClientBuilder.build();
            log.info("SuperSqlTextToSqlService 初始化完成，ChatClient 已就绪");
        } else {
            log.warn("SuperSqlTextToSqlService 初始化完成，但 ChatClient 未配置，调用时将显式报错");
        }
    }

    /**
     * 自然语言查询数据库
     * @param question 自然语言问题
     * @return 查询结果的 JSON 字符串
     */
    @Tool(description = "基于自然语言查询数据库并返回 JSON 结果，仅支持只读 SELECT 查询")
    public String naturalLanguageQuery(
            @ToolParam(description = "自然语言查询问题，例如：查询所有用户数量") String question) {
        if (chatClient == null) {
            log.error("ChatClient 未配置，无法执行自然语言查询。请确认 spring-ai-alibaba-starter-dashscope 已引入且 API Key 已配置");
            throw new IllegalStateException("ChatClient 未配置，无法执行自然语言查询");
        }
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("查询问题不能为空");
        }
        try {
            // 1. 拉取数据库 schema（复用 TextToSqlService.getDatabaseSchema 逻辑）
            String schemaDescription = buildSchemaDescription();
            log.info("已加载数据库 schema，开始生成 SQL，问题：{}", question);

            // 2. 调用 ChatClient 生成 SQL
            String prompt = buildPrompt(schemaDescription, question);
            String raw = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            String sql = extractSql(raw);
            log.info("ChatClient 生成 SQL：{}", sql);

            // 3. 安全校验：仅允许 SELECT
            if (!SqlSafetyValidator.isSelectOnly(sql)) {
                log.error("SQL 安全校验失败，拒绝执行：{}", sql);
                throw new IllegalStateException("SQL 安全校验失败：仅允许只读 SELECT 语句，拒绝执行");
            }

            // 4. 执行查询
            List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, Collections.emptyMap());
            log.info("SQL 执行成功，返回 {} 行数据", rows.size());

            // 5. 返回 JSON 结果
            return objectMapper.writeValueAsString(rows);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("自然语言查询失败：{}", e.getMessage(), e);
            throw new RuntimeException("自然语言查询失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建数据库 schema 描述文本（复用 TextToSqlService.getDatabaseSchema 逻辑）
     */
    private String buildSchemaDescription() {
        List<TableInfo> schema = textToSqlService.getDatabaseSchema();
        if (schema == null || schema.isEmpty()) {
            log.error("数据库 schema 为空，请检查数据库连接或 information_schema 访问权限");
            throw new IllegalStateException("数据库 schema 为空，无法生成 SQL");
        }
        Map<String, List<TableInfo>> byTable = schema.stream()
                .collect(Collectors.groupingBy(TableInfo::getTableName));
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<TableInfo>> entry : byTable.entrySet()) {
            sb.append("表 ").append(entry.getKey()).append("(");
            String columns = entry.getValue().stream()
                    .map(t -> t.getColumnName() + " " + t.getDataType())
                    .collect(Collectors.joining(", "));
            sb.append(columns).append(");\n");
        }
        return sb.toString();
    }

    /**
     * 构建 ChatClient 提示词
     */
    private String buildPrompt(String schemaDescription, String question) {
        return "你是一个专业的 SQL 专家。请根据以下数据库表结构，生成一条只读 SELECT 语句来回答用户问题。\n"
                + "要求：\n"
                + "1. 只能生成 SELECT 语句，严禁 INSERT/UPDATE/DELETE/DROP/ALTER/CREATE/TRUNCATE/GRANT/REVOKE/MERGE 等任何写操作\n"
                + "2. 只输出 SQL 语句本身，不要包含任何解释、markdown 代码块标记或注释\n"
                + "3. 目标数据库类型：PostgreSQL\n"
                + "4. 如无法生成，请直接输出：ERROR\n\n"
                + "数据库表结构：\n" + schemaDescription + "\n"
                + "用户问题：" + question;
    }

    /**
     * 从模型输出中提取 SQL（去除 markdown 代码块标记等）
     */
    private String extractSql(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("ChatClient 返回内容为空，无法提取 SQL");
        }
        String text = raw.trim();
        // 去除 markdown 代码块
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) {
                text = text.substring(firstNewline + 1);
            }
            int fenceEnd = text.lastIndexOf("```");
            if (fenceEnd >= 0) {
                text = text.substring(0, fenceEnd);
            }
            text = text.trim();
        }
        if (text.equalsIgnoreCase("ERROR") || text.isBlank()) {
            throw new IllegalStateException("ChatClient 无法生成有效 SQL：" + raw);
        }
        return text;
    }
}
