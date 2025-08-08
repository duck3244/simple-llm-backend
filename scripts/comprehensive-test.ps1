param(
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$SkipLLM,
    [switch]$Verbose,
    [switch]$Help
)

if ($Help) {
    Write-Host @"
Simple LLM Backend 종합 테스트 스크립트

사용법:
  .\scripts\comprehensive-test.ps1 [옵션]

옵션:
  -BaseUrl <url>    테스트할 서버 URL (기본값: http://localhost:8080)
  -SkipLLM         LLM 추론 테스트 건너뛰기
  -Verbose         상세한 출력 표시
  -Help            이 도움말 표시

예시:
  .\scripts\comprehensive-test.ps1
  .\scripts\comprehensive-test.ps1 -BaseUrl http://localhost:9090
  .\scripts\comprehensive-test.ps1 -SkipLLM -Verbose
"@
    exit 0
}

$Host.UI.RawUI.WindowTitle = "Simple LLM Backend - Comprehensive Test"

# 테스트 결과 저장용
$script:TestResults = @()
$script:TotalTests = 0
$script:PassedTests = 0
$script:FailedTests = 0

# 색상 출력 함수
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    
    $ColorMap = @{
        "Red" = [ConsoleColor]::Red
        "Green" = [ConsoleColor]::Green
        "Yellow" = [ConsoleColor]::Yellow
        "Cyan" = [ConsoleColor]::Cyan
        "Magenta" = [ConsoleColor]::Magenta
        "Blue" = [ConsoleColor]::Blue
        "Gray" = [ConsoleColor]::Gray
        "White" = [ConsoleColor]::White
    }
    
    Write-Host $Message -ForegroundColor $ColorMap[$Color]
}

# 테스트 실행 함수
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method = "GET",
        [string]$Url,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int]$ExpectedStatus = 200,
        [string]$ExpectedContent = $null,
        [int]$TimeoutSeconds = 30
    )
    
    $script:TotalTests++
    
    if ($Verbose) {
        Write-ColorOutput "Testing: $Name" "Yellow"
        Write-ColorOutput "  URL: $Url" "Gray"
        Write-ColorOutput "  Method: $Method" "Gray"
    } else {
        Write-Host "Testing $Name... " -NoNewline
    }
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = $TimeoutSeconds
            UseBasicParsing = $true
        }
        
        if ($Headers.Count -gt 0) {
            $params.Headers = $Headers
        }
        
        if ($Body) {
            $params.Body = $Body
            if (-not $Headers.ContainsKey("Content-Type")) {
                $params.ContentType = "application/json"
            }
        }
        
        $startTime = Get-Date
        $response = Invoke-WebRequest @params
        $endTime = Get-Date
        $responseTime = ($endTime - $startTime).TotalMilliseconds
        
        $success = $true
        $errorMessage = $null
        
        # 상태 코드 확인
        if ($response.StatusCode -ne $ExpectedStatus) {
            $success = $false
            $errorMessage = "Expected status $ExpectedStatus, got $($response.StatusCode)"
        }
        
        # 응답 내용 확인
        if ($ExpectedContent -and $response.Content -notlike "*$ExpectedContent*") {
            $success = $false
            $errorMessage = "Expected content '$ExpectedContent' not found in response"
        }
        
        # 결과 저장
        $testResult = @{
            Name = $Name
            Status = if ($success) { "PASS" } else { "FAIL" }
            ResponseTime = [math]::Round($responseTime, 2)
            StatusCode = $response.StatusCode
            Error = $errorMessage
            Response = if ($response.Content.Length -lt 500) { $response.Content } else { $response.Content.Substring(0, 500) + "..." }
        }
        
        $script:TestResults += $testResult
        
        if ($success) {
            $script:PassedTests++
            if ($Verbose) {
                Write-ColorOutput "  ✅ PASS (${responseTime}ms)" "Green"
                Write-ColorOutput "  Response: $($response.Content)" "Gray"
            } else {
                Write-ColorOutput "✅ PASS (${responseTime}ms)" "Green"
            }
        } else {
            $script:FailedTests++
            if ($Verbose) {
                Write-ColorOutput "  ❌ FAIL - $errorMessage" "Red"
                Write-ColorOutput "  Response: $($response.Content)" "Gray"
            } else {
                Write-ColorOutput "❌ FAIL - $errorMessage" "Red"
            }
        }
        
        return $testResult
        
    } catch {
        $script:FailedTests++
        $errorMessage = $_.Exception.Message
        
        $testResult = @{
            Name = $Name
            Status = "FAIL"
            ResponseTime = 0
            StatusCode = 0
            Error = $errorMessage
            Response = $null
        }
        
        $script:TestResults += $testResult
        
        if ($Verbose) {
            Write-ColorOutput "  ❌ FAIL - $errorMessage" "Red"
        } else {
            Write-ColorOutput "❌ FAIL - $errorMessage" "Red"
        }
        
        return $testResult
    }
}

