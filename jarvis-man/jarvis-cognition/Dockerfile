FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && mvn package -DskipTests

COPY target/jarvis-cognition-*.jar app.jar

EXPOSE 8090 9101

ENTRYPOINT ["java", "-jar", "app.jar"]
