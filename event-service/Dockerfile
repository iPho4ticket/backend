FROM openjdk:17-jdk-slim

ARG EVENT_PORT
ARG RDS_HOST
ARG RDS_USERNAME
ARG RDS_PASSWORD

ENV EVENT_PORT=${EVENT_PORT}
ENV RDS_HOST=${RDS_HOST}
ENV RDS_USERNAME=${RDS_USERNAME}
ENV RDS_PASSWORD=${RDS_PASSWORD}

WORKDIR /ipho

COPY build/libs/event-service.jar /ipho/event-service.jar

ENTRYPOINT ["java", "-jar", "/ipho/event-service.jar"]