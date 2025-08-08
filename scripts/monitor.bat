@echo off
title LLM Backend Monitor

:monitor_loop
echo === %date% %time% ===

REM 프로세스 확인
tasklist | findstr java >nul
if %ERRORLEVEL% equ 0 (
    echo ✅ Java process is running
) else (
    echo ❌ Java process not found
)

REM 헬스체크 (PowerShell 사용)
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/health' -UseBasicParsing -TimeoutSec 5; if ($response.StatusCode -eq 200) { Write-Host '✅ Health check passed' -ForegroundColor Green } else { Write-Host '❌ Health check failed' -ForegroundColor Red } } catch { Write-Host '❌ Health check error: ' $_.Exception.Message -ForegroundColor Red }"

REM 메모리 사용량 확인
for /f "tokens=5" %%a in ('tasklist /FI "IMAGENAME eq java.exe" /FO TABLE ^| findstr java') do (
    echo 💾 Memory usage: %%a
)

echo ---
timeout /t 60 /nobreak >nul
goto monitor_loop