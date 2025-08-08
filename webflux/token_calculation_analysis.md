# WebFlux 기반 토큰량 계산 기능 추가 방안

## 🎯 토큰량 계산이 필요한 이유

1. **비용 관리**: LLM API 사용량 기반 과금
2. **사용량 제한**: 사용자별/세션별 토큰 한도 관리
3. **성능 최적화**: 토큰 수에 따른 응답 시간 예측
4. **통계 수집**: 사용 패턴 분석 및 리포트

## 🔧 구현 가능한 방법들

### Method 1: **OpenAI Tiktoken 라이브러리 사용** (추천)
```java
// 의존성 추가 (build.gradle)
implementation 'com.knuddels:jtokkit:0.6.1'
```

### Method 2: **외부 토크나이저 API 호출** (WebFlux 활용)
- Hugging Face Tokenizer API
- OpenAI Tokenizer API
- 자체 토크나이저 서버

### Method 3: **로컬 토크나이저 구현**
- BPE (Byte-Pair Encoding) 구현
- SentencePiece 통합

## 🚀 WebFlux를 활용한 구현 방안

### 방안 1: **외부 토크나이저 API와 WebFlux 연동**

#### 장점
- ✅ 다양한 모델의 토크나이저 지원
- ✅ 최신 토크나이저 자동 업데이트
- ✅ WebFlux의 비동기 처리 활용
- ✅ 여러 요청 병렬 처리 가능

#### 단점
- ❌ 네트워크 의존성
- ❌ 외부 서비스 장애 위험
- ❌ 지연 시간 증가

### 방안 2: **로컬 토크나이저 + WebFlux 스트리밍**

#### 장점
- ✅ 네트워크 독립적
- ✅ 빠른 처리 속도
- ✅ 배치 처리 최적화
- ✅ WebFlux 스트리밍 활용

#### 단점
- ❌ 모델별 토크나이저 관리 복잡
- ❌ 라이브러리 의존성

## 💻 구현 예시

### 1. 토큰 계산 서비스 인터페이스
```java
public interface TokenCalculationService {
    
    // 동기 방식 (기존 패턴 유지)
    TokenInfo calculateTokens(String text, String model);
    
    // Reactive 방식 (WebFlux 활용)
    Mono<TokenInfo> calculateTokensReactive(String text, String model);
    
    // 배치 처리 (WebFlux 스트리밍)
    Flux<TokenInfo> calculateTokensBatch(List<String> texts, String model);
    
    // 스트리밍 처리
    Flux<TokenInfo> calculateTokensStream(Flux<String> textStream, String model);
}
```

### 2. 토큰 정보 DTO
```java
@Data
@Builder
public class TokenInfo {
    private String text;
    private String model;
    private int inputTokens;
    private int outputTokens;
    private int totalTokens;
    private double estimatedCost;
    private long processingTimeMs;
    private TokenizationMethod method;
    
    public enum TokenizationMethod {
        LOCAL_TIKTOKEN,
        HUGGINGFACE_API,
        OPENAI_API,
        SENTENCEPIECE
    }
}
```

### 3. 로컬 토크나이저 구현 (Tiktoken)
```java
@Service
@Slf4j
public class LocalTokenCalculationService implements TokenCalculationService {
    
    private final Map<String, Encoding> encodingCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initializeEncodings() {
        // 주요 모델별 인코딩 초기화
        encodingCache.put("gpt-3.5-turbo", Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE));
        encodingCache.put("gpt-4", Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE));
        encodingCache.put("text-davinci-003", Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.P50K_BASE));
    }
    
    @Override
    public TokenInfo calculateTokens(String text, String model) {
        long startTime = System.currentTimeMillis();
        
        Encoding encoding = getEncodingForModel(model);
        List<Integer> tokens = encoding.encode(text);
        
        return TokenInfo.builder()
                .text(text.length() > 100 ? text.substring(0, 100) + "..." : text)
                .model(model)
                .inputTokens(tokens.size())
                .totalTokens(tokens.size())
                .estimatedCost(calculateCost(tokens.size(), model))
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .method(TokenInfo.TokenizationMethod.LOCAL_TIKTOKEN)
                .build();
    }
    
    @Override
    public Mono<TokenInfo> calculateTokensReactive(String text, String model) {
        return Mono.fromCallable(() -> calculateTokens(text, model))
                .subscribeOn(Schedulers.boundedElastic()); // CPU 집약적 작업을 별도 스레드에서
    }
    
    @Override
    public Flux<TokenInfo> calculateTokensBatch(List<String> texts, String model) {
        return Flux.fromIterable(texts)
                .parallel() // 병렬 처리
                .runOn(Schedulers.parallel())
                .map(text -> calculateTokens(text, model))
                .sequential();
    }
    
    @Override
    public Flux<TokenInfo> calculateTokensStream(Flux<String> textStream, String model) {
        return textStream
                .buffer(10) // 10개씩 배치 처리
                .flatMap(batch -> calculateTokensBatch(batch, model));
    }
    
    private Encoding getEncodingForModel(String model) {
        return encodingCache.getOrDefault(model, 
            encodingCache.get("gpt-3.5-turbo")); // 기본값
    }
    
    private double calculateCost(int tokens, String model) {
        // 모델별 토큰당 비용 계산
        Map<String, Double> costPerToken = Map.of(
            "gpt-3.5-turbo", 0.002 / 1000, // $0.002 per 1K tokens
            "gpt-4", 0.03 / 1000,          // $0.03 per 1K tokens
            "text-davinci-003", 0.02 / 1000
        );
        
        return tokens * costPerToken.getOrDefault(model, 0.002 / 1000);
    }
}
```

