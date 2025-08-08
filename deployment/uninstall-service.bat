@echo off
chcp 65001 >nul
echo ========================================
echo  Uninstalling Simple LLM Backend Service
echo ========================================

REM 관리자 권한 확인
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [ERROR] This script requires administrator privileges.
    echo Please run as administrator.
    echo.
    echo Right-click on Command Prompt and select "Run as administrator"
    pause
    exit /b 1
)

echo [INFO] Administrator privileges confirmed.
echo.

REM 현재 디렉토리 및 파일 확인
echo Checking installation files...
if not exist "winsw.exe" (
    echo [WARNING] WinSW executable not found in current directory.
    echo Service may not be properly installed or files were moved.
)

if not exist "SimpleLLMBackend.xml" (
    echo [WARNING] Service configuration file not found.
    echo Service may not be properly configured.
)

echo.

REM 서비스 상태 확인
echo Checking service status...
sc query SimpleLLMBackend >nul 2>&1
if %errorLevel% equ 0 (
    echo [INFO] Service found. Proceeding with uninstallation...
    
    REM 서비스 중지
    echo.
    echo Stopping Simple LLM Backend service...
    net stop SimpleLLMBackend
    
    if %errorLevel% equ 0 (
        echo [SUCCESS] Service stopped successfully.
    ) else (
        echo [WARNING] Failed to stop service or service was already stopped.
    )
    
    REM 잠시 대기 (서비스 완전 종료 대기)
    echo Waiting for service to fully stop...
    timeout /t 3 /nobreak >nul
    
    REM 서비스 제거
    echo.
    echo Uninstalling service...
    if exist "winsw.exe" (
        winsw.exe uninstall SimpleLLMBackend.xml
        
        if %errorLevel% equ 0 (
            echo [SUCCESS] Service uninstalled successfully!
        ) else (
            echo [ERROR] Failed to uninstall service.
            echo You may need to remove it manually using:
            echo   sc delete SimpleLLMBackend
        )
    ) else (
        echo [INFO] Using sc command to remove service...
        sc delete SimpleLLMBackend
        
        if %errorLevel% equ 0 (
            echo [SUCCESS] Service removed successfully!
        ) else (
            echo [ERROR] Failed to remove service.
        )
    )
    
) else (
    echo [INFO] Service not found. Nothing to uninstall.
)

echo.

REM 파일 정리 옵션
echo.
echo ========================================
echo  Cleanup Options
echo ========================================
echo.
set /p cleanup="Do you want to remove installation files? (y/N): "

if /i "%cleanup%"=="y" (
    echo.
    echo Cleaning up installation files...
    
    if exist "SimpleLLMBackend.xml" (
        del "SimpleLLMBackend.xml"
        echo [DELETED] SimpleLLMBackend.xml
    )
    
    if exist "winsw.exe" (
        del "winsw.exe"
        echo [DELETED] winsw.exe
    )
    
    if exist "SimpleLLMBackend.out.log" (
        del "SimpleLLMBackend.out.log"
        echo [DELETED] SimpleLLMBackend.out.log
    )
    
    if exist "SimpleLLMBackend.err.log" (
        del "SimpleLLMBackend.err.log"
        echo [DELETED] SimpleLLMBackend.err.log
    )
    
    if exist "SimpleLLMBackend.wrapper.log" (
        del "SimpleLLMBackend.wrapper.log"
        echo [DELETED] SimpleLLMBackend.wrapper.log
    )
    
    echo [SUCCESS] Cleanup completed.
    
) else (
    echo [INFO] Installation files preserved.
    echo You can manually delete them later if needed:
    echo   - SimpleLLMBackend.xml
    echo   - winsw.exe
    echo   - *.log files
)

echo.

REM 로그 파일 정리 옵션
set /p logcleanup="Do you want to remove application log files in logs\ directory? (y/N): "

if /i "%logcleanup%"=="y" (
    if exist "logs" (
        echo.
        echo Cleaning up log files...
        rd /s /q "logs"
        echo [DELETED] logs directory and all contents
    ) else (
        echo [INFO] No logs directory found.
    )
) else (
    echo [INFO] Log files preserved in logs\ directory.
)

echo.
echo ========================================
echo  Uninstallation Summary
echo ========================================
echo.

REM 최종 서비스 상태 확인
sc query SimpleLLMBackend >nul 2>&1
if %errorLevel% equ 0 (
    echo [WARNING] Service still exists. Manual removal may be required.
    echo Try running: sc delete SimpleLLMBackend
) else (
    echo [SUCCESS] Service completely removed from system.
)

echo.
echo Uninstallation process completed.
echo.
echo If you want to reinstall the service later:
echo 1. Copy the JAR file to this directory
echo 2. Run install-service.bat as administrator
echo.
echo Thank you for using Simple LLM Backend!
echo.

pause