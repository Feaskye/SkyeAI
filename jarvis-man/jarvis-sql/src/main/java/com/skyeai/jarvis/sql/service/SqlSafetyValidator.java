package com.skyeai.jarvis.sql.service;

import lombok.extern.slf4j.Slf4j;
import java.util.regex.Pattern;

/**
 * SQL 安全校验器
 * v10 新增：强制只读 SELECT，禁止 DDL/DML
 */
@Slf4j
public class SqlSafetyValidator {

    private static final Pattern FORBIDDEN = Pattern.compile(
        "\\b(INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|TRUNCATE|GRANT|REVOKE|MERGE)\\b",
        Pattern.CASE_INSENSITIVE);

    /**
     * 校验 SQL 是否仅包含 SELECT 语句
     */
    public static boolean isSelectOnly(String sql) {
        if (sql == null || sql.isBlank()) return false;
        if (FORBIDDEN.matcher(sql).find()) return false;
        // 简单校验：首关键词应为 SELECT（忽略前导空格和注释）
        String trimmed = sql.trim().toUpperCase();
        // 跳过 SQL 注释
        while (trimmed.startsWith("--") || trimmed.startsWith("/*")) {
            if (trimmed.startsWith("--")) {
                int idx = trimmed.indexOf('\n');
                if (idx < 0) return false;
                trimmed = trimmed.substring(idx + 1).trim().toUpperCase();
            } else {
                int idx = trimmed.indexOf("*/");
                if (idx < 0) return false;
                trimmed = trimmed.substring(idx + 2).trim().toUpperCase();
            }
        }
        boolean isSelect = trimmed.startsWith("SELECT") || trimmed.startsWith("WITH");
        if (!isSelect) {
            log.warn("SQL 校验失败：非 SELECT 语句 - {}", sql.substring(0, Math.min(sql.length(), 50)));
        }
        return isSelect;
    }
}
