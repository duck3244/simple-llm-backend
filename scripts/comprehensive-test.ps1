Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Comprehensive API Test Suite" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api"
$testResults = @()

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method = "GET",
        [string]$Url,
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    
    Write-Host "Testing $Name..." -ForegroundColor Yellow
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            TimeoutSec = 10
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "[PASS] $Name" -ForegroundColor Green
        return @{ Name = $Name; Status = "PASS"; Response = $response }
    } catch {
        Write-Host "[FAIL] $Name - $($_.Exception.Message)" -ForegroundColor Red
        return @{ Name = $Name; Status = "FAIL"; Error = $_.Exception.Message }
    }
}

# 테스트 실행
$testResults += Test-Endpoint -Name "Health Check" -Url "$baseUrl/health"
$testResults += Test-Endpoint -Name "Detailed Health" -Url "$baseUrl/health/detailed"
$testResults += Test-Endpoint -Name "Application Info" -Url "$baseUrl/info"

# vLLM 테스트
$vllmBody = @{
    prompt = "Explain machine learning in simple terms"
    engine = "vllm"
    max_tokens = 100
    temperature = 0.7
} | ConvertTo-Json

$testResults += Test-Endpoint -Name "vLLM Generation" -Method "POST" -Url "$baseUrl/generate" -Headers @{"Content-Type" = "application/json"} -Body $vllmBody

# SGLang 테스트
$sglangBody = @{
    prompt = "Write a Python function to calculate fibonacci"
    engine = "sglang"
    max_tokens = 150
    temperature = 0.5
} | ConvertTo-Json

$testResults += Test-Endpoint -Name "SGLang Generation" -Method "POST" -Url "$baseUrl/generate" -Headers @{"Content-Type" = "application/json"} -Body $sglangBody

# 통계 테스트 (Oracle DB 사용시)
$testResults += Test-Endpoint -Name "Statistics" -Url "$baseUrl/stats"

# 결과 요약
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Test Results Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$passCount = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count

foreach ($result in $testResults) {
    $color = if ($result.Status -eq "PASS") { "Green" } else { "Red" }
    Write-Host "  $($result.Status): $($result.Name)" -ForegroundColor $color
}

Write-Host ""
Write-Host "Total Tests: $($testResults.Count)" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red

if ($failCount -eq 0) {
    Write-Host ""
    Write-Host "All tests passed! 🎉" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "Some tests failed. Check the application logs for details." -ForegroundColor Yellow
}

Read-Host "Press Enter to exit"