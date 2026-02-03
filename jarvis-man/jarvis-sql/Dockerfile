FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/jarvis-sql-*.jar app.jar

EXPOSE 8085 9096

ENTRYPOINT ["java", "-jar", "app.jar"]