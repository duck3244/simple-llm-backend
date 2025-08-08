package com.example.simple.service;

import com.example.simple.config.TokenCalculationConfig;
import com.example.simple.dto.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 외부 API를 사용한 토큰 계산 서비스
 * Hugging Face, OpenAI API 등을 활용
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "token-calculation.external.enabled", havingValue = "true")
public class ExternalTokenCalculationService implements TokenCalculationService {
    
    private final TokenCalculationConfig config;
    private final WebClient.Builder webClientBuilder;
    private final LocalTokenCalculationService fallbackService;
    
    private WebClient huggingFaceClient;
    private WebClient openAIClient;
    
    // 통계 정보
    private final AtomicLong totalCalculations = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicLong apiErrors = new AtomicLong(0);
    private final AtomicLong fallbackUsages = new AtomicLong(0);
    
    @PostConstruct
    public void initialize() {
        initializeClients();
        log.info("External token calculation service initialized");
    }
    
    private void initializeClients() {
        // Hugging Face API 클라이언트
        if (config.getExternal().getHuggingFace().isEnabled()) {
            huggingFaceClient = webClientBuilder
                    .baseUrl(config.getExternal().getHuggingFace().getBaseUrl())
                    .defaultHeader("Authorization", 
                        "Bearer " + config.getExternal().getHuggingFace().getApiToken())
                    .build();
            log.info("Hugging Face API client initialized");
        }
        
        // OpenAI API 클라이언트
        if (config.getExternal().getOpenAI().isEnabled()) {
            openAIClient = webClientBuilder
                    .baseUrl(config.getExternal().getOpenAI().getBaseUrl())
                    .defaultHeader("Authorization", 
                        "Bearer " + config.getExternal().getOpenAI().getApiToken())
                    .build();
            log.info("OpenAI API client initialized");
        }
    }
    
    @Override
    public TokenInfo calculateTokens(String text, String model) {
        // 외부 API는 비동기이므로 동기 메서드는 블로킹 처리
        return calculateTokensReactive(text, model).block();
    }
    
    @Override
    public Mono<TokenInfo> calculateTokensReactive(String text, String model) {
        if (text == null || text.isEmpty()) {
            return Mono.just(createEmptyTokenInfo(text, model));
        }
        
        long startTime = System.currentTimeMillis();
        totalCalculations.incrementAndGet();
        
        return calculateWithPrimaryApi(text, model, startTime)
                .onErrorResume(this::handleApiError)
                .switchIfEmpty(fallbackToLocal(text, model, startTime))
                .doOnSuccess(result -> {
                    long processingTime = System.currentTimeMillis() - startTime;
                    totalProcessingTime.addAndGet(processingTime);
                });
    }
    
    @Override
    public Flux<TokenInfo> calculateTokensBatch(List<String> texts, String model) {
        if (texts == null || texts.isEmpty()) {
            return Flux.empty();
        }
        
        return Flux.fromIterable(texts)
                .flatMap(text -> calculateTokensReactive(text, model), 
                    config.getExternal().getConcurrencyLimit())
                .onBackpressureBuffer(1000);
    }
    
    @Override
    public Flux<TokenInfo> calculateTokensStream(Flux<String> textStream, String model) {
        return textStream
                .flatMap(text -> calculateTokensReactive(text, model), 
                    config.getExternal().getConcurrencyLimit())
                .onBackpressureBuffer(500);
    }
    
    @Override
    public Mono<TokenInfo> calculateTotalTokens(String inputText, int expectedOutputTokens, String model) {
        return calculateTokensReactive(inputText, model)
                .map(tokenInfo -> {
                    double inputCost = tokenInfo.getEstimatedCost();
                    double outputCost = calculateOutputCost(expectedOutputTokens, model);
                    
                    return TokenInfo.builder()
                            .text(tokenInfo.getText())
                            .model(tokenInfo.getModel())
                            .inputTokens(tokenInfo.getInputTokens())
                            .outputTokens(expectedOutputTokens)
                            .totalTokens(tokenInfo.getInputTokens() + expectedOutputTokens)
                            .estimatedCost(inputCost + outputCost)
                            .processingTimeMs(tokenInfo.getProcessingTimeMs())
                            .method(tokenInfo.getMethod())
                            .calculatedAt(tokenInfo.getCalculatedAt())
                            .build();
                });
    }
    
