# Multi-stage build for Simple LLM Backend
# Windows 호환성을 고려한 Docker 이미지

# Build stage
FROM openjdk:11-jdk-slim as builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 빌드 파일 복사
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle settings.gradle gradle.properties ./

# 소스 코드 복사
COPY src src

# 실행 권한 부여 (Linux 환경용)
RUN chmod +x gradlew

# 애플리케이션 빌드
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM openjdk:11-jre-slim

# 메타데이터 설정
LABEL maintainer="Simple LLM Backend Team"
LABEL version="0.0.1-SNAPSHOT"
LABEL description="Simple LLM Backend - Java 11, Spring Boot 2.3.2"

# 필수 패키지 설치
RUN apt-get update && \
    apt-get install -y curl wget && \
    rm -rf /var/lib/apt/lists/*

# 비루트 사용자 생성
RUN groupadd -r llmuser && useradd -r -g llmuser llmuser

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/simple-llm-backend-0.0.1-SNAPSHOT.jar app.jar

# 로그 디렉토리 생성
RUN mkdir -p logs && chown -R llmuser:llmuser /app

# 비루트 사용자로 전환
USER llmuser

# 포트 노출
EXPOSE 8080

# 환경변수 설정
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Djava.awt.headless=true"

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]