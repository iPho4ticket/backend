FROM openjdk:17-jdk

ARG AUTH_PORT
ARG JWT_SECRET_KEY
ARG JWT_EXPIRATION_TIME
ARG USER_SERVICE_NAME
ARG USER_SERVICE_URL

WORKDIR /ipho

COPY build/libs/auth-service.jar /ipho/auth-service.jar

ENTRYPOINT ["java", "-jar", "/ipho/auth-service.jar"]