    @Override
    public boolean supportsModel(String model) {
        // 외부 API는 대부분의 모델을 지원한다고 가정
        return config.getExternal().isEnabled();
    }
    
    @Override
    public Mono<Boolean> isHealthy() {
        return testApiConnection()
                .map(result -> result)
                .onErrorReturn(false);
    }
    
    @Override
    public void clearCache() {
        log.info("External API service does not maintain local cache");
    }
    
    @Override
    public ServiceStats getStats() {
        return new ExternalServiceStats();
    }
    
    private Mono<TokenInfo> calculateWithPrimaryApi(String text, String model, long startTime) {
        // 우선순위: OpenAI API > Hugging Face API
        if (openAIClient != null && shouldUseOpenAI(model)) {
            return calculateWithOpenAI(text, model, startTime);
        } else if (huggingFaceClient != null) {
            return calculateWithHuggingFace(text, model, startTime);
        } else {
            return Mono.empty();
        }
    }
    
    private Mono<TokenInfo> calculateWithOpenAI(String text, String model, long startTime) {
        Map<String, Object> requestBody = Map.of(
            "model", mapToOpenAIModel(model),
            "input", text
        );
        
        return openAIClient
                .post()
                .uri("/tokenizer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(OpenAITokenResponse.class)
                .map(response -> createTokenInfo(text, model, response.getTokens().size(), 
                    System.currentTimeMillis() - startTime, TokenInfo.TokenizationMethod.OPENAI_API))
                .timeout(config.getExternal().getOpenAI().getTimeout())
                .retryWhen(Retry.backoff(config.getExternal().getRetryAttempts(), Duration.ofMillis(500)));
    }
    
    private Mono<TokenInfo> calculateWithHuggingFace(String text, String model, long startTime) {
        Map<String, Object> requestBody = Map.of(
            "inputs", text,
            "options", Map.of("use_cache", false)
        );
        
        String huggingFaceModel = mapToHuggingFaceModel(model);
        
        return huggingFaceClient
                .post()
                .uri("/models/{model}", huggingFaceModel)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(HuggingFaceTokenResponse.class)
                .map(response -> createTokenInfo(text, model, response.getTokenCount(), 
                    System.currentTimeMillis() - startTime, TokenInfo.TokenizationMethod.HUGGINGFACE_API))
                .timeout(config.getExternal().getHuggingFace().getTimeout())
                .retryWhen(Retry.backoff(config.getExternal().getRetryAttempts(), Duration.ofMillis(500)));
    }
    
    private Mono<TokenInfo> handleApiError(Throwable throwable) {
        apiErrors.incrementAndGet();
        log.warn("External API call failed: {}", throwable.getMessage());
        return Mono.empty(); // 빈 Mono 반환하여 fallback 트리거
    }
    
    private Mono<TokenInfo> fallbackToLocal(String text, String model, long startTime) {
        fallbackUsages.incrementAndGet();
        log.debug("Falling back to local token calculation for model: {}", model);
        
        return fallbackService.calculateTokensReactive(text, model)
                .map(tokenInfo -> tokenInfo.toBuilder()
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build());
    }
    
    private Mono<Boolean> testApiConnection() {
        String testText = "Hello";
        
        if (openAIClient != null) {
            return calculateWithOpenAI(testText, "gpt-3.5-turbo", System.currentTimeMillis())
                    .map(result -> true)
                    .onErrorReturn(false);
        } else if (huggingFaceClient != null) {
            return calculateWithHuggingFace(testText, "gpt-3.5-turbo", System.currentTimeMillis())
                    .map(result -> true)
                    .onErrorReturn(false);
        }
        
        return Mono.just(false);
    }
    
    private boolean shouldUseOpenAI(String model) {
        // OpenAI 모델들은 OpenAI API를 우선 사용
        return model.toLowerCase().contains("gpt") || model.toLowerCase().contains("davinci");
    }
    
    private String mapToOpenAIModel(String model) {
        Map<String, String> modelMapping = Map.of(
            "vllm", "gpt-3.5-turbo",
            "sglang", "gpt-4",
            "gpt-3.5-turbo", "gpt-3.5-turbo",
            "gpt-4", "gpt-4"
        );
        return modelMapping.getOrDefault(model, "gpt-3.5-turbo");
    }
    
    private String mapToHuggingFaceModel(String model) {
        Map<String, String> modelMapping = Map.of(
            "gpt-3.5-turbo", "gpt2",
            "gpt-4", "gpt2",
            "claude", "bert-base-uncased",
            "vllm", "gpt2",
            "sglang", "gpt2"
        );
        return modelMapping.getOrDefault(model, "gpt2");
    }
    
    private double calculateOutputCost(int tokens, String model) {
        TokenCalculationConfig.CostConfig.ModelCost cost = config.getModelCost(model);
        return (tokens / 1000.0) * cost.getOutputCostPer1K();
    }
    
    private TokenInfo createTokenInfo(String text, String model, int tokens, long processingTime, 
                                     TokenInfo.TokenizationMethod method) {
        return TokenInfo.builder()
                .text(truncateText(text))
                .model(config.normalizeModelName(model))
                .inputTokens(tokens)
                .totalTokens(tokens)
                .estimatedCost(calculateInputCost(tokens, model))
                .processingTimeMs(processingTime)
                .method(method)
                .build();
    }
    
    private double calculateInputCost(int tokens, String model) {
        TokenCalculationConfig.CostConfig.ModelCost cost = config.getModelCost(model);
        return (tokens / 1000.0) * cost.getInputCostPer1K();
    }
    
    private TokenInfo createEmptyTokenInfo(String text, String model) {
        return TokenInfo.builder()
                .text(text != null ? text : "")
                .model(config.normalizeModelName(model))
                .inputTokens(0)
                .totalTokens(0)
                .estimatedCost(0.0)
                .processingTimeMs(0L)
                .method(TokenInfo.TokenizationMethod.SIMPLE_ESTIMATION)
                .build();
    }
    
    private String truncateText(String text) {
        if (text.length() <= 100) {
            return text;
        }
        return text.substring(0, 100) + "...";
    }
    
    /**
     * 외부 서비스 통계 구현
     */
    private class ExternalServiceStats implements ServiceStats {
        @Override
        public long getTotalCalculations() {
            return totalCalculations.get();
        }
        
        @Override
        public long getCacheHits() {
            return 0; // 외부 API는 로컬 캐시 없음
        }
        
        @Override
        public long getCacheMisses() {
            return totalCalculations.get();
        }
        
        @Override
        public double getAverageProcessingTime() {
            long calculations = totalCalculations.get();
            if (calculations == 0) return 0.0;
            return (double) totalProcessingTime.get() / calculations;
        }
        
        @Override
        public String getServiceType() {
            return "External API (HuggingFace/OpenAI)";
        }
        
        public long getApiErrors() {
            return apiErrors.get();
        }
        
        public long getFallbackUsages() {
            return fallbackUsages.get();
        }
    }
}

/**
 * OpenAI API 응답 DTO
 */
class OpenAITokenResponse {
    private List<Integer> tokens;
    
    public List<Integer> getTokens() {
        return tokens;
    }
    
    public void setTokens(List<Integer> tokens) {
        this.tokens = tokens;
    }
}

/**
 * Hugging Face API 응답 DTO
 */
class HuggingFaceTokenResponse {
    private int tokenCount;
    
    public int getTokenCount() {
        return tokenCount;
    }
    
    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }
}