# 使用官方Maven镜像构建应用
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 使用官方Java镜像运行应用
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=builder /app/target/java-jarvis-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080 9090

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]
