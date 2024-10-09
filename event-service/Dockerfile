FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/event-service.jar /app/event-service.jar

ENTRYPOINT ["java", "-jar", "/app/event-service.jar"]