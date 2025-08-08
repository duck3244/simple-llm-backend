param(
    [string]$Profile = $null,
    [string]$Port = $null,
    [switch]$Help
)

# 도움말 표시
if ($Help) {
    Write-Host @"
Simple LLM Backend 실행 스크립트

사용법:
  .\scripts\run.ps1 [옵션]

옵션:
  -Profile <profile>    Spring 프로필 지정 (dev, prod, test)
  -Port <port>         서버 포트 지정 (기본값: 8080)
  -Help               이 도움말 표시

예시:
  .\scripts\run.ps1                    # 기본 설정으로 실행
  .\scripts\run.ps1 -Profile prod      # 운영 프로필로 실행
  .\scripts\run.ps1 -Port 9090         # 포트 9090으로 실행

환경변수 파일:
  .env 파일을 생성하여 환경변수를 설정할 수 있습니다.
  .env.example 파일을 참조하세요.
"@
    exit 0
}

$Host.UI.RawUI.WindowTitle = "Simple LLM Backend"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Simple LLM Backend Starting..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Java 버전 확인
Write-Host "Checking Java version..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java not found"
    }
    Write-Host "✅ Java is available" -ForegroundColor Green
    # Java 버전 표시 (첫 번째 줄만)
    $javaVersion[0] | Write-Host -ForegroundColor Gray
} catch {
    Write-Host "❌ Java 11이 설치되지 않았거나 PATH에 없습니다." -ForegroundColor Red
    Write-Host "Java 11을 설치하고 JAVA_HOME을 설정해주세요." -ForegroundColor Red
    Write-Host ""
    Write-Host "다운로드 링크: https://adoptium.net/temurin/releases/" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""

# .env 파일 로드 (개선된 파싱)
if (Test-Path ".env") {
    Write-Host "Loading environment variables from .env file..." -ForegroundColor Green
    $envCount = 0
    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        # 주석 라인과 빈 라인 건너뛰기
        if ($line -and -not $line.StartsWith("#")) {
            if ($line -match "^([^=]+)=(.*)$") {
                $key = $matches[1].Trim()
                $value = $matches[2].Trim()
                if ($key -and $value) {
                    [Environment]::SetEnvironmentVariable($key, $value, "Process")
                    Write-Host "  $key = $value" -ForegroundColor Gray
                    $envCount++
                }
            }
        }
    }
    Write-Host "✅ Loaded $envCount environment variables" -ForegroundColor Green
} else {
    Write-Host "⚠️  .env file not found, using default values..." -ForegroundColor Yellow
    Write-Host "   You can create .env file from .env.example template." -ForegroundColor Gray
}

Write-Host ""

# 명령줄 인수로 환경변수 오버라이드
if ($Profile) {
    $env:SPRING_PROFILES_ACTIVE = $Profile
    Write-Host "🔧 Profile overridden: $Profile" -ForegroundColor Magenta
}

if ($Port) {
    $env:SERVER_PORT = $Port
    Write-Host "🔧 Port overridden: $Port" -ForegroundColor Magenta
}

# 기본 환경변수 설정
if (-not $env:VLLM_BASE_URL) { $env:VLLM_BASE_URL = "http://localhost:8000" }
if (-not $env:SGLANG_BASE_URL) { $env:SGLANG_BASE_URL = "http://localhost:30000" }
if (-not $env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE = "dev" }
if (-not $env:SERVER_PORT) { $env:SERVER_PORT = "8080" }
if (-not $env:VLLM_ENABLED) { $env:VLLM_ENABLED = "true" }
if (-not $env:SGLANG_ENABLED) { $env:SGLANG_ENABLED = "true" }

