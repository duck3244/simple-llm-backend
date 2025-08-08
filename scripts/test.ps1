# Simple LLM Backend API Test (PowerShell)

$Host.UI.RawUI.WindowTitle = "LLM Backend API Test"

Write-Host "=== Simple LLM Backend API Test ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api"

function Test-Api {
    param(
        [string]$Name,
        [string]$Uri,
        [string]$Method = "GET",
        [hashtable]$Body = $null
    )
    
    Write-Host "`n$Name..." -ForegroundColor Yellow
    
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            UseBasicParsing = $true
        }
        
        if ($Body) {
            $params.Body = $Body | ConvertTo-Json
            $params.ContentType = "application/json"
        }
        
        $response = Invoke-WebRequest @params
        
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ Success" -ForegroundColor Green
            
            # JSON 응답 포맷팅
            try {
                $jsonResponse = $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
                Write-Host $jsonResponse -ForegroundColor Cyan
            } catch {
                Write-Host $response.Content -ForegroundColor Cyan
            }
        } else {
            Write-Host "❌ Failed - Status: $($response.StatusCode)" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 1. Health Check
Test-Api -Name "1. Health Check" -Uri "$baseUrl/health"

# 2. Application Info
Test-Api -Name "2. Application Info" -Uri "$baseUrl/info"

# 3. Detailed Health Check
Test-Api -Name "3. Detailed Health Check" -Uri "$baseUrl/health/detailed"

# 4. vLLM Test
Test-Api -Name "4. vLLM Test" -Uri "$baseUrl/generate" -Method "POST" -Body @{
    prompt = "Hello, world!"
    engine = "vllm"
    max_tokens = 50
    temperature = 0.7
}

# 5. SGLang Test
Test-Api -Name "5. SGLang Test" -Uri "$baseUrl/generate" -Method "POST" -Body @{
    prompt = "What is artificial intelligence?"
    engine = "sglang"
    max_tokens = 100
    temperature = 0.5
}

# 6. Stats (if available)
Test-Api -Name "6. Statistics (if Oracle DB configured)" -Uri "$baseUrl/stats"

Write-Host "`n=== Test Completed ===" -ForegroundColor Green
Read-Host "Press Enter to exit"