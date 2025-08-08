@echo off
echo ========================================
echo  Simple LLM Backend Monitor
echo ========================================

set "BASE_URL=http://localhost:8080/api"

:MONITOR_LOOP
cls
echo [%date% %time%] Monitoring Simple LLM Backend...
echo.

REM Health Check
echo Checking application health...
curl -s %BASE_URL%/health > nul
if %ERRORLEVEL% equ 0 (
    echo [OK] Application is running
) else (
    echo [ERROR] Application is not responding
)

REM Detailed Health Check
echo.
echo Detailed health status:
curl -s %BASE_URL%/health/detailed | findstr /C:"vllm" /C:"sglang" /C:"application"

REM Memory and CPU info
echo.
echo System resources:
wmic process where "CommandLine like '%%simple-llm-backend%%'" get ProcessId,PageFileUsage,WorkingSetSize /format:table

echo.
echo Press Ctrl+C to stop monitoring...
echo Refreshing in 30 seconds...
echo.

timeout /t 30 /nobreak > nul
goto MONITOR_LOOP