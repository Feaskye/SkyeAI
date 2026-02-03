FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/jarvis-rag-*.jar app.jar

EXPOSE 8082 9093

ENTRYPOINT ["java", "-jar", "app.jar"]