FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/seat-service.jar /app/seat-service.jar

ENTRYPOINT ["java", "-jar", "/app/seat-service.jar"]