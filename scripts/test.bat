@echo off
title LLM Backend API Test

echo === Simple LLM Backend API Test ===

REM PowerShell이 사용 가능한지 확인
powershell -Command "Get-Host" >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo Using PowerShell for HTTP requests...
    goto :powershell_test
) else (
    echo PowerShell not available, using curl if available...
    curl --version >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        goto :curl_test
    ) else (
        echo Neither PowerShell nor curl available for testing.
        echo Please install curl or use PowerShell script instead.
        pause
        exit /b 1
    )
)

:powershell_test
echo 1. Health Check...
powershell -Command "try { (Invoke-WebRequest -Uri 'http://localhost:8080/api/health' -UseBasicParsing).Content } catch { 'Health check failed: ' + $_.Exception.Message }"

echo.
echo 2. Application Info...
powershell -Command "try { (Invoke-WebRequest -Uri 'http://localhost:8080/api/info' -UseBasicParsing).Content } catch { 'Info check failed: ' + $_.Exception.Message }"

echo.
echo 3. vLLM Test...
powershell -Command "$body = @{ prompt = 'Hello, world!'; engine = 'vllm'; max_tokens = 50 } | ConvertTo-Json; try { (Invoke-WebRequest -Uri 'http://localhost:8080/api/generate' -Method POST -Body $body -ContentType 'application/json' -UseBasicParsing).Content } catch { 'vLLM test failed: ' + $_.Exception.Message }"

echo.
echo 4. SGLang Test...
powershell -Command "$body = @{ prompt = 'What is Spring Boot?'; engine = 'sglang'; max_tokens = 100 } | ConvertTo-Json; try { (Invoke-WebRequest -Uri 'http://localhost:8080/api/generate' -Method POST -Body $body -ContentType 'application/json' -UseBasicParsing).Content } catch { 'SGLang test failed: ' + $_.Exception.Message }"

goto :end

:curl_test
echo 1. Health Check...
curl -s http://localhost:8080/api/health

echo.
echo 2. Application Info...
curl -s http://localhost:8080/api/info

echo.
echo 3. vLLM Test...
curl -X POST http://localhost:8080/api/generate ^
  -H "Content-Type: application/json" ^
  -d "{\"prompt\": \"Hello, world!\", \"engine\": \"vllm\", \"max_tokens\": 50}"

echo.
echo 4. SGLang Test...
curl -X POST http://localhost:8080/api/generate ^
  -H "Content-Type: application/json" ^
  -d "{\"prompt\": \"What is Spring Boot?\", \"engine\": \"sglang\", \"max_tokens\": 100}"

:end
echo.
echo === Test Completed ===
pause