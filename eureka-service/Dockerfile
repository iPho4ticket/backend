FROM openjdk:17-jdk

WORKDIR /ipho

COPY build/libs/eureka-service.jar /ipho/eureka-service.jar

ENTRYPOINT ["java", "-jar", "/ipho/eureka-service.jar"]