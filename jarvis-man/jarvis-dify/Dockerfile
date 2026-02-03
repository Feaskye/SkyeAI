FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/jarvis-dify-*.jar app.jar

EXPOSE 8084 9095

ENTRYPOINT ["java", "-jar", "app.jar"]