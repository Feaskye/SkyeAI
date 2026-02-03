FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/jarvis-advisor-*.jar app.jar

EXPOSE 8086 9097

ENTRYPOINT ["java", "-jar", "app.jar"]