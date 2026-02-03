FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && mvn package -DskipTests

COPY target/jarvis-frontend-*.jar app.jar

EXPOSE 8087 9098

ENTRYPOINT ["java", "-jar", "app.jar"]
