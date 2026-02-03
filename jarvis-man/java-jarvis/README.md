# Java Jarvis 项目

## 环境要求
- Java 17
- Maven 3.6+
- Redis
- MySQL

## 快速开始

### 使用 Java 17 运行项目

由于系统默认可能使用的是 Java 8，而本项目需要 Java 17，因此提供了专门的脚本：

```powershell
# 编译项目
.
un-with-java17.ps1 compile

# 运行测试
.
un-with-java17.ps1 test

# 打包项目
.
un-with-java17.ps1 package

# 运行应用
.
un-with-java17.ps1 spring-boot:run
```

### 脚本说明
- `run-with-java17.ps1`: 自动设置 Java 17 环境并执行 Maven 命令
- Java 17 路径: `D:\Program Files\JetBrains\javajdks\temurin-17.0.17`

### 项目结构
- `src/main/java`: 源代码
- `src/main/resources`: 配置文件
- `src/test`: 测试代码

### 核心功能
- 股票监控与提醒
- 日程管理
- AI 聊天
- Redis 缓存
- MySQL 数据持久化

### 配置文件
- `application.properties`: Spring Boot 配置
- `RedisConfig.java`: Redis 配置
- `RestTemplateConfig.java`: HTTP 客户端配置

### 常见问题

#### RedisTemplate bean 找不到
已通过在 `RedisConfig.java` 中显式配置 `RedisTemplate<?, ?>` bean 解决。

#### Java 版本问题
使用 `run-with-java17.ps1` 脚本确保使用 Java 17 运行项目。

#### MySQL 连接
确保本地 MySQL 服务已启动，且配置了正确的用户名和密码。

#### Redis 连接
确保本地 Redis 服务已启动，默认端口 6379。
