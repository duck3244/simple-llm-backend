@echo off
echo ========================================
echo  Simple LLM Backend API Test
echo ========================================

set "BASE_URL=http://localhost:8080/api"

echo Testing Health Check...
curl -s %BASE_URL%/health
if %ERRORLEVEL% equ 0 (
    echo [PASS] Health check successful
) else (
    echo [FAIL] Health check failed
)
echo.

echo Testing Detailed Health Check...
curl -s %BASE_URL%/health/detailed
if %ERRORLEVEL% equ 0 (
    echo [PASS] Detailed health check successful
) else (
    echo [FAIL] Detailed health check failed
)
echo.

echo Testing Application Info...
curl -s %BASE_URL%/info
if %ERRORLEVEL% equ 0 (
    echo [PASS] Application info successful
) else (
    echo [FAIL] Application info failed
)
echo.

echo Testing vLLM Generation...
curl -s -X POST %BASE_URL%/generate ^
  -H "Content-Type: application/json" ^
  -d "{\"prompt\": \"Hello, world!\", \"engine\": \"vllm\", \"max_tokens\": 50, \"temperature\": 0.7}"
if %ERRORLEVEL% equ 0 (
    echo [PASS] vLLM generation test successful
) else (
    echo [FAIL] vLLM generation test failed
)
echo.

echo Testing SGLang Generation...
curl -s -X POST %BASE_URL%/generate ^
  -H "Content-Type: application/json" ^
  -d "{\"prompt\": \"What is AI?\", \"engine\": \"sglang\", \"max_tokens\": 100, \"temperature\": 0.5}"
if %ERRORLEVEL% equ 0 (
    echo [PASS] SGLang generation test successful
) else (
    echo [FAIL] SGLang generation test failed
)
echo.

echo ========================================
echo  API Test Complete
echo ========================================
pause