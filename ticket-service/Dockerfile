FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/ticket-service.jar /app/payment-service.jar

CMD ["java", "-jar", "/app/ticket-service.jar"]