# JVM 옵션 설정
if (-not $env:JAVA_OPTS) {
    $env:JAVA_OPTS = "-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Djava.awt.headless=true"
}

Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  Server Port      : $env:SERVER_PORT" -ForegroundColor Gray
Write-Host "  Spring Profile   : $env:SPRING_PROFILES_ACTIVE" -ForegroundColor Gray
Write-Host "  vLLM Enabled     : $env:VLLM_ENABLED" -ForegroundColor Gray
Write-Host "  vLLM URL         : $env:VLLM_BASE_URL" -ForegroundColor Gray
Write-Host "  SGLang Enabled   : $env:SGLANG_ENABLED" -ForegroundColor Gray
Write-Host "  SGLang URL       : $env:SGLANG_BASE_URL" -ForegroundColor Gray
Write-Host "  Java Options     : $env:JAVA_OPTS" -ForegroundColor Gray
Write-Host ""

# JAR 파일 경로 확인
$jarFile = "build/libs/simple-llm-backend-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "❌ JAR file not found at $jarFile" -ForegroundColor Red
    Write-Host "Please run './gradlew.bat build' first to build the application." -ForegroundColor Red
    Write-Host ""
    Write-Host "Example:" -ForegroundColor Yellow
    Write-Host "  .\gradlew.bat clean build" -ForegroundColor Gray
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

# 로그 디렉토리 생성
if (-not (Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs" | Out-Null
    Write-Host "📁 Created logs directory." -ForegroundColor Green
}

# PID 파일 경로 설정
$pidFile = "logs/simple-llm-backend.pid"

Write-Host "Starting Simple LLM Backend..." -ForegroundColor Green
Write-Host "JAR: $jarFile" -ForegroundColor Gray
Write-Host "PID file: $pidFile" -ForegroundColor Gray
Write-Host ""

# 현재 시간 로깅
$startTime = Get-Date
Write-Host "[$($startTime.ToString('yyyy-MM-dd HH:mm:ss'))] Starting application..." -ForegroundColor Cyan

try {
    # Java 명령 구성
    $javaArgs = @()
    $javaArgs += $env:JAVA_OPTS.Split(' ')
    $javaArgs += "-jar"
    $javaArgs += $jarFile
    $javaArgs += "--server.port=$env:SERVER_PORT"
    $javaArgs += "--spring.profiles.active=$env:SPRING_PROFILES_ACTIVE"

    # 애플리케이션 실행
    & java $javaArgs
    
    $exitCode = $LASTEXITCODE
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    Write-Host ""
    Write-Host "[$($endTime.ToString('yyyy-MM-dd HH:mm:ss'))] Application stopped" -ForegroundColor Cyan
    Write-Host "Exit code: $exitCode" -ForegroundColor Gray
    Write-Host "Runtime: $($duration.ToString('hh\:mm\:ss'))" -ForegroundColor Gray

    if ($exitCode -ne 0) {
        Write-Host ""
        Write-Host "❌ Application failed to start or crashed." -ForegroundColor Red
        Write-Host ""
        Write-Host "🔧 Troubleshooting steps:" -ForegroundColor Yellow
        Write-Host "1. Check logs in the logs\ directory" -ForegroundColor Gray
        Write-Host "2. Verify Java 11 is installed" -ForegroundColor Gray
        Write-Host "3. Ensure port $env:SERVER_PORT is available" -ForegroundColor Gray
        Write-Host "4. Check if LLM servers are running (if enabled)" -ForegroundColor Gray
        Write-Host "5. Verify database configuration (if using Oracle)" -ForegroundColor Gray
        Write-Host ""
        Write-Host "💡 Common solutions:" -ForegroundColor Yellow
        Write-Host "- Check if another instance is already running" -ForegroundColor Gray
        Write-Host "- Verify firewall settings" -ForegroundColor Gray
        Write-Host "- Check available memory (requires at least 2GB)" -ForegroundColor Gray
        Write-Host ""
        Read-Host "Press Enter to exit"
    } else {
        Write-Host "✅ Application stopped normally." -ForegroundColor Green
    }

    exit $exitCode

} catch {
    Write-Host ""
    Write-Host "❌ Failed to start application: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Error details: $_" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}