### 4. 외부 API 토크나이저 구현 (WebFlux 활용)
```java
@Service
@Slf4j
public class ExternalTokenCalculationService implements TokenCalculationService {
    
    private final WebClient webClient;
    private final TokenCalculationConfig config;
    
    public ExternalTokenCalculationService(WebClient.Builder webClientBuilder, 
                                         TokenCalculationConfig config) {
        this.config = config;
        this.webClient = webClientBuilder
                .baseUrl(config.getHuggingFaceApiUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiToken())
                .build();
    }
    
    @Override
    public Mono<TokenInfo> calculateTokensReactive(String text, String model) {
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> requestBody = Map.of(
            "inputs", text,
            "options", Map.of("use_cache", false)
        );
        
        return webClient
                .post()
                .uri("/models/{model}", getHuggingFaceModelName(model))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(HuggingFaceTokenResponse.class)
                .map(response -> TokenInfo.builder()
                        .text(text.length() > 100 ? text.substring(0, 100) + "..." : text)
                        .model(model)
                        .inputTokens(response.getTokens().size())
                        .totalTokens(response.getTokens().size())
                        .estimatedCost(calculateCost(response.getTokens().size(), model))
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .method(TokenInfo.TokenizationMethod.HUGGINGFACE_API)
                        .build())
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(ex -> {
                    log.warn("External tokenizer failed, falling back to local: {}", ex.getMessage());
                    return calculateTokensReactive(text, model); // 로컬 fallback
                });
    }
    
    @Override
    public Flux<TokenInfo> calculateTokensBatch(List<String> texts, String model) {
        return Flux.fromIterable(texts)
                .flatMap(text -> calculateTokensReactive(text, model), 5) // 동시 처리 제한
                .onBackpressureBuffer(100); // 백프레셔 처리
    }
    
    private String getHuggingFaceModelName(String model) {
        Map<String, String> modelMapping = Map.of(
            "gpt-3.5-turbo", "gpt2",
            "gpt-4", "gpt2",
            "claude", "bert-base-uncased"
        );
        return modelMapping.getOrDefault(model, "gpt2");
    }
}
```

### 5. 토큰 계산 통합 서비스
```java
@Service
@Slf4j
public class IntegratedTokenCalculationService {
    
    private final LocalTokenCalculationService localService;
    private final ExternalTokenCalculationService externalService;
    private final TokenCalculationConfig config;
    
    public Mono<TokenInfo> calculateTokens(String text, String model, boolean useExternal) {
        if (useExternal && config.isExternalApiEnabled()) {
            return externalService.calculateTokensReactive(text, model)
                    .onErrorResume(ex -> {
                        log.warn("External service failed, using local: {}", ex.getMessage());
                        return localService.calculateTokensReactive(text, model);
                    });
        } else {
            return localService.calculateTokensReactive(text, model);
        }
    }
    
    // 프롬프트와 예상 응답 토큰 모두 계산
    public Mono<TokenInfo> calculateRequestTokens(LLMRequest request) {
        String model = mapEngineToModel(request.getEngine());
        
        return calculateTokens(request.getPrompt(), model, false)
                .map(tokenInfo -> {
                    int maxResponseTokens = request.getMaxTokens() != null ? 
                            request.getMaxTokens() : 512;
                    
                    return TokenInfo.builder()
                            .text(tokenInfo.getText())
                            .model(model)
                            .inputTokens(tokenInfo.getInputTokens())
                            .outputTokens(maxResponseTokens)
                            .totalTokens(tokenInfo.getInputTokens() + maxResponseTokens)
                            .estimatedCost(tokenInfo.getEstimatedCost() + 
                                    calculateOutputCost(maxResponseTokens, model))
                            .processingTimeMs(tokenInfo.getProcessingTimeMs())
                            .method(tokenInfo.getMethod())
                            .build();
                });
    }
    
    private String mapEngineToModel(String engine) {
        Map<String, String> engineMapping = Map.of(
            "vllm", "gpt-3.5-turbo",
            "sglang", "gpt-4"
        );
        return engineMapping.getOrDefault(engine, "gpt-3.5-turbo");
    }
}
```

