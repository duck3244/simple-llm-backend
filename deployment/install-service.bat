@echo off
echo ========================================
echo  Installing Simple LLM Backend Service
echo ========================================

REM 관리자 권한 확인
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo This script requires administrator privileges.
    echo Please run as administrator.
    pause
    exit /b 1
)

REM 현재 디렉토리 확인
if not exist "simple-llm-backend-0.0.1-SNAPSHOT.jar" (
    echo Error: JAR file not found in current directory.
    echo Please copy the JAR file to this directory first.
    pause
    exit /b 1
)

REM WinSW 다운로드 확인
if not exist "winsw.exe" (
    echo Downloading WinSW...
    powershell -Command "try { Invoke-WebRequest -Uri 'https://github.com/winsw/winsw/releases/latest/download/WinSW-x64.exe' -OutFile 'winsw.exe' } catch { Write-Host 'Failed to download WinSW. Please download manually.' -ForegroundColor Red; exit 1 }"
    if %errorLevel% neq 0 (
        echo Failed to download WinSW. Please download manually from:
        echo https://github.com/winsw/winsw/releases/latest/download/WinSW-x64.exe
        pause
        exit /b 1
    )
)

REM 서비스 XML 파일 생성
echo Creating service configuration...
(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<service^>
echo     ^<id^>SimpleLLMBackend^</id^>
echo     ^<name^>Simple LLM Backend Service^</name^>
echo     ^<description^>Simple LLM Backend REST API Service^</description^>
echo     
echo     ^<executable^>java^</executable^>
echo     ^<arguments^>-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -jar simple-llm-backend-0.0.1-SNAPSHOT.jar^</arguments^>
echo     
echo     ^<workingdirectory^>%%BASE%%^</workingdirectory^>
echo     
echo     ^<env name="SPRING_PROFILES_ACTIVE" value="prod"/^>
echo     ^<env name="VLLM_BASE_URL" value="http://localhost:8000"/^>
echo     ^<env name="SGLANG_BASE_URL" value="http://localhost:30000"/^>
echo     
echo     ^<logpath^>%%BASE%%\logs^</logpath^>
echo     ^<log mode="roll-by-size"^>
echo         ^<sizeThreshold^>10MB^</sizeThreshold^>
echo         ^<keepFiles^>8^</keepFiles^>
echo     ^</log^>
echo     
echo     ^<onfailure action="restart" delay="10 sec"/^>
echo     ^<onfailure action="restart" delay="20 sec"/^>
echo     ^<onfailure action="none"/^>
echo     
echo     ^<resetfailure^>1 hour^</resetfailure^>
echo ^</service^>
) > SimpleLLMBackend.xml

REM 로그 디렉토리 생성
if not exist "logs" mkdir logs

REM 서비스 설치
echo Installing Windows service...
winsw.exe install SimpleLLMBackend.xml

if %ERRORLEVEL% equ 0 (
    echo Service installed successfully!
    echo.
    echo Starting service...
    net start SimpleLLMBackend
    
    if %ERRORLEVEL% equ 0 (
        echo Service started successfully!
        echo.
        echo You can manage the service using:
        echo   - Start: net start SimpleLLMBackend
        echo   - Stop:  net stop SimpleLLMBackend
        echo   - Status: sc query SimpleLLMBackend
        echo.
        echo Service logs are available in: %CD%\logs\
    ) else (
        echo Failed to start service. Check logs for details.
    )
) else (
    echo Failed to install service.
    echo Check if Java is properly installed and accessible from PATH.
)

echo.
echo Installation complete. Press any key to exit.
pause# README와 일치하도록 수정된 파일들

## 1. 누락된 실행 스크립트

### `scripts/run.bat`
```batch
@echo off
echo ========================================
echo  Simple LLM Backend Starting...
echo ========================================

REM 환경변수 파일 로드
if exist ".env" (
    echo Loading environment variables from .env file...
    for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
        if not "%%a"=="" if not "%%b"=="" (
            set "%%a=%%b"
        )
    )
) else (
    echo .env file not found, using default values...
)

REM Java 옵션 설정
set "JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Djava.awt.headless=true"

REM Spring 프로필 기본값 설정
if not defined SPRING_PROFILES_ACTIVE (
    set "SPRING_PROFILES_ACTIVE=dev"
)

echo Java Options: %JAVA_OPTS%
echo Spring Profile: %SPRING_PROFILES_ACTIVE%
echo vLLM URL: %VLLM_BASE_URL%
echo SGLang URL: %SGLANG_BASE_URL%
echo.

REM JAR 파일 경로 확인
set "JAR_FILE=build\libs\simple-llm-backend-0.0.1-SNAPSHOT.jar"
if not exist "%JAR_FILE%" (
    echo Error: JAR file not found at %JAR_FILE%
    echo Please run 'gradlew.bat build' first
    pause
    exit /b 1
)

REM 로그 디렉토리 생성
if not exist "logs" mkdir logs

echo Starting application...
echo.
java %JAVA_OPTS% -jar "%JAR_FILE%" --spring.profiles.active=%SPRING_PROFILES_ACTIVE%

if %ERRORLEVEL% neq 0 (
    echo.
    echo Application failed to start. Check logs for details.
    pause
)