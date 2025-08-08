@echo off
chcp 65001 >nul
title Simple LLM Backend

echo ========================================
echo  Simple LLM Backend Starting...
echo ========================================

REM Java 11 확인
echo Checking Java version...
java -version 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java 11이 설치되지 않았거나 PATH에 없습니다.
    echo Java 11을 설치하고 JAVA_HOME을 설정해주세요.
    echo.
    echo 다운로드 링크: https://adoptium.net/temurin/releases/
    pause
    exit /b 1
)

REM 환경변수 파일 로드 (개선된 파싱)
if exist ".env" (
    echo Loading environment variables from .env file...
    for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
        REM 주석 라인(#으로 시작) 건너뛰기
        echo %%a | findstr /r "^[[:space:]]*#" >nul
        if errorlevel 1 (
            REM 빈 라인 건너뛰기
            if not "%%a"=="" if not "%%b"=="" (
                REM 앞뒤 공백 제거
                for /f "tokens=*" %%x in ("%%a") do for /f "tokens=*" %%y in ("%%b") do (
                    set "%%x=%%y"
                    echo   %%x=%%y
                )
            )
        )
    )
    echo Environment variables loaded successfully.
) else (
    echo .env file not found, using default values...
    echo You can create .env file from .env.example template.
)

echo.

REM 기본 환경변수 설정
if not defined VLLM_BASE_URL set "VLLM_BASE_URL=http://localhost:8000"
if not defined SGLANG_BASE_URL set "SGLANG_BASE_URL=http://localhost:30000"
if not defined SPRING_PROFILES_ACTIVE set "SPRING_PROFILES_ACTIVE=dev"
if not defined SERVER_PORT set "SERVER_PORT=8080"
if not defined VLLM_ENABLED set "VLLM_ENABLED=true"
if not defined SGLANG_ENABLED set "SGLANG_ENABLED=true"

REM JVM 옵션 설정 (Windows 최적화)
if not defined JAVA_OPTS set "JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Djava.awt.headless=true"

echo Configuration:
echo   Server Port      : %SERVER_PORT%
echo   Spring Profile   : %SPRING_PROFILES_ACTIVE%
echo   vLLM Enabled     : %VLLM_ENABLED%
echo   vLLM URL         : %VLLM_BASE_URL%
echo   SGLang Enabled   : %SGLANG_ENABLED%
echo   SGLang URL       : %SGLANG_BASE_URL%
echo   Java Options     : %JAVA_OPTS%
echo.

REM JAR 파일 경로 확인
set "JAR_FILE=build\libs\simple-llm-backend-0.0.1-SNAPSHOT.jar"
if not exist "%JAR_FILE%" (
    echo [ERROR] JAR file not found at %JAR_FILE%
    echo Please run 'gradlew.bat build' first to build the application.
    echo.
    echo Example:
    echo   gradlew.bat clean build
    echo.
    pause
    exit /b 1
)

REM 로그 디렉토리 생성
if not exist "logs" (
    mkdir logs
    echo Created logs directory.
)

REM PID 파일 경로 설정 (프로세스 관리용)
set "PID_FILE=logs\simple-llm-backend.pid"

echo Starting Simple LLM Backend...
echo JAR: %JAR_FILE%
echo PID file: %PID_FILE%
echo.

REM 애플리케이션 실행
echo [%date% %time%] Starting application...
java %JAVA_OPTS% -jar "%JAR_FILE%" --server.port=%SERVER_PORT% --spring.profiles.active=%SPRING_PROFILES_ACTIVE%

REM 종료 코드 확인
set EXIT_CODE=%ERRORLEVEL%
echo.
echo [%date% %time%] Application stopped with exit code: %EXIT_CODE%

if %EXIT_CODE% neq 0 (
    echo [ERROR] Application failed to start or crashed.
    echo.
    echo Troubleshooting steps:
    echo 1. Check logs in the logs\ directory
    echo 2. Verify Java 11 is installed
    echo 3. Ensure port %SERVER_PORT% is available
    echo 4. Check if LLM servers are running (if enabled)
    echo 5. Verify database configuration (if using Oracle)
    echo.
    echo Common solutions:
    echo - Check if another instance is already running
    echo - Verify firewall settings
    echo - Check available memory (requires at least 2GB)
    echo.
    pause
) else (
    echo Application stopped normally.
)

exit /b %EXIT_CODE%