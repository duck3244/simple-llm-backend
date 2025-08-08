@echo off
chcp 65001 >nul
title Simple LLM Backend - Quick Start

echo ========================================
echo  Simple LLM Backend Quick Start
echo ========================================
echo.

REM 관리자 권한 확인
net session >nul 2>&1
set IS_ADMIN=%errorlevel%

if %IS_ADMIN% equ 0 (
    echo [INFO] Running with administrator privileges
) else (
    echo [INFO] Running with standard user privileges
)

echo.

REM Java 설치 확인
echo [1/6] Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java 11 is not installed or not in PATH
    echo.
    echo Please install Java 11:
    echo 1. Download from: https://adoptium.net/temurin/releases/
    echo 2. Install and ensure JAVA_HOME is set
    echo 3. Add %%JAVA_HOME%%\bin to PATH
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Java is installed
)

REM .env 파일 확인 및 생성
echo.
echo [2/6] Checking environment configuration...
if not exist ".env" (
    if exist ".env.example" (
        echo [INFO] Creating .env file from .env.example...
        copy ".env.example" ".env" >nul
        echo [OK] .env file created
        echo [INFO] Please review and modify .env file