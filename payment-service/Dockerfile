FROM openjdk:17-jdk-slim

WORKDIR /ipho

ARG PAYMENT_PORT
ARG RDS_HOST
ARG RDS_USERNAME
ARG RDS_PASSWORD

ENV PAYMENT_PORT=${PAYMENT_PORT}
ENV RDS_HOST=${RDS_HOST}
ENV RDS_USERNAME=${RDS_USERNAME}
ENV RDS_PASSWORD=${RDS_PASSWORD}

COPY build/libs/payment-service.jar /ipho/payment-service.jar

CMD ["java", "-jar", "/ipho/payment-service.jar"]
