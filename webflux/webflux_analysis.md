# Simple LLM Backend - WebFlux 사용 현황 분석

## 📍 WebFlux가 적용된 부분들

### 1. **의존성 설정** (`build.gradle`)
```gradle
dependencies {
    // Spring Boot Starters (2.3.2 compatible)
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'  // ← WebFlux 의존성
    // ...
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-webflux'  // ← 테스트용
}
```

### 2. **WebClient 설정** (`WebClientConfig.java`)
```java
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();  // ← WebFlux의 WebClient
    }
}
```

### 3. **VllmService.java** - HTTP 클라이언트로 사용
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class VllmService {
    
    private final WebClient.Builder webClientBuilder;  // ← WebFlux WebClient 주입
    
    public LLMResponse generate(LLMRequest request, long startTime) {
        // ...
        WebClient webClient = webClientBuilder
                .baseUrl(llmConfig.getVllm().getBaseUrl())
                .build();
        
        String response = webClient
                .post()
                .uri("/v1/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)           // ← Reactive Mono 사용
                .timeout(llmConfig.getVllm().getTimeout())  // ← Reactive timeout
                .block();                           // ← 동기화 (Blocking)
        // ...
    }
}
```

### 4. **SglangService.java** - HTTP 클라이언트로 사용
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class SglangService {
    
    private final WebClient.Builder webClientBuilder;  // ← WebFlux WebClient 주입
    
    public LLMResponse generate(LLMRequest request, long startTime) {
        // ...
        WebClient webClient = webClientBuilder
                .baseUrl(llmConfig.getSglang().getBaseUrl())
                .build();
        
        String response = webClient
                .post()
                .uri("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)           // ← Reactive Mono 사용
                .timeout(llmConfig.getSglang().getTimeout())
                .block();                           // ← 동기화 (Blocking)
        // ...
    }
}
```

### 5. **HealthController.java** - 엔진 상태 확인
```java
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired
    private WebClient.Builder webClientBuilder;  // ← WebFlux WebClient 주입
    
    private boolean checkEngineHealth(String baseUrl, String healthPath) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
            
            String response = webClient
                    .get()
                    .uri(healthPath)
                    .retrieve()
                    .bodyToMono(String.class)       // ← Reactive Mono
                    .timeout(Duration.ofSeconds(5)) // ← Reactive timeout
                    .block();                       // ← 동기화 (Blocking)
            
            return response != null;
        } catch (Exception e) {
            // ...
        }
    }
}
```

### 6. **테스트 코드**에서도 사용
```java
// VllmServiceTest.java, SglangServiceTest.java
@Mock
private WebClient.Builder webClientBuilder;

@Mock
private WebClient webClient;

@Mock
private WebClient.RequestBodyUriSpec requestBodyUriSpec;
// ... WebFlux 관련 Mock 객체들
```

## 🔍 WebFlux 사용 패턴 분석

### ✅ **현재 사용 방식: Reactive + Blocking**
```java
// Reactive 체인 구성
String response = webClient
    .post()
    .uri("/generate")
    .bodyValue(requestBody)
    .retrieve()
    .bodyToMono(String.class)    // ← Reactive: Mono<String>
    .timeout(Duration.ofSeconds(30))
    .block();                    // ← Blocking: String 변환
```

### 🎯 **WebFlux가 사용되는 목적**

1. **비동기 HTTP 클라이언트**: 외부 LLM 서버(vLLM, SGLang)와의 통신
2. **타임아웃 처리**: Reactive timeout 기능 활용
3. **논블로킹 I/O**: 내부적으로는 논블로킹 방식으로 네트워크 통신
4. **에러 핸들링**: WebClientException 등 Reactive 예외 처리

### 📊 **WebFlux vs Spring MVC 혼용 구조**

```
┌─────────────────────────────────────┐
│           Frontend/Client           │
└─────────────┬───────────────────────┘
              │ HTTP Request
              ▼
┌─────────────────────────────────────┐
│        Spring MVC Layer            │  ← @RestController (동기)
│  - @RestController                 │
│  - @RequestMapping                 │
│  - Servlet API                     │
└─────────────┬───────────────────────┘
              │ Method Call
              ▼
┌─────────────────────────────────────┐
│         Service Layer              │  ← 동기 처리
│  - LLMService                      │
│  - VllmService, SglangService      │
└─────────────┬───────────────────────┘
              │ WebClient Call
              ▼
┌─────────────────────────────────────┐
│        WebFlux Layer               │  ← Reactive (비동기) + .block()
│  - WebClient                       │
│  - Mono<T>.block()                 │
│  - Reactive Streams                │
└─────────────┬───────────────────────┘
              │ HTTP Request
              ▼
┌─────────────────────────────────────┐
│      External LLM Servers          │
│  - vLLM (http://localhost:8000)    │
│  - SGLang (http://localhost:30000) │
└─────────────────────────────────────┘
```

## 🚨 **현재 구조의 특징**

### ✅ **장점**
1. **간단한 구조**: 기존 Spring MVC 패턴 유지
2. **WebClient 활용**: RestTemplate보다 현대적이고 성능이 좋은 HTTP 클라이언트
3. **타임아웃 지원**: Reactive timeout 기능 활용
4. **논블로킹 I/O**: 내부적으로 Netty 기반 논블로킹 통신

### ⚠️ **제한사항**
1. **블로킹 방식**: `.block()` 사용으로 Reactive의 장점 일부 상실
2. **스레드 점유**: 요청당 하나의 스레드가 블로킹됨
3. **백프레셔 미활용**: Reactive Streams의 백프레셔 기능 사용 안 함

## 💡 **개선 제안**

### Option 1: **완전 Reactive 구조로 변경**
```java
// Controller도 Reactive로 변경
@PostMapping("/generate")
public Mono<LLMResponse> generate(@RequestBody LLMRequest request) {
    return llmService.generateResponseReactive(request);
}

// Service도 Reactive로 변경
public Mono<LLMResponse> generateResponseReactive(LLMRequest request) {
    return webClient
        .post()
        .uri("/generate")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .map(this::parseResponse)          // ← .block() 대신 .map() 사용
        .timeout(Duration.ofSeconds(30))
        .onErrorMap(this::handleError);    // ← Reactive 에러 처리
}
```

### Option 2: **현재 구조 유지 (권장)**
현재 구조는 다음과 같은 이유로 적절합니다:

1. **단순성**: 동기 방식으로 이해하기 쉬운 코드
2. **성능**: LLM 추론은 보통 몇 초 걸리므로 블로킹이 큰 문제가 되지 않음
3. **호환성**: 기존 Spring MVC 패턴과 완벽 호환
4. **WebClient 장점**: 여전히 RestTemplate보다 좋은 성능과 기능

## 📝 **결론**

**WebFlux는 다음 용도로만 제한적으로 사용됨:**
- ✅ **HTTP 클라이언트**: WebClient로 외부 LLM 서버 통신
- ✅ **타임아웃 처리**: Reactive timeout 기능
- ✅ **논블로킹 I/O**: 내부 네트워크 통신 최적화

**완전한 Reactive Programming은 아님:**
- ❌ Controller는 여전히 Spring MVC (@RestController)
- ❌ Service 레이어는 동기 방식
- ❌ `.block()` 사용으로 Reactive Chain 중단

이는 **하이브리드 접근법**으로, WebClient의 장점은 활용하면서 코드의 복잡성은 최소화한 실용적인 구조입니다.