# Simple LLM Backend

> **Windows 10 Pro 환경에서 동작하는 간단한 LLM 추론 테스트 백엔드**

[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://openjdk.java.net/projects/jdk/11/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Oracle](https://img.shields.io/badge/Oracle-OJDBC7-red.svg)](https://www.oracle.com/database/)
[![Windows](https://img.shields.io/badge/Windows-10%20Pro-blue.svg)](https://www.microsoft.com/windows)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

vLLM과 SGLang 추론 엔진을 지원하는 간단한 REST API 백엔드입니다. Java 11, Spring Boot 2.3.2, Oracle OJDBC7을 사용하여 Windows 10 Pro 환경에서 안정적으로 동작하도록 최적화되었습니다.

## 📋 목차

- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시작하기](#-시작하기)
- [API 사용법](#-api-사용법)
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

## 🛠 기술 스택

### 핵심 프레임워크
```
Java 11
├── Spring Boot 2.3.2
├── Spring Web (REST API)
├── Spring WebFlux (HTTP Client)
├── Spring Data JPA (ORM)
└── Spring Actuator (모니터링)
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

### 1. 프로젝트 다운로드

#### Git Clone 방식
```batch
git clone https://github.com/your-username/simple-llm-backend.git
cd simple-llm-backend
```

#### 직접 다운로드 방식
1. GitHub에서 ZIP 파일 다운로드
2. 압축 해제 후 폴더로 이동

### 2. Java 11 설치 및 확인

```batch
REM Java 버전 확인
java -version

REM JAVA_HOME 환경변수 확인
echo %JAVA_HOME%
```

Java 11이 설치되지 않았다면:
1. [Adoptium OpenJDK 11](https://adoptium.net/temurin/releases/) 다운로드
2. 설치 후 JAVA_HOME 환경변수 설정
3. PATH에 `%JAVA_HOME%\bin` 추가

### 3. 환경 설정

#### 환경변수 파일 생성
```batch
REM .env 파일 생성
copy .env.example .env
notepad .env
```

#### .env 파일 내용 예시
```bash
# LLM 엔진 설정
VLLM_ENABLED=true
VLLM_BASE_URL=http://localhost:8000
SGLANG_ENABLED=true
SGLANG_BASE_URL=http://localhost:30000

# Oracle 데이터베이스 (선택사항)
DB_HOST=localhost
DB_PORT=1521
DB_SID=XE
DB_USERNAME=llmchat
DB_PASSWORD=your_password

# Spring 프로필
SPRING_PROFILES_ACTIVE=dev
```

### 4. 애플리케이션 실행

#### Command Prompt 방식
```batch
REM 프로젝트 빌드
gradlew.bat build

REM 애플리케이션 실행
scripts\run.bat
```

#### PowerShell 방식 (권장)
```powershell
# 프로젝트 빌드
.\gradlew.bat build

# 애플리케이션 실행
.\scripts\run.ps1
```

#### IDE에서 실행
- **IntelliJ IDEA**: `SimpleApplication.java` → Run
- **Eclipse**: 프로젝트 우클릭 → Run As → Spring Boot App
- **VS Code**: Spring Boot Dashboard 확장 사용

### 5. 실행 확인

브라우저에서 다음 URL로 접속하여 정상 동작 확인:

- **헬스체크**: http://localhost:8080/api/health
- **애플리케이션 정보**: http://localhost:8080/api/info
- **상세 헬스체크**: http://localhost:8080/api/health/detailed

## 📚 API 사용법

### 기본 URL
```
http://localhost:8080/api
```

### 주요 엔드포인트

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | 기본 헬스체크 |
| GET | `/health/detailed` | 상세 헬스체크 |
| GET | `/info` | 애플리케이션 정보 |
| POST | `/generate` | LLM 추론 요청 |
| GET | `/stats` | 통계 정보 (Oracle DB 사용시) |

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

#### PowerShell 방식
```powershell
# vLLM 테스트
$body = @{
    prompt = "Hello, world!"
    engine = "vllm"
    max_tokens = 50
    temperature = 0.7
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/generate" -Method POST -Body $body -ContentType "application/json"
```

### 응답 형식

```json
{
  "text": "안녕하세요! 저는 AI 어시스턴트입니다. 다양한 질문에 답변하고 도움을 드릴 수 있습니다.",
  "engine": "vllm",
  "response_time_ms": 1250,
  "success": true,
  "error": null
}
```

## ⚙️ 환경 설정

### 개발환경 (dev)

```batch
REM 개발 프로필로 실행
set SPRING_PROFILES_ACTIVE=dev
scripts\run.bat
```

개발환경 특징:
- H2 인메모리 데이터베이스 사용
- 상세한 디버그 로깅
- H2 웹 콘솔 활성화 (`/h2-console`)
- 모든 Actuator 엔드포인트 노출

### 운영환경 (prod)

```batch
REM 운영 프로필로 실행
set SPRING_PROFILES_ACTIVE=prod
scripts\run.bat
```

운영환경 특징:
- Oracle 데이터베이스 사용
- 최적화된 로깅 설정
- 보안 강화된 Actuator 설정
- 성능 최적화된 JVM 옵션

### 환경변수 설정

#### Windows 시스템 환경변수
```batch
REM 시스템 전체에 환경변수 설정
setx VLLM_BASE_URL "http://your-vllm-server:8000" /M
setx SGLANG_BASE_URL "http://your-sglang-server:30000" /M
```

#### 배치 파일에서 설정
```batch
REM scripts\run.bat 파일에서 설정
set VLLM_BASE_URL=http://localhost:8000
set SGLANG_BASE_URL=http://localhost:30000
set SPRING_PROFILES_ACTIVE=dev
```

## 🧪 테스트

### 자동 테스트 실행

#### 모든 테스트 실행
```batch
REM Command Prompt
gradlew.bat test

REM PowerShell (상세 리포트 포함)
.\scripts\run-tests.ps1
```

#### 특정 테스트 클래스 실행
```batch
gradlew.bat test --tests "LLMControllerTest"
gradlew.bat test --tests "LLMConfigTest"
```

#### 테스트 커버리지 확인
```batch
REM 커버리지 리포트 생성
gradlew.bat jacocoTestReport

REM 리포트 열기
start build\reports\jacoco\test\html\index.html
```

### 수동 테스트

#### API 테스트 스크립트 실행
```batch
REM 기본 테스트
scripts\test.bat

REM 종합 테스트 (PowerShell)
scripts\comprehensive-test.ps1
```

#### 개별 API 테스트
```batch
REM 헬스체크
curl http://localhost:8080/api/health

REM vLLM 테스트
curl -X POST http://localhost:8080/api/generate -H "Content-Type: application/json" -d "{\"prompt\": \"Hello\", \"engine\": \"vllm\"}"
```

### 테스트 결과 확인

테스트 실행 후 다음 위치에서 결과 확인:
- **테스트 리포트**: `build\reports\tests\test\index.html`
- **커버리지 리포트**: `build\reports\jacoco\test\html\index.html`
- **로그 파일**: `logs\simple-llm-backend-dev.log`

## 🐳 배포

### Windows 서비스로 등록

#### 1. WinSW 다운로드 및 설정
```batch
REM WinSW 설치 스크립트 실행
deployment\install-service.bat
```

#### 2. 서비스 시작/중지
```batch
REM 서비스 시작
net start SimpleLLMBackend

REM 서비스 중지
net stop SimpleLLMBackend

REM 서비스 상태 확인
sc query SimpleLLMBackend
```

### Docker 배포

#### 1. Docker 이미지 빌드
```batch
REM 애플리케이션 빌드
gradlew.bat build

REM Docker 이미지 빌드
docker build -f docker\Dockerfile -t simple-llm-backend:latest .
```

#### 2. Docker Compose로 실행
```batch
REM 전체 스택 시작
docker-compose -f docker\docker-compose.yml up -d

REM 로그 확인
docker-compose -f docker\docker-compose.yml logs -f
```

### 수동 배포

#### 1. JAR 파일 생성
```batch
gradlew.bat bootJar
```

#### 2. 서버에서 실행
```batch
REM JAR 파일 복사 후 실행
java -jar simple-llm-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 📊 모니터링

### Spring Boot Actuator

개발환경에서 모든 모니터링 엔드포인트에 접근 가능:

```
http://localhost:8080/api/actuator
```

주요 엔드포인트:
- **헬스체크**: `/actuator/health`
- **환경정보**: `/actuator/env`
- **메트릭**: `/actuator/metrics`
- **로그 레벨**: `/actuator/loggers`

### 로그 모니터링

#### 실시간 로그 확인
```batch
REM Command Prompt
tail -f logs\simple-llm-backend.log

REM PowerShell
Get-Content logs\simple-llm-backend.log -Wait
```

#### Windows 이벤트 뷰어
서비스로 실행 시 Windows 이벤트 뷰어에서 로그 확인 가능

### 상태 모니터링 스크립트

```batch
REM 지속적인 상태 모니터링
scripts\monitor.bat
```

## 🔧 문제 해결

### 일반적인 문제들

#### 1. Java 버전 문제
```batch
REM 증상: "java is not recognized" 또는 버전 불일치
REM 해결: Java 11 설치 및 JAVA_HOME 설정 확인

java -version
echo %JAVA_HOME%
```

#### 2. 포트 충돌
```batch
REM 증상: "Port 8080 is already in use"
REM 해결: 포트 사용 중인 프로세스 확인 및 종료

netstat -ano | findstr :8080
taskkill /PID [PID번호] /F
```

#### 3. LLM 서버 연결 실패
```batch
REM 증상: "Connection refused" 또는 "Timeout"
REM 해결: LLM 서버 상태 및 URL 확인

curl http://localhost:8000/health    REM vLLM 확인
curl http://localhost:30000/health   REM SGLang 확인
```

#### 4. 메모리 부족
```batch
REM 증상: "OutOfMemoryError"
REM 해결: JVM 힙 메모리 증가

set JAVA_OPTS=-Xmx2g -Xms1g
```

#### 5. 권한 문제
```batch
REM 증상: "Access Denied" 또는 파일 생성 실패
REM 해결: 관리자 권한으로 실행

REM 관리자 권한으로 Command Prompt 실행 후 재시도
```

### 로그 레벨 조정

#### 실시간 로그 레벨 변경
```bash
curl -X POST http://localhost:8080/api/actuator/loggers/com.example.simple \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

### 성능 최적화

#### JVM 튜닝 (scripts\run.bat에서 설정)
```batch
set JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8
```

#### 데이터베이스 연결 풀 조정 (application.yml)
```yaml
spring.datasource.hikari:
  maximum-pool-size: 20
  minimum-idle: 5
```

## 📁 프로젝트 구조

```
simple-llm-backend/
├── .env.example                    # ✅ 환경변수 템플릿
├── .gitignore
├── README.md                       # ✅ 프로젝트 문서
├── build.gradle                    # ✅ Gradle 빌드 설정
├── gradle.properties               # ✅ Gradle 속성
├── gradlew
├── gradlew.bat
├── settings.gradle                 # ✅ Gradle 설정
│
├── src/
│   ├── main/
│   │   ├── java/com/example/simple/
│   │   │   ├── SimpleApplication.java           # ✅ 메인 애플리케이션
│   │   │   ├── config/
│   │   │   │   ├── LLMConfig.java              # ✅ Duration 타입 지원
│   │   │   │   └── WebClientConfig.java        # ✅ WebClient 설정
│   │   │   ├── controller/
│   │   │   │   ├── ApiController.java          # ✅ 통합 API 컨트롤러
│   │   │   │   ├── HealthController.java       # ✅ 헬스체크
│   │   │   │   ├── LLMController.java          # ✅ LLM 컨트롤러
│   │   │   │   └── StatsController.java        # ✅ 통계 컨트롤러
│   │   │   ├── service/
│   │   │   │   ├── LLMService.java             # ✅ LLM 서비스
│   │   │   │   ├── LoggingService.java         # ✅ 로깅 서비스
│   │   │   │   ├── SglangService.java          # ✅ SGLang 서비스
│   │   │   │   └── VllmService.java            # ✅ vLLM 서비스
│   │   │   ├── dto/
│   │   │   │   ├── LLMRequest.java             # ✅ 요청 DTO
│   │   │   │   └── LLMResponse.java            # ✅ 응답 DTO
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java # ✅ 예외 처리
│   │   └── resources/
│   │       ├── application.yml                 # ✅ 수정됨 (context-path 제거)
│   │       ├── application-dev.yml             # ✅ 개발환경 설정
│   │       ├── application-prod.yml            # ✅ 운영환경 설정
│   │       ├── logback-spring.xml              # ✅ 로깅 설정
│   │       └── sql/
│   │           ├── schema.sql                  # ✅ H2 스키마
│   │           └── data.sql                    # ✅ 테스트 데이터
│   └── test/
│       └── java/com/example/simple/
│           ├── SimpleApplicationTests.java     # ✅ 애플리케이션 테스트
│           ├── config/
│           │   └── LLMConfigTest.java          # ✅ 수정됨 (Duration 지원)
│           ├── controller/
│           │   ├── HealthControllerTest.java   # ✅ 헬스체크 테스트
│           │   └── LLMControllerTest.java      # ✅ LLM 컨트롤러 테스트
│           ├── service/
│           │   ├── LLMServiceTest.java         # ✅ LLM 서비스 테스트
│           │   ├── SglangServiceTest.java      # ✅ SGLang 서비스 테스트
│           │   └── VllmServiceTest.java        # ✅ vLLM 서비스 테스트
│           └── integration/
│               └── LLMIntegrationTest.java     # ✅ 통합 테스트
│
├── scripts/                        # ✅ 실행 스크립트
│   ├── run.bat                     # ✅ 수정됨 (환경변수 처리 개선)
│   ├── run.ps1                     # ✅ 수정됨 (매개변수 지원)
│   ├── test.bat                    # ✅ API 테스트
│   ├── test.ps1                    # ✅ PowerShell API 테스트
│   ├── comprehensive-test.ps1      # ✅ 수정됨 (종합 테스트)
│   ├── run-tests.ps1               # ✅ 단위 테스트 실행
│   ├── monitor.bat                 # ✅ 모니터링 스크립트
│   └── quick-start.bat             # ✅ 새로 추가 (빠른 시작)
│
├── docker/                         # ✅ 새로 추가
│   ├── Dockerfile                  # ✅ 멀티스테이지 빌드
│   ├── docker-compose.yml          # ✅ 전체 스택 구성
│   ├── nginx/
│   │   ├── nginx.conf              # ✅ 리버스 프록시 설정
│   │   └── generate-ssl.sh         # ✅ SSL 인증서 생성
│   └── oracle-init/
│       └── 01-init.sql             # ✅ Oracle DB 초기화
│
├── deployment/                     # ✅ 배포 설정
│   ├── install-service.bat         # ✅ Windows 서비스 설치
│   ├── uninstall-service.bat       # ✅ 새로 추가 (서비스 제거)
│   ├── windows-service.xml         # ✅ WinSW 설정
│   └── simple-llm-backend.service  # ✅ Linux 서비스 (참조용)
│
└── logs/                           # ✅ 로그 디렉토리 (자동 생성)
    └── simple-llm-backend.log
```

### 개발 환경 설정

1. 프로젝트 Fork 및 Clone
2. Java 11 및 IDE 설정
3. 개발 브랜치 생성: `git checkout -b feature/new-feature`
4. 개발 및 테스트
5. Pull Request 생성

### 코딩 스타일

- **Java 코딩 컨벤션** 준수
- **단위 테스트** 작성 필수
- **JavaDoc** 주석 작성
- **로깅** 적절한 레벨로 설정

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 지원 및 문의

### 문서 및 리소스
- **API 문서**: http://localhost:8080/api/actuator
- **GitHub Issues**: 버그 리포트 및 기능 요청
- **Wiki**: 상세한 사용법 및 예제

### 문제 해결
1. 먼저 이 README의 [문제 해결](#-문제-해결) 섹션 확인
2. GitHub Issues에서 유사한 문제 검색
3. 새로운 이슈 생성 시 다음 정보 포함:
   - Windows 버전
   - Java 버전
   - 오류 로그
   - 재현 단계

---

**Simple LLM Backend** - Windows 환경에서 간단하고 안정적인 LLM 추론 서비스를 제공합니다.