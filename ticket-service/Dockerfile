FROM openjdk:17-jdk-slim

WORKDIR /ipho

ARG TICKET_PORT
ARG RDS_HOST
ARG RDS_USERNAME
ARG RDS_PASSWORD

ENV TICKET_PORT=${TICKET_PORT}
ENV RDS_HOST=${RDS_HOST}
ENV RDS_USERNAME=${RDS_USERNAME}
ENV RDS_PASSWORD=${RDS_PASSWORD}

COPY build/libs/ticket-service.jar /ipho/ticket-service.jar

CMD ["java", "-jar", "/ipho/ticket-service.jar"]