# Simple LLM Backend 실행 스크립트 (PowerShell)

$Host.UI.RawUI.WindowTitle = "Simple LLM Backend"

# Java 11 확인
Write-Host "Checking Java version..." -ForegroundColor Green
try {
    $javaVersion = java -version 2>&1
    Write-Host $javaVersion[0] -ForegroundColor Yellow
    
    if ($javaVersion -notmatch "11\.") {
        Write-Host "Warning: Java 11이 권장됩니다." -ForegroundColor Yellow
    }
} catch {
    Write-Host "Java가 설치되지 않았거나 PATH에 없습니다." -ForegroundColor Red
    Write-Host "Java 11을 설치하고 JAVA_HOME을 설정해주세요." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# 환경변수 로드
if (Test-Path ".env") {
    Write-Host "Loading environment variables from .env..." -ForegroundColor Green
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^([^=]+)=(.*)$") {
            Set-Item -Path "Env:$($matches[1])" -Value $matches[2]
        }
    }
}

# 기본 환경변수 설정
if (-not $env:VLLM_BASE_URL) { $env:VLLM_BASE_URL = "http://localhost:8000" }
if (-not $env:SGLANG_BASE_URL) { $env:SGLANG_BASE_URL = "http://localhost:30000" }
if (-not $env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE = "dev" }

# JVM 옵션 설정
$javaOpts = @(
    "-Xms512m",
    "-Xmx2g",
    "-XX:+UseG1GC",
    "-XX:MaxGCPauseMillis=200",
    "-Dfile.encoding=UTF-8",
    "-Djava.awt.headless=true"
) -join " "

# 로그 디렉토리 생성
if (-not (Test-Path "logs")) {
    New-Item -ItemType Directory -Name "logs" | Out-Null
}

Write-Host "`nStarting Simple LLM Backend..." -ForegroundColor Green
Write-Host "VLLM URL: $env:VLLM_BASE_URL" -ForegroundColor Cyan
Write-Host "SGLang URL: $env:SGLANG_BASE_URL" -ForegroundColor Cyan
Write-Host "Profile: $env:SPRING_PROFILES_ACTIVE" -ForegroundColor Cyan
Write-Host ""

# 애플리케이션 실행
try {
    $jarFile = Get-ChildItem -Path "build\libs\*.jar" | Select-Object -First 1
    if (-not $jarFile) {
        throw "JAR 파일을 찾을 수 없습니다. gradlew build를 먼저 실행해주세요."
    }
    
    $cmd = "java $javaOpts -jar `"$($jarFile.FullName)`""
    Invoke-Expression $cmd
} catch {
    Write-Host "Application failed to start: $_" -ForegroundColor Red
    Read-Host "Press Enter to exit"
}