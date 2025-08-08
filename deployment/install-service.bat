@echo off
echo Installing Simple LLM Backend as Windows Service...

REM WinSW 다운로드 확인
if not exist "winsw.exe" (
    echo Downloading WinSW...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/winsw/winsw/releases/latest/download/WinSW-x64.exe' -OutFile 'winsw.exe'"
)

REM 서비스 설치
copy windows-service.xml SimpleLLMBackend.xml
winsw.exe install SimpleLLMBackend.xml

if %ERRORLEVEL% equ 0 (
    echo Service installed successfully!
    echo Starting service...
    net start SimpleLLMBackend
) else (
    echo Failed to install service.
)

pause