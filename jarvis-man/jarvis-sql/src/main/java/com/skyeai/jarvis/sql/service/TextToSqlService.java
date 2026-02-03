package com.skyeai.jarvis.sql.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TextToSqlService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${text-to-sql.timeout}")
    private int timeout;

    @Value("${text-to-sql.max-retries}")
    private int maxRetries;

    @Value("${text-to-sql.supported-databases}")
    private List<String> supportedDatabases;

    @PostConstruct
    public void init() {
        System.out.println("TextToSqlService initialized successfully");
        System.out.println("Supported databases: " + supportedDatabases);
    }

    /**
     * 将自然语言转换为SQL
     * @param query 自然语言查询
     * @param databaseType 数据库类型
     * @return SQL语句
     */
    public String generateSql(String query, String databaseType) {
        // 实际应用中应该使用AI模型生成SQL
        try {
            // 简单的SQL生成示例
            if (query.contains("所有用户")) {
                return "SELECT * FROM users";
            } else if (query.contains("用户数量")) {
                return "SELECT COUNT(*) FROM users";
            } else if (query.contains("最近的订单")) {
                return "SELECT * FROM orders ORDER BY created_at DESC LIMIT 10";
            } else {
                return "SELECT * FROM " + extractTableName(query);
            }
        } catch (Exception e) {
            System.err.println("Failed to generate SQL: " + e.getMessage());
            throw new RuntimeException("Failed to generate SQL", e);
        }
    }

    /**
     * 执行SQL语句
     * @param sql SQL语句
     * @return 执行结果
     */
    public List<Map<String, Object>> executeSql(String sql) {
        try {
            System.out.println("Executing SQL: " + sql);
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            System.err.println("Failed to execute SQL: " + e.getMessage());
            throw new RuntimeException("Failed to execute SQL", e);
        }
    }

    /**
     * 执行自然语言查询
     * @param query 自然语言查询
     * @param databaseType 数据库类型
     * @return 查询结果
     */
    public List<Map<String, Object>> executeNaturalLanguageQuery(String query, String databaseType) {
        // 生成SQL
        String sql = generateSql(query, databaseType);
        // 执行SQL
        return executeSql(sql);
    }

    /**
     * 提取表名
     * @param query 自然语言查询
     * @return 表名
     */
    private String extractTableName(String query) {
        // 简单的表名提取逻辑
        if (query.contains("用户")) {
            return "users";
        } else if (query.contains("订单")) {
            return "orders";
        } else if (query.contains("产品")) {
            return "products";
        } else if (query.contains("库存")) {
            return "inventory";
        } else {
            return "unknown";
        }
    }

    /**
     * 获取数据库表结构
     * @return 表结构信息
     */
    public List<TableInfo> getDatabaseSchema() {
        try {
            String sql = "SELECT table_name, column_name, data_type FROM information_schema.columns WHERE table_schema = 'public'";
            return jdbcTemplate.query(sql, new RowMapper<TableInfo>() {
                @Override
                public TableInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                    TableInfo info = new TableInfo();
                    info.setTableName(rs.getString("table_name"));
                    info.setColumnName(rs.getString("column_name"));
                    info.setDataType(rs.getString("data_type"));
                    return info;
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to get database schema: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 表结构信息类
     */
    public static class TableInfo {
        private String tableName;
        private String columnName;
        private String dataType;

        // Getters and Setters
        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }
    }
}
