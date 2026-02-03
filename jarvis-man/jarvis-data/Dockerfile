FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/jarvis-data-*.jar app.jar

EXPOSE 8081 9091

ENTRYPOINT ["java", "-jar", "app.jar"]