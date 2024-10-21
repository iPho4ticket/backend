FROM openjdk:17-jdk

WORKDIR /ipho

ARG USER_PORT
ARG RDS_HOST
ARG RDS_USERNAME
ARG RDS_PASSWORD

ENV USER_PORT=${USER_PORT}
ENV RDS_HOST=${RDS_HOST}
ENV RDS_USERNAME=${RDS_USERNAME}
ENV RDS_PASSWORD=${RDS_PASSWORD}

COPY build/libs/user-service.jar /ipho/user-service.jar

ENTRYPOINT ["java", "-jar", "/ipho/user-service.jar"]