# 프로젝트 디렉토리 생성
New-Item -ItemType Directory -Name "simple-llm-backend"
Set-Location "simple-llm-backend"

# 디렉토리 구조 생성
$directories = @(
    "src\main\java\com\example\simple\config",
    "src\main\java\com\example\simple\controller",
    "src\main\java\com\example\simple\service",
    "src\main\java\com\example\simple\dto",
    "src\main\java\com\example\simple\exception",
    "src\main\resources\sql",
    "src\test\java\com\example\simple\controller",
    "src\test\java\com\example\simple\service",
    "scripts",
    "docker",
    "deployment",
    "logs"
)

foreach ($dir in $directories) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
}

Write-Host "프로젝트 구조가 생성되었습니다!" -ForegroundColor Green