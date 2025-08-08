Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Simple LLM Backend Test Suite" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Gradle 래퍼 확인
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "Error: gradlew.bat not found. Please run from project root." -ForegroundColor Red
    exit 1
}

Write-Host "Running unit tests..." -ForegroundColor Yellow
& .\gradlew.bat test

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Unit tests passed!" -ForegroundColor Green
} else {
    Write-Host "❌ Unit tests failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Running integration tests..." -ForegroundColor Yellow
& .\gradlew.bat integrationTest

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Integration tests passed!" -ForegroundColor Green
} else {
    Write-Host "❌ Integration tests failed!" -ForegroundColor Red
}

Write-Host ""
Write-Host "Generating test reports..." -ForegroundColor Yellow
& .\gradlew.bat jacocoTestReport

Write-Host ""
Write-Host "📊 Test Reports Generated:" -ForegroundColor Cyan
Write-Host "  - Test Results: build\reports\tests\test\index.html" -ForegroundColor Gray
Write-Host "  - Coverage Report: build\reports\jacoco\test\html\index.html" -ForegroundColor Gray

Write-Host ""
Write-Host "Opening test reports..." -ForegroundColor Green
if (Test-Path "build\reports\tests\test\index.html") {
    Start-Process "build\reports\tests\test\index.html"
}

if (Test-Path "build\reports\jacoco\test\html\index.html") {
    Start-Process "build\reports\jacoco\test\html\index.html"
}

Read-Host "Press Enter to exit"