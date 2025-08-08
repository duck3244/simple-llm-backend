@echo off
title Simple LLM Backend

REM Java 11 확인
echo Checking Java version...
java -version
if %ERRORLEVEL% neq 0 (
    echo Java 11이 설치되지 않았거나 PATH에 없습니다.
    echo Java 11을 설치하고 JAVA_HOME을 설정해주세요.
    pause
    exit /b 1
)

REM 환경변수 설정
if exist ".env" (
    echo Loading environment variables from .env...
    for /f "delims=" %%i in (.env) do set %%i
)

REM 기본 환경변수 설정
if not defined VLLM_BASE_URL set VLLM_BASE_URL=http://localhost:8000
if not defined SGLANG_BASE_URL set SGLANG_BASE_URL=http://localhost:30000
if not defined SPRING_PROFILES_ACTIVE set SPRING_PROFILES_ACTIVE=dev

REM JVM 옵션 설정 (Windows 최적화)
set JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Djava.awt.headless=true

REM 로그 디렉토리 생성
if not exist "logs" mkdir logs

echo Starting Simple LLM Backend...
echo VLLM URL: %VLLM_BASE_URL%
echo SGLang URL: %SGLANG_BASE_URL%
echo Profile: %SPRING_PROFILES_ACTIVE%

REM 애플리케이션 실행
java %JAVA_OPTS% -jar build\libs\simple-llm-backend-0.0.1-SNAPSHOT.jar

if %ERRORLEVEL% neq 0 (
    echo Application failed to start. Check logs for details.
    pause
)