Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Simple LLM Backend Starting..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# .env 파일 로드
if (Test-Path ".env") {
    Write-Host "Loading environment variables from .env file..." -ForegroundColor Green
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^([^#][^=]+)=(.*)$") {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            if ($key -and $value) {
                [Environment]::SetEnvironmentVariable($key, $value, "Process")
                Write-Host "  $key = $value" -ForegroundColor Gray
            }
        }
    }
} else {
    Write-Host ".env file not found, using default values..." -ForegroundColor Yellow
}

# Java 옵션 설정
$env:JAVA_OPTS = "-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Djava.awt.headless=true"

# Spring 프로필 기본값 설정
if (-not $env:SPRING_PROFILES_ACTIVE) {
    $env:SPRING_PROFILES_ACTIVE = "dev"
}

Write-Host ""
Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  Java Options: $env:JAVA_OPTS" -ForegroundColor Gray
Write-Host "  Spring Profile: $env:SPRING_PROFILES_ACTIVE" -ForegroundColor Gray
Write-Host "  vLLM URL: $env:VLLM_BASE_URL" -ForegroundColor Gray
Write-Host "  SGLang URL: $env:SGLANG_BASE_URL" -ForegroundColor Gray
Write-Host ""

# JAR 파일 경로 확인
$jarFile = "build/libs/simple-llm-backend-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "Error: JAR file not found at $jarFile" -ForegroundColor Red
    Write-Host "Please run './gradlew.bat build' first" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# 로그 디렉토리 생성
if (-not (Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs" | Out-Null
}

Write-Host "Starting application..." -ForegroundColor Green
Write-Host ""

try {
    java $env:JAVA_OPTS.Split(' ') -jar $jarFile "--spring.profiles.active=$env:SPRING_PROFILES_ACTIVE"
} catch {
    Write-Host ""
    Write-Host "Application failed to start. Check logs for details." -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Read-Host "Press Enter to exit"
}