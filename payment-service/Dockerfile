# 베이스 이미지 설정 (JDK 17 사용)
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 빌드 결과물 JAR 파일을 복사
COPY build/libs/payment-service.jar /app/payment-service.jar

# 컨테이너가 시작될 때 실행할 명령어 설정
CMD ["java", "-jar", "/app/payment-service.jar"]
