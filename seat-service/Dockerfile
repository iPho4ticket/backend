FROM openjdk:17-jdk-slim

WORKDIR /ipho

ARG SEAT_PORT
ARG RDS_HOST
ARG RDS_USERNAME
ARG RDS_PASSWORD

ENV SEAT_PORT=${SEAT_PORT}
ENV RDS_HOST=${RDS_HOST}
ENV RDS_USERNAME=${RDS_USERNAME}
ENV RDS_PASSWORD=${RDS_PASSWORD}

COPY build/libs/seat-service.jar /ipho/seat-service.jar

ENTRYPOINT ["java", "-jar", "/ipho/seat-service.jar"]