### 6. 컨트롤러에 토큰 계산 엔드포인트 추가
```java
@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@Slf4j
public class TokenController {
    
    private final IntegratedTokenCalculationService tokenService;
    
    @PostMapping("/calculate")
    public Mono<ResponseEntity<TokenInfo>> calculateTokens(
            @RequestBody TokenCalculationRequest request) {
        
        return tokenService.calculateTokens(
                request.getText(), 
                request.getModel(), 
                request.isUseExternal())
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> {
                    log.error("Token calculation failed: {}", ex.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
    
    @PostMapping("/calculate-batch")
    public Flux<TokenInfo> calculateTokensBatch(
            @RequestBody TokenBatchRequest request) {
        
        return tokenService.calculateTokensBatch(
                request.getTexts(), 
                request.getModel())
                .onBackpressureBuffer(1000);
    }
    
    @PostMapping("/estimate-request")
    public Mono<ResponseEntity<TokenInfo>> estimateRequestTokens(
            @RequestBody LLMRequest request) {
        
        return tokenService.calculateRequestTokens(request)
                .map(ResponseEntity::ok);
    }
}
```

### 7. LLM 서비스에 토큰 계산 통합
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedLLMService {
    
    private final LLMService originalLLMService;
    private final IntegratedTokenCalculationService tokenService;
    
    public Mono<LLMResponseWithTokens> generateResponseWithTokens(LLMRequest request) {
        return tokenService.calculateRequestTokens(request)
                .flatMap(tokenInfo -> {
                    // 토큰 한도 체크
                    if (tokenInfo.getTotalTokens() > getMaxTokensForUser()) {
                        return Mono.error(new TokenLimitExceededException(
                            "Token limit exceeded: " + tokenInfo.getTotalTokens()));
                    }
                    
                    // 기존 LLM 호출 (동기 방식)
                    return Mono.fromCallable(() -> originalLLMService.generateResponse(request))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(llmResponse -> LLMResponseWithTokens.builder()
                                    .llmResponse(llmResponse)
                                    .tokenInfo(tokenInfo)
                                    .build());
                });
    }
}
```

## 🎯 WebFlux 활용의 장점

### 1. **비동기 처리**
- 토큰 계산과 LLM 호출을 병렬로 처리
- 외부 토크나이저 API 호출 시 논블로킹

### 2. **백프레셔 처리**
- 대량 텍스트 배치 처리 시 메모리 보호
- 스트리밍 방식으로 메모리 효율적 처리

### 3. **에러 처리 및 Fallback**
- 외부 API 실패 시 로컬 토크나이저로 자동 전환
- Reactive한 에러 핸들링

### 4. **성능 최적화**
- 병렬 토큰 계산
- 캐싱과 결합하여 성능 향상

## 📊 구현 권장사항

### 우선순위 1: **로컬 토크나이저 (Tiktoken)**
```gradle
implementation 'com.knuddels:jtokkit:0.6.1'
```
- 빠른 처리 속도
- 네트워크 독립적
- OpenAI 호환

### 우선순위 2: **WebFlux 스트리밍 배치 처리**
- 대량 텍스트 처리 최적화
- 메모리 효율적

### 우선순위 3: **외부 API Fallback**
- Hugging Face API 연동
- 다양한 모델 지원

## 🚀 결론

**WebFlux를 활용한 토큰량 계산 기능은 충분히 추가 가능하며, 다음과 같은 이점이 있습니다:**

1. ✅ **기존 아키텍처와 호환**: 하이브리드 방식 유지
2. ✅ **성능 향상**: 병렬 처리 및 스트리밍
3. ✅ **확장성**: 배치 처리 및 외부 API 연동
4. ✅ **실용성**: 비용 관리 및 사용량 제한 기능

가장 실용적인 접근법은 **로컬 Tiktoken + WebFlux 스트리밍**을 조합하여 빠르고 안정적인 토큰 계산 서비스를 구축하는 것입니다.