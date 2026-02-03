FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/jarvis-knowledge-*.jar app.jar

EXPOSE 8083 9094

ENTRYPOINT ["java", "-jar", "app.jar"]