# 헤더 출력
Write-ColorOutput "========================================" "Cyan"
Write-ColorOutput " Simple LLM Backend 종합 테스트" "Cyan"
Write-ColorOutput "========================================" "Cyan"
Write-ColorOutput "Base URL: $BaseUrl" "Gray"
Write-ColorOutput "Test Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Gray"
Write-ColorOutput ""

# 서버 연결 확인
Write-ColorOutput "📡 서버 연결 확인..." "Magenta"
$serverCheck = Test-Endpoint -Name "Server Connection" -Url "$BaseUrl/api/health" -TimeoutSeconds 10

if ($serverCheck.Status -eq "FAIL") {
    Write-ColorOutput ""
    Write-ColorOutput "❌ 서버에 연결할 수 없습니다. 다음을 확인하세요:" "Red"
    Write-ColorOutput "  1. 애플리케이션이 실행 중인지 확인" "Gray"
    Write-ColorOutput "  2. 포트 $($BaseUrl.Split(':')[-1])가 열려있는지 확인" "Gray"
    Write-ColorOutput "  3. 방화벽 설정 확인" "Gray"
    Write-ColorOutput ""
    exit 1
}

Write-ColorOutput ""

# 기본 API 테스트
Write-ColorOutput "🔧 기본 API 테스트..." "Magenta"
Test-Endpoint -Name "Health Check" -Url "$BaseUrl/api/health" -ExpectedContent "OK"
Test-Endpoint -Name "Application Info" -Url "$BaseUrl/api/info" -ExpectedContent "Simple LLM Backend"
Test-Endpoint -Name "Detailed Health" -Url "$BaseUrl/api/health/detailed" -ExpectedContent "application"

Write-ColorOutput ""

# Actuator 엔드포인트 테스트
Write-ColorOutput "📊 Actuator 엔드포인트 테스트..." "Magenta"
Test-Endpoint -Name "Actuator Health" -Url "$BaseUrl/actuator/health"
Test-Endpoint -Name "Actuator Info" -Url "$BaseUrl/actuator/info"

Write-ColorOutput ""

# 통계 API 테스트
Write-ColorOutput "📈 통계 API 테스트..." "Magenta"
Test-Endpoint -Name "Statistics API" -Url "$BaseUrl/api/stats"

Write-ColorOutput ""

# LLM 추론 테스트 (옵션)
if (-not $SkipLLM) {
    Write-ColorOutput "🤖 LLM 추론 테스트..." "Magenta"
    
    # vLLM 테스트
    $vllmBody = @{
        prompt = "Hello, world! Please respond briefly."
        engine = "vllm"
        max_tokens = 50
        temperature = 0.7
    } | ConvertTo-Json
    
    Test-Endpoint -Name "vLLM Generation" -Method "POST" -Url "$BaseUrl/api/generate" `
        -Headers @{"Content-Type" = "application/json"} -Body $vllmBody -TimeoutSeconds 60
    
    # SGLang 테스트
    $sglangBody = @{
        prompt = "What is 2+2? Answer briefly."
        engine = "sglang"
        max_tokens = 30
        temperature = 0.5
    } | ConvertTo-Json
    
    Test-Endpoint -Name "SGLang Generation" -Method "POST" -Url "$BaseUrl/api/generate" `
        -Headers @{"Content-Type" = "application/json"} -Body $sglangBody -TimeoutSeconds 60
    
    # 잘못된 요청 테스트
    $invalidBody = @{
        prompt = ""  # 빈 프롬프트
        engine = "vllm"
    } | ConvertTo-Json
    
    Test-Endpoint -Name "Invalid Request Test" -Method "POST" -Url "$BaseUrl/api/generate" `
        -Headers @{"Content-Type" = "application/json"} -Body $invalidBody -ExpectedStatus 400
    
    Write-ColorOutput ""
} else {
    Write-ColorOutput "⏭️  LLM 추론 테스트 건너뜀 (-SkipLLM 옵션)" "Yellow"
    Write-ColorOutput ""
}

# 오류 처리 테스트
Write-ColorOutput "⚠️  오류 처리 테스트..." "Magenta"
Test-Endpoint -Name "404 Error Test" -Url "$BaseUrl/api/nonexistent" -ExpectedStatus 404
Test-Endpoint -Name "Invalid JSON Test" -Method "POST" -Url "$BaseUrl/api/generate" `
    -Headers @{"Content-Type" = "application/json"} -Body "{ invalid json }" -ExpectedStatus 400

Write-ColorOutput ""

# 성능 테스트
Write-ColorOutput "⚡ 성능 테스트..." "Magenta"
$performanceTests = @()
for ($i = 1; $i -le 5; $i++) {
    $result = Test-Endpoint -Name "Performance Test $i" -Url "$BaseUrl/api/health"
    $performanceTests += $result.ResponseTime
}

$avgResponseTime = ($performanceTests | Measure-Object -Average).Average
Write-ColorOutput "Average response time: $([math]::Round($avgResponseTime, 2))ms" "Gray"

Write-ColorOutput ""

# 테스트 결과 요약
Write-ColorOutput "========================================" "Cyan"
Write-ColorOutput " 테스트 결과 요약" "Cyan"
Write-ColorOutput "========================================" "Cyan"

Write-ColorOutput "전체 테스트: $script:TotalTests" "White"
Write-ColorOutput "성공: $script:PassedTests" "Green"
Write-ColorOutput "실패: $script:FailedTests" "Red"

$successRate = if ($script:TotalTests -gt 0) { 
    [math]::Round(($script:PassedTests / $script:TotalTests) * 100, 1) 
} else { 0 }

Write-ColorOutput "성공률: $successRate%" $(if ($successRate -ge 90) { "Green" } elseif ($successRate -ge 70) { "Yellow" } else { "Red" })

Write-ColorOutput ""

# 실패한 테스트 상세 정보
if ($script:FailedTests -gt 0) {
    Write-ColorOutput "❌ 실패한 테스트:" "Red"
    $script:TestResults | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
        Write-ColorOutput "  • $($_.Name): $($_.Error)" "Red"
    }
    Write-ColorOutput ""
}

# 추천 사항
if ($script:FailedTests -gt 0) {
    Write-ColorOutput "🔧 문제 해결 권장사항:" "Yellow"
    Write-ColorOutput "  1. 애플리케이션 로그 확인: logs\simple-llm-backend.log" "Gray"
    Write-ColorOutput "  2. 환경변수 설정 확인: .env 파일" "Gray"
    Write-ColorOutput "  3. LLM 서버 상태 확인 (vLLM, SGLang)" "Gray"
    Write-ColorOutput "  4. 데이터베이스 연결 확인 (Oracle/H2)" "Gray"
    Write-ColorOutput "  5. 포트 충돌 확인: netstat -ano | findstr :8080" "Gray"
    Write-ColorOutput ""
} else {
    Write-ColorOutput "🎉 모든 테스트가 성공했습니다!" "Green"
    Write-ColorOutput ""
}

# 테스트 결과를 파일로 저장
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$reportFile = "logs\test-report-$timestamp.json"

if (-not (Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs" | Out-Null
}

$testReport = @{
    timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    baseUrl = $BaseUrl
    summary = @{
        total = $script:TotalTests
        passed = $script:PassedTests
        failed = $script:FailedTests
        successRate = $successRate
    }
    tests = $script:TestResults
    environment = @{
        powershell = $PSVersionTable.PSVersion.ToString()
        os = [System.Environment]::OSVersion.ToString()
        user = [System.Environment]::UserName
    }
}

$testReport | ConvertTo-Json -Depth 4 | Out-File -FilePath $reportFile -Encoding UTF8
Write-ColorOutput "📝 테스트 결과가 저장되었습니다: $reportFile" "Gray"

Write-ColorOutput ""
Write-ColorOutput "테스트 완료! " "Cyan" -NoNewline
if ($script:FailedTests -eq 0) {
    Write-ColorOutput "🎯 Perfect Score!" "Green"
} else {
    Write-ColorOutput "일부 개선이 필요합니다." "Yellow"
}

# 종료 코드 반환
exit $script:FailedTests