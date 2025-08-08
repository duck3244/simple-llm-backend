# Simple LLM Backend with Token Management

> **Windows 10 Pro 환경에서 동작하는 LLM 추론 + 토큰 계산 통합 백엔드**

[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://openjdk.java.net/projects/jdk/11/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WebFlux](https://img.shields.io/badge/Spring-WebFlux-blue.svg)](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
[![Tiktoken](https://img.shields.io/badge/Tiktoken-0.6.1-red.svg)](https://github.com/knuddelsgmbh/jtokkit)
[![Oracle](https://img.shields.io/badge/Oracle-OJDBC7-red.svg)](https://www.oracle.com/database/)
[![Windows](https://img.shields.io/badge/Windows-10%20Pro-blue.svg)](https://www.microsoft.com/windows)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

vLLM과 SGLang 추론 엔진을 지원하는 LLM 백엔드에 **WebFlux 기반 토큰 계산 및 관리 기능**을 통합한 종합 솔루션입니다. Java 11, Spring Boot 2.3.2, Tiktoken을 사용하여 Windows 10 Pro 환경에서 안정적으로 동작하도록 최적화되었습니다.

## 📋 목차

- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시작하기](#-시작하기)
- [API 사용법](#-api-사용법)
- [토큰 계산 기능](#-토큰-계산-기능)
- [환경 설정](#-환경-설정)
- [테스트](#-테스트)
- [배포](#-배포)
- [문제 해결](#-문제-해결)

## ✨ 주요 기능

### 🤖 LLM 추론 지원
- **vLLM 엔진**: 고성능 LLM 추론 서버 연동
- **SGLang 엔진**: 구조화된 생성 언어 모델 지원
- **엔진 선택**: 요청별로 원하는 엔진 선택 가능
- **페일오버**: 운영환경에서 엔진 장애 시 자동 전환

### 🧮 토큰 계산 및 관리 (NEW!)
- **정확한 토큰 계산**: OpenAI Tiktoken 라이브러리 사용
- **비동기 처리**: WebFlux 기반 Reactive 토큰 계산
- **배치 처리**: 대량 텍스트 병렬 토큰 계산
- **비용 추정**: 모델별 정확한 API 사용 비용 계산
- **토큰 제한**: 사용자별/요청별 토큰 한도 관리
- **외부 API 지원**: Hugging Face, OpenAI 토크나이저 연동
- **캐싱**: 토큰 계산 결과 캐싱으로 성능 최적화

### 💻 Windows 환경 최적화
- **배치 스크립트**: `.bat` 파일로 간편한 실행
- **PowerShell 지원**: 상세한 기능을 위한 PowerShell 스크립트
- **Windows 서비스**: WinSW를 통한 서비스 등록 지원
- **경로 처리**: Windows 파일 시스템 경로 완벽 지원

### 🔍 모니터링 및 관리
- **헬스체크**: 시스템 및 LLM 엔진 상태 확인
- **상세 로깅**: 개발/운영환경별 로그 레벨 설정
- **통계 API**: Oracle DB 사용 시 요청/응답 통계 제공
- **Spring Actuator**: 시스템 메트릭 및 상태 모니터링
- **토큰 통계**: 토큰 사용량 및 비용 분석

## 🛠 기술 스택

### 핵심 프레임워크
```
Java 11
├── Spring Boot 2.3.2
├── Spring Web (REST API)
├── Spring WebFlux (Reactive + HTTP Client)
├── Spring Data JPA (ORM)
├── Spring Actuator (모니터링)
└── Spring Cache (토큰 계산 캐싱)
```

### 토큰 계산 기술
```
Token Management
├── Tiktoken (OpenAI 토크나이저)
├── WebFlux (비동기 처리)
├── Caffeine (캐싱)
├── Reactor (Reactive Streams)
└── External APIs (Hugging Face, OpenAI)
```

### 데이터베이스
```
Oracle 12c (OJDBC7)
├── H2 (개발/테스트용)
├── HikariCP (연결 풀)
└── Hibernate (ORM)
```

### LLM 엔진
```
HTTP 기반 통신
├── vLLM (http://localhost:8000)
├── SGLang (http://localhost:30000)
└── WebClient (비동기 클라이언트)
```

## 🚀 시작하기

### 사전 요구사항

#### 필수 소프트웨어
- **Java 11** 이상 ([OpenJDK 11](https://adoptopenjdk.net/) 권장)
- **Git** ([Git for Windows](https://git-scm.com/download/win))
- **LLM 서버** (vLLM 또는 SGLang 중 하나 이상)

#### 선택사항
- **Oracle 12c** (통계 및 로깅 기능 사용 시)
- **Redis** (캐싱 기능 사용 시)

### 1. 빠른 시작

#### 자동 설치 및 실행
```batch
# 프로젝트 다운로드
git clone https://github.com/your-username/simple-llm-backend.git
cd simple-llm-backend

# 빠른 시작 (모든 설정 자동화)
scripts\quick-start.bat
```

#### 수동 설치
```batch
# 1. 환경설정 파일 생성
copy .env.example .env

# 2. 프로젝트 빌드
gradlew.bat build

# 3. 애플리케이션 실행
scripts\run.bat
```

### 2. 환경 설정

#### .env 파일 설정
```bash
# LLM 엔진 설정
VLLM_ENABLED=true
VLLM_BASE_URL=http://localhost:8000
SGLANG_ENABLED=true
SGLANG_BASE_URL=http://localhost:30000

# 토큰 계산 설정
TOKEN_EXTERNAL_ENABLED=false
HUGGINGFACE_API_TOKEN=your_token_here
OPENAI_API_TOKEN=your_token_here

# 데이터베이스 설정 (선택사항)
DB_HOST=localhost
DB_PORT=1521
DB_SID=XE
DB_USERNAME=llmchat
DB_PASSWORD=your_password

# 서버 설정
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### 3. 실행 확인

브라우저에서 다음 URL로 접속하여 정상 동작 확인:

- **기본 헬스체크**: http://localhost:8080/api/health
- **애플리케이션 정보**: http://localhost:8080/api/info
- **상세 헬스체크**: http://localhost:8080/api/health/detailed
- **토큰 서비스 상태**: http://localhost:8080/api/tokens/health

## 📚 API 사용법

### 기본 LLM API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | 기본 헬스체크 |
| GET | `/api/health/detailed` | 상세 헬스체크 |
| GET | `/api/info` | 애플리케이션 정보 |
| POST | `/api/generate` | LLM 추론 요청 |
| GET | `/api/stats` | 통계 정보 (Oracle DB 사용시) |

### 토큰 계산 API (NEW!)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tokens/calculate` | 단일 텍스트 토큰 계산 |
| POST | `/api/tokens/calculate-batch` | 배치 토큰 계산 |
| POST | `/api/tokens/estimate-request` | LLM 요청 토큰 추정 |
| POST | `/api/tokens/validate` | 토큰 제한 검증 |
| GET | `/api/tokens/health` | 토큰 서비스 상태 |
| GET | `/api/tokens/stats` | 토큰 서비스 통계 |
| GET | `/api/tokens/supported-models` | 지원 모델 목록 |

### LLM 추론 요청

#### vLLM으로 추론
```bash
curl -X POST http://localhost:8080/api/generate ^
  -H "Content-Type: application/json" ^
  -d "{\"prompt\": \"안녕하세요, 자기소개를 해주세요\", \"engine\": \"vllm\", \"max_tokens\": 100, \"temperature\": 0.7}"
```

#### SGLang으로 추론
```bash
curl -X POST http://localhost:8080/api/generate ^
  -H "Content-Type: application/json" ^
  -d "{\"prompt\": \"파이썬으로 피보나치 수열을 구현하는 방법은?\", \"engine\": \"sglang\", \"max_tokens\": 200, \"temperature\": 0.5}"
```

## 🧮 토큰 계산 기능

### 단일 텍스트 토큰 계산

```bash
curl -X POST http://localhost:8080/api/tokens/calculate ^
  -H "Content-Type: application/json" ^
  -d "{\"text\": \"Hello, how many tokens is this?\", \"model\": \"gpt-3.5-turbo\"}"
```

**응답 예시:**
```json
{
  "text": "Hello, how many tokens is this?",
  "model": "gpt-3.5-turbo",
  "inputTokens": 7,
  "outputTokens": 0,
  "totalTokens": 7,
  "estimatedCost": 0.0000105,
  "processingTimeMs": 15,
  "method": "LOCAL_TIKTOKEN",
  "calculatedAt": "2024-01-15T10:30:45",
  "averageCharsPerToken": 4.57,
  "tokenDensity": 0.219
}
```

### 배치 토큰 계산

```bash
curl -X POST http://localhost:8080/api/tokens/calculate-batch ^
  -H "Content-Type: application/json" ^
  -d "{\"texts\": [\"First text\", \"Second longer text\", \"Third text is even longer\"], \"model\": \"gpt-4\", \"parallelism\": 3}"
```

### LLM 요청 토큰 추정

```bash
curl -X POST http://localhost:8080/api/tokens/estimate-request ^
  -H "Content-Type: application/json" ^
  -d "{\"prompt\": \"Write a story about AI\", \"engine\": \"vllm\", \"max_tokens\": 500, \"temperature\": 0.7}"
```

**응답 예시:**
```json
{
  "text": "Write a story about AI",
  "model": "gpt-3.5-turbo",
  "inputTokens": 5,
  "outputTokens": 500,
  "totalTokens": 505,
  "estimatedCost": 0.001007,
  "processingTimeMs": 12,
  "method": "LOCAL_TIKTOKEN"
}
```

### 토큰 제한 검증

```bash
curl -X POST "http://localhost:8080/api/tokens/validate?maxTokens=1000" ^
  -H "Content-Type: application/json" ^
  -d "{\"prompt\": \"Your prompt here\", \"engine\": \"vllm\", \"max_tokens\": 800}"
```

### PowerShell을 사용한 고급 테스트

```powershell
# 토큰 계산 테스트
$body = @{
    text = "이것은 한국어와 English가 섞인 mixed language text입니다."
    model = "gpt-3.5-turbo"
    includeDetailedAnalysis = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/tokens/calculate" -Method POST -Body $body -ContentType "application/json"

# 배치 처리 테스트
$batchBody = @{
    texts = @(
        "Short text",
        "Medium length text with more words",
        "This is a longer text that contains multiple sentences and should have more tokens than the previous examples."
    )
    model = "gpt-4"
    useExternal = $false
    parallelism = 3
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/tokens/calculate-batch" -Method POST -Body $batchBody -ContentType "application/json"
```

## ⚙️ 환경 설정

### 개발환경 (dev)

```batch
# 개발 프로필로 실행
set SPRING_PROFILES_ACTIVE=dev
scripts\run.bat
```

**개발환경 특징:**
- H2 인메모리 데이터베이스 사용
- 로컬 토큰 계산만 활성화
- 상세한 디버그 로깅
- 토큰 계산 캐시 30분
- 외부 API 비활성화
- H2 웹 콘솔 활성화 (`/h2-console`)

### 운영환경 (prod)

```batch
# 운영 프로필로 실행
set SPRING_PROFILES_ACTIVE=prod
scripts\run.bat
```

**운영환경 특징:**
- Oracle 데이터베이스 사용
- 외부 토큰 API 활성화 가능
- 최적화된 로깅 설정
- 토큰 계산 캐시 1시간
- 보안 강화된 Actuator 설정
- 성능 최적화된 JVM 옵션

### 토큰 계산 설정

#### application-dev.yml에서 토큰 설정
```yaml
token-calculation:
  local:
    enabled: true
    parallel-threads: 2
    max-text-length: 50000
    
  external:
    enabled: false
    hugging-face:
      enabled: false
      api-token: ${HUGGINGFACE_API_TOKEN:}
    open-ai:
      enabled: false  
      api-token: ${OPENAI_API_TOKEN:}
      
  cache:
    enabled: true
    expire-after-write: 30m
    maximum-size: 1000
    
  cost:
    model-costs:
      gpt-3.5-turbo:
        input-cost-per1-k: 0.0015
        output-cost-per1-k: 0.002
      gpt-4:
        input-cost-per1-k: 0.03
        output-cost-per1-k: 0.06
        
token-limits:
  max-tokens-per-request: 4096
  max-cost-per-request: 0.50
  max-tokens-per-user-daily: 50000
```

## 🧪 테스트

### 자동 테스트 실행

#### 모든 테스트 실행 (토큰 계산 포함)
```batch
# Command Prompt
gradlew.bat test

# PowerShell (상세 리포트 포함)
.\scripts\run-tests.ps1
```

#### 토큰 계산 전용 테스트
```batch
gradlew.bat test --tests "*Token*"
```

### API 테스트

#### 기본 API 테스트
```batch
# 기본 테스트
scripts\test.bat

# 종합 테스트 (PowerShell)
scripts\comprehensive-test.ps1

# 토큰 계산 포함 종합 테스트
scripts\comprehensive-test.ps1 -IncludeTokenTests
```

#### 토큰 계산 전용 테스트 스크립트

**PowerShell 토큰 테스트 예시:**
```powershell
# scripts\test-tokens.ps1
Write-Host "=== 토큰 계산 기능 테스트 ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"

# 1. 기본 토큰 계산
$tokenTest = @{
    text = "Hello, world! This is a test."
    model = "gpt-3.5-turbo"
} | ConvertTo-Json

$result = Invoke-RestMethod -Uri "$baseUrl/api/tokens/calculate" -Method POST -Body $tokenTest -ContentType "application/json"
Write-Host "토큰 수: $($result.totalTokens), 비용: $($result.estimatedCost)" -ForegroundColor Green

# 2. LLM 요청 토큰 추정
$llmTest = @{
    prompt = "Write a short story"
    engine = "vllm"
    max_tokens = 100
} | ConvertTo-Json

$estimate = Invoke-RestMethod -Uri "$baseUrl/api/tokens/estimate-request" -Method POST -Body $llmTest -ContentType "application/json"
Write-Host "예상 총 토큰: $($estimate.totalTokens), 예상 비용: $($estimate.estimatedCost)" -ForegroundColor Green

# 3. 토큰 제한 검증
$validation = Invoke-RestMethod -Uri "$baseUrl/api/tokens/validate?maxTokens=1000" -Method POST -Body $llmTest -ContentType "application/json"
Write-Host "토큰 제한 검증: $($validation.valid)" -ForegroundColor $(if($validation.valid) { "Green" } else { "Red" })

# 4. 서비스 상태
$health = Invoke-RestMethod -Uri "$baseUrl/api/tokens/health" -Method GET
Write-Host "토큰 서비스 상태: $($health.overallHealthy)" -ForegroundColor $(if($health.overallHealthy) { "Green" } else { "Red" })
```

### 성능 테스트

#### 토큰 계산 성능 테스트
```powershell
# 대량 텍스트 토큰 계산 성능 테스트
$largeTexts = @()
for ($i = 1; $i -le 100; $i++) {
    $largeTexts += "This is test text number $i with some additional content to make it longer."
}

$batchTest = @{
    texts = $largeTexts
    model = "gpt-3.5-turbo"
    parallelism = 5
} | ConvertTo-Json

Measure-Command {
    Invoke-RestMethod -Uri "http://localhost:8080/api/tokens/calculate-batch" -Method POST -Body $batchTest -ContentType "application/json"
}
```

## 🐳 배포

### Windows 서비스로 등록

#### 1. 서비스 설치
```batch
# 관리자 권한으로 실행
deployment\install-service.bat
```

#### 2. 서비스 관리
```batch
# 서비스 시작
net start SimpleLLMBackend

# 서비스 중지
net stop SimpleLLMBackend

# 서비스 상태 확인
sc query SimpleLLMBackend

# 서비스 제거
deployment\uninstall-service.bat
```

### Docker 배포

#### 1. Docker 이미지 빌드
```batch
# 애플리케이션 빌드
gradlew.bat build

# Docker 이미지 빌드
docker build -f docker\Dockerfile -t simple-llm-backend:latest .
```

#### 2. Docker Compose로 전체 스택 실행
```batch
# 전체 스택 시작 (Oracle DB + Nginx + 애플리케이션)
docker-compose -f docker\docker-compose.yml up -d

# 로그 확인
docker-compose -f docker\docker-compose.yml logs -f simple-llm-backend

# 토큰 계산 테스트
curl http://localhost/api/tokens/health
```

### 클라우드 배포

#### AWS ECS/Fargate 배포 예시
```yaml
# ecs-task-definition.json
{
  "family": "simple-llm-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "simple-llm-backend",
      "image": "your-registry/simple-llm-backend:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"},
        {"name": "TOKEN_EXTERNAL_ENABLED", "value": "true"},
        {"name": "HUGGINGFACE_API_TOKEN", "value": "your-token"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/simple-llm-backend",
          "awslogs-region": "us-west-2",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

## 📊 모니터링

### Spring Boot Actuator

기본 모니터링 엔드포인트:
- **헬스체크**: `/actuator/health`
- **환경정보**: `/actuator/env`  
- **메트릭**: `/actuator/metrics`
- **캐시 정보**: `/actuator/caches`
- **로그 레벨**: `/actuator/loggers`

### 토큰 계산 모니터링

#### 토큰 서비스 통계
```bash
curl http://localhost:8080/api/tokens/stats
```

**응답 예시:**
```json
{
  "localStats": {
    "totalCalculations": 1543,
    "cacheHits": 892,
    "cacheMisses": 651,
    "averageProcessingTime": 12.5,
    "serviceType": "Local Tiktoken"
  },
  "externalStats": null,
  "totalCalculations": 1543
}
```

#### 캐시 모니터링
```bash
curl http://localhost:8080/actuator/caches
```

#### 메트릭 수집 (Micrometer)
```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: "health,info,metrics,caches"
  metrics:
    export:
      prometheus:
        enabled: true
        step: 1m
    tags:
      application: simple-llm-backend
      environment: production
```

### 실시간 로그 모니터링

#### Windows에서 실시간 로그 확인
```batch
# Command Prompt
tail -f logs\simple-llm-backend.log

# PowerShell
Get-Content logs\simple-llm-backend.log -Wait -Tail 50
```

#### 토큰 계산 관련 로그 필터링
```powershell
# 토큰 계산 로그만 표시
Get-Content logs\simple-llm-backend.log -Wait | Where-Object { $_ -match "Token" }

# 오류 로그만 표시  
Get-Content logs\simple-llm-backend.log -Wait | Where-Object { $_ -match "ERROR" }
```

## 🔧 문제 해결

### 일반적인 문제들

#### 1. Java 버전 문제
```batch
# 증상: "java is not recognized" 또는 버전 불일치
# 해결: Java 11 설치 및 JAVA_HOME 설정 확인

java -version
echo %JAVA_HOME%

# Java 11 다운로드: https://adoptium.net/temurin/releases/
```

#### 2. 토큰 계산 초기화 실패
```batch
# 증상: "Token calculation service initialization failed"
# 해결: Tiktoken 라이브러리 의존성 확인

# 1. 의존성 재설치
gradlew.bat clean build --refresh-dependencies

# 2. 캐시 클리어
gradlew.bat clean
del /Q /S .gradle\caches\*

# 3. 로그에서 상세 오류 확인
type logs\simple-llm-backend-dev.log | findstr "Token"
```

#### 3. 외부 토큰 API 연결 실패
```batch
# 증상: "External API call failed" 또는 "Timeout"
# 해결: API 토큰 및 네트워크 설정 확인

# 1. API 토큰 확인
echo %HUGGINGFACE_API_TOKEN%
echo %OPENAI_API_TOKEN%

# 2. 네트워크 연결 테스트
curl -H "Authorization: Bearer %HUGGINGFACE_API_TOKEN%" https://api-inference.huggingface.co/

# 3. 로컬 전용 모드로 전환
set TOKEN_EXTERNAL_ENABLED=false
```

#### 4. 캐시 관련 문제
```batch
# 증상: 토큰 계산 결과가 캐시되지 않거나 오래된 결과 반환
# 해결: 캐시 설정 확인 및 클리어

# 1. 캐시 상태 확인
curl http://localhost:8080/actuator/caches

# 2. 캐시 클리어 (애플리케이션 재시작)
scripts\run.bat

# 3. 캐시 비활성화 테스트
# application-dev.yml에서 token-calculation.cache.enabled: false
```

#### 5. 메모리 부족 (토큰 계산 시)
```batch
# 증상: "OutOfMemoryError" 또는 느린 처리 속도
# 해결: JVM 힙 메모리 증가 및 설정 최적화

# 1. JVM 메모리 증가
set JAVA_OPTS=-Xmx4g -Xms2g

# 2. 토큰 계산 최적화 설정
# application-dev.yml에서:
# token-calculation.local.parallel-threads: 2
# token-calculation.local.max-text-length: 10000

# 3. 배치 크기 제한
# 한 번에 처리할 텍스트 수를 줄임
```

#### 6. 포트 충돌
```batch
# 증상: "Port 8080 is already in use"
# 해결: 포트 사용 중인 프로세스 확인 및 종료

netstat -ano | findstr :8080
taskkill /PID [PID번호] /F

# 또는 다른 포트 사용
set SERVER_PORT=9090
scripts\run.bat
```

### 토큰 계산 특화 문제 해결

#### 토큰 수 불일치 문제
```bash
# 문제: 계산된 토큰 수가 예상과 다름
# 해결: 모델별 토크나이저 확인

# 1. 지원 모델 확인
curl http://localhost:8080/api/tokens/supported-models

# 2. 다른 모델로 테스트
curl -X POST http://localhost:8080/api/tokens/calculate \
  -H "Content-Type: application/json" \
  -d '{"text": "test", "model": "gpt-4"}'

# 3. 외부 API와 비교 (API 토큰 있는 경우)
curl -X POST http://localhost:8080/api/tokens/calculate \
  -H "Content-Type: application/json" \
  -d '{"text": "test", "model": "gpt-3.5-turbo", "useExternal": true}'
```

#### 성능 최적화

```yaml
# application-prod.yml 최적화 설정
token-calculation:
  local:
    parallel-threads: 8  # CPU 코어 수에 맞게 조정
    max-text-length: 100000
    
  cache:
    enabled: true
    expire-after-write: 1h  # 운영환경에서는 긴 캐시 시간
    maximum-size: 50000
    
  external:
    concurrency-limit: 10  # 외부 API 동시 요청 수 증가
    timeout: 30s
```

### 로그 레벨 조정

#### 실시간 로그 레벨 변경
```bash
# 토큰 계산 디버그 로깅 활성화
curl -X POST http://localhost:8080/actuator/loggers/com.example.simple.service.LocalTokenCalculationService \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# 전체 애플리케이션 로그 레벨 변경
curl -X POST http://localhost:8080/actuator/loggers/com.example.simple \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "INFO"}'
```

## 📁 프로젝트 구조

```
simple-llm-backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/simple/
│   │   │   ├── SimpleApplication.java
│   │   │   ├── config/
│   │   │   │   ├── LLMConfig.java
│   │   │   │   ├── TokenCalculationConfig.java       # NEW!
│   │   │   │   └── WebClientConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── ApiController.java
│   │   │   │   ├── TokenController.java               # NEW!
│   │   │   │   └── StatsController.java
│   │   │   ├── service/
│   │   │   │   ├── LLMService.java
│   │   │   │   ├── EnhancedLLMService.java            # NEW!
│   │   │   │   ├── TokenCalculationService.java      # NEW!
│   │   │   │   ├── LocalTokenCalculationService.java # NEW!
│   │   │   │   ├── ExternalTokenCalculationService.java # NEW!
│   │   │   │   ├── IntegratedTokenCalculationService.java # NEW!
│   │   │   │   ├── VllmService.java
│   │   │   │   ├── SglangService.java
│   │   │   │   └── LoggingService.java
│   │   │   ├── dto/
│   │   │   │   ├── LLMRequest.java
│   │   │   │   ├── LLMResponse.java
│   │   │   │   ├── TokenInfo.java                     # NEW!
│   │   │   │   ├── TokenCalculationRequest.java      # NEW!
│   │   │   │   └── LLMResponseWithTokens.java         # NEW!
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       └── TokenLimitExceededException.java   # NEW!
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml                    # Enhanced!
│   │       ├── application-prod.yml
│   │       └── logback-spring.xml
│   └── test/
│       └── java/com/example/simple/
│           ├── SimpleApplicationTests.java
│           ├── service/
│           │   ├── LocalTokenCalculationServiceTest.java # NEW!
│           │   └── IntegratedTokenCalculationServiceTest.java # NEW!
│           └── controller/
│               └── TokenControllerTest.java           # NEW!
├── scripts/
│   ├── run.bat
│   ├── run.ps1
│   ├── test.bat
│   ├── test-tokens.ps1                                # NEW!
│   ├── comprehensive-test.ps1                         # Enhanced!
│   ├── quick-start.bat
│   └── monitor.bat
├── docker/
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── nginx/
├── deployment/
│   ├── install-service.bat
│   └── uninstall-service.bat
├── .env.example                                       # Enhanced!
├── build.gradle                                       # Enhanced!
└── README.md                                          # This file!
```

## 🔄 업데이트 가이드

### v1.0에서 v2.0으로 업그레이드 (토큰 계산 기능 추가)

#### 1. 의존성 업데이트
```batch
# 1. 새로운 의존성이 포함된 build.gradle 적용
gradlew.bat clean build --refresh-dependencies

# 2. 캐시 설정 확인
# application-dev.yml에서 spring.cache 설정 추가됨
```

#### 2. 환경설정 업데이트
```bash
# .env 파일에 토큰 계산 관련 설정 추가
echo TOKEN_EXTERNAL_ENABLED=false >> .env
echo HUGGINGFACE_API_TOKEN= >> .env
echo OPENAI_API_TOKEN= >> .env
```

#### 3. 데이터베이스 마이그레이션 (선택사항)
```sql
-- 토큰 사용량 추적 테이블 추가 (향후 기능용)
CREATE TABLE TOKEN_USAGE_LOG (
    ID NUMBER(19) PRIMARY KEY,
    USER_ID VARCHAR2(100),
    REQUEST_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    MODEL VARCHAR2(50),
    INPUT_TOKENS NUMBER(10),
    OUTPUT_TOKENS NUMBER(10),
    TOTAL_TOKENS NUMBER(10),
    ESTIMATED_COST NUMBER(10,6),
    METHOD VARCHAR2(50)
);
```

#### 4. 기존 API 호환성
기존 API는 모두 그대로 동작하며, 새로운 토큰 계산 API가 추가되었습니다:
- 기존: `/api/generate` → 변경 없음
- 새로운: `/api/tokens/*` → 새로 추가

## 📖 고급 사용법

### WebFlux Reactive Programming 활용

#### Reactive 체인을 이용한 토큰 검증 + LLM 호출
```java
// Java 코드 예시 (참고용)
public Mono<LLMResponseWithTokens> generateWithValidation(LLMRequest request, int maxTokens) {
    return tokenService.validateTokenLimits(request, maxTokens)
        .filter(ValidationResult::isValid)
        .switchIfEmpty(Mono.error(new TokenLimitExceededException("Token limit exceeded")))
        .flatMap(validation -> llmService.generateResponseWithTokens(request))
        .doOnSuccess(response -> log.info("Generation completed: {} tokens, ${}", 
            response.getTokenUsage().getTotalTokens(), 
            response.getTokenUsage().getTotalCost()));
}
```

#### 스트리밍 배치 처리
```java
// 대용량 텍스트 스트림 처리 예시
public Flux<TokenInfo> processLargeTextStream(Flux<String> textStream, String model) {
    return textStream
        .buffer(100)  // 100개씩 배치
        .flatMap(batch -> tokenService.calculateTokensBatch(batch, model), 3)  // 3개 배치 동시 처리
        .onBackpressureBuffer(1000)  // 백프레셔 처리
        .doOnNext(tokenInfo -> log.debug("Processed: {} tokens", tokenInfo.getTotalTokens()));
}
```

### 커스텀 토크나이저 추가

#### 새로운 토크나이저 구현
```java
@Service
public class CustomTokenCalculationService implements TokenCalculationService {
    
    @Override
    public Mono<TokenInfo> calculateTokensReactive(String text, String model) {
        // 커스텀 토크나이저 로직 구현
        return Mono.fromCallable(() -> {
            int tokens = customTokenize(text, model);
            return TokenInfo.builder()
                .text(text)
                .model(model)
                .inputTokens(tokens)
                .totalTokens(tokens)
                .method(TokenInfo.TokenizationMethod.CUSTOM)
                .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private int customTokenize(String text, String model) {
        // 여기에 커스텀 토크나이저 로직 구현
        return text.split("\\s+").length; // 간단한 단어 기반 토크나이저 예시
    }
}
```

## 🔮 향후 계획

### 개발 로드맵

#### v2.1 (계획)
- [ ] **사용자별 토큰 할당량 관리**
  - 일일/월별 토큰 한도 설정
  - 사용량 대시보드
  - 알림 시스템

#### v2.2 (계획)  
- [ ] **더 많은 모델 지원**
  - Claude 3.5 Sonnet/Haiku
  - Llama 2/3 토크나이저
  - Gemini Pro 토크나이저
  - 커스텀 모델 토크나이저

#### v2.3 (계획)
- [ ] **고급 분석 기능**
  - 토큰 사용 패턴 분석
  - 비용 최적화 제안
  - 성능 벤치마킹
  - 사용량 예측

#### v3.0 (계획)
- [ ] **AI 기반 토큰 최적화**
  - 프롬프트 압축
  - 자동 토큰 절약 제안
  - 스마트 캐싱
  - 적응형 토큰 제한

### 기여 방법

#### 새로운 토크나이저 추가
1. `TokenCalculationService` 인터페이스 구현
2. 테스트 코드 작성
3. 문서 업데이트
4. Pull Request 제출

#### 버그 리포트
GitHub Issues에 다음 정보와 함께 제출:
- Windows 버전
- Java 버전  
- 오류 로그
- 재현 단계
- 토큰 계산 관련 설정

## 📊 성능 벤치마크

### 토큰 계산 성능

| 텍스트 길이 | 로컬 계산 시간 | 외부 API 시간 | 캐시 히트 시간 |
|-------------|----------------|----------------|----------------|
| 100자       | ~5ms          | ~150ms         | ~1ms           |
| 1,000자     | ~12ms         | ~200ms         | ~1ms           |
| 10,000자    | ~45ms         | ~500ms         | ~1ms           |
| 100,000자   | ~200ms        | ~2s            | ~1ms           |

### 배치 처리 성능

| 텍스트 수 | 순차 처리 | 병렬 처리 (4 threads) | 성능 향상 |
|-----------|-----------|------------------------|-----------|
| 10개      | ~50ms     | ~20ms                  | 2.5x      |
| 100개     | ~500ms    | ~150ms                 | 3.3x      |
| 1,000개   | ~5s       | ~1.2s                  | 4.2x      |

### 메모리 사용량

| 기능           | 최소 메모리 | 권장 메모리 | 최적 메모리 |
|----------------|-------------|-------------|-------------|
| 기본 LLM 기능  | 256MB       | 512MB       | 1GB         |
| 토큰 계산 추가 | 512MB       | 1GB         | 2GB         |
| 대량 배치 처리 | 1GB         | 2GB         | 4GB         |

## 🛡️ 보안 고려사항

### API 키 관리
```bash
# 환경변수로 API 키 관리 (권장)
set HUGGINGFACE_API_TOKEN=hf_your_token_here
set OPENAI_API_TOKEN=sk-your_token_here

# .env 파일에 저장 (개발환경만)
echo HUGGINGFACE_API_TOKEN=hf_your_token_here >> .env

# 운영환경에서는 Azure Key Vault, AWS Secrets Manager 등 사용 권장
```

### 토큰 데이터 보안
- 프롬프트 내용은 로그에 일부만 기록 (100자 제한)
- 토큰 계산 결과는 캐시되지만 원본 텍스트는 저장하지 않음
- HTTPS 통신 강제 (운영환경)
- API 요청 제한 (Rate Limiting)

### 데이터 프라이버시
```yaml
# application-prod.yml
logging:
  level:
    com.example.simple.service: WARN  # 개인정보 로깅 최소화
    
token-calculation:
  local:
    log-full-text: false  # 전체 텍스트 로깅 비활성화
  cache:
    store-original-text: false  # 원본 텍스트 캐시 저장 안 함
```

## 🤝 커뮤니티

### 지원 채널
- **GitHub Issues**: 버그 리포트 및 기능 요청
- **GitHub Discussions**: 질문 및 아이디어 공유
- **Wiki**: 상세한 사용법 및 예제

### 기여자 가이드
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Update documentation
6. Submit a pull request

### 라이선스
이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 🚀 빠른 시작 요약

```batch
# 1. 프로젝트 클론
git clone https://github.com/your-username/simple-llm-backend.git
cd simple-llm-backend

# 2. 빠른 설정 및 실행
scripts\quick-start.bat

# 3. API 테스트
curl http://localhost:8080/api/health
curl http://localhost:8080/api/tokens/health

# 4. 토큰 계산 테스트
curl -X POST http://localhost:8080/api/tokens/calculate ^
  -H "Content-Type: application/json" ^
  -d "{\"text\": \"Hello, world!\", \"model\": \"gpt-3.5-turbo\"}"

# 5. LLM 추론 테스트
curl -X POST http://localhost:8080/api/generate ^
  -H "Content-Type: application/json" ^
  -d "{\"prompt\": \"Hello!\", \"engine\": \"vllm\", \"max_tokens\": 50}"
```

## 📞 지원 및 문의

### 문서 및 리소스
- **API 문서**: http://localhost:8080/actuator
- **토큰 API 문서**: http://localhost:8080/api/tokens/supported-models
- **GitHub Repository**: https://github.com/your-username/simple-llm-backend
- **GitHub Issues**: 버그 리포트 및 기능 요청
- **Wiki**: 상세한 사용법 및 예제

### 자주 묻는 질문 (FAQ)

#### Q: 토큰 계산이 정확한가요?
A: OpenAI의 공식 Tiktoken 라이브러리를 사용하므로 GPT 모델의 토큰 계산은 매우 정확합니다. 다른 모델의 경우 유사한 토크나이저를 사용하여 근사치를 제공합니다.

#### Q: 외부 API 없이도 사용할 수 있나요?
A: 네, 로컬 Tiktoken 라이브러리만으로도 모든 기능을 사용할 수 있습니다. 외부 API는 선택사항입니다.

#### Q: 대용량 텍스트 처리가 가능한가요?
A: WebFlux 기반 스트리밍 처리와 배치 처리를 지원하므로 대용량 텍스트도 효율적으로 처리할 수 있습니다.

#### Q: 캐시는 어떻게 관리되나요?
A: Caffeine 캐시를 사용하며, 개발환경에서는 30분, 운영환경에서는 1시간 캐시됩니다. `/actuator/caches`에서 상태를 확인할 수 있습니다.

---

**Simple LLM Backend with Token Management** - Windows 환경에서 LLM 추론과 정확한 토큰 계산을 제공하는 종합 솔루션입니다.

🌟 **Star this repo** if you find it useful!

📧 **Contact**: your-email@example.com