package com.example.simple.service;

import com.example.simple.config.TokenCalculationConfig;
import com.example.simple.dto.TokenInfo;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tiktoken 라이브러리를 사용한 로컬 토큰 계산 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LocalTokenCalculationService implements TokenCalculationService {
    
    private final TokenCalculationConfig config;
    private final Map<String, Encoding> encodingCache = new ConcurrentHashMap<>();
    private EncodingRegistry registry;
    
    // 통계 정보
    private final AtomicLong totalCalculations = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    @PostConstruct
    public void initialize() {
        try {
            registry = Encodings.newDefaultEncodingRegistry();
            initializeEncodings();
            log.info("Local token calculation service initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize token calculation service", e);
            throw new RuntimeException("Token calculation service initialization failed", e);
        }
    }
    
    private void initializeEncodings() {
        // 주요 모델별 인코딩 초기화
        Map<String, EncodingType> modelEncodings = Map.of(
            "gpt-3.5-turbo", EncodingType.CL100K_BASE,
            "gpt-4", EncodingType.CL100K_BASE,
            "gpt-4-turbo", EncodingType.CL100K_BASE,
            "text-davinci-003", EncodingType.P50K_BASE,
            "text-davinci-002", EncodingType.P50K_BASE,
            "code-davinci-002", EncodingType.P50K_BASE,
            "claude-3-haiku", EncodingType.CL100K_BASE,  // Claude는 유사한 토크나이저 사용
            "claude-3-sonnet", EncodingType.CL100K_BASE,
            "claude-3-opus", EncodingType.CL100K_BASE
        );
        
        modelEncodings.forEach((model, encodingType) -> {
            try {
                Encoding encoding = registry.getEncoding(encodingType);
                encodingCache.put(model, encoding);
                log.debug("Initialized encoding for model: {}", model);
            } catch (Exception e) {
                log.warn("Failed to initialize encoding for model {}: {}", model, e.getMessage());
            }
        });
        
        log.info("Initialized {} model encodings", encodingCache.size());
    }
    
    @Override
    public TokenInfo calculateTokens(String text, String model) {
        if (text == null || text.isEmpty()) {
            return createEmptyTokenInfo(text, model);
        }
        
        if (text.length() > config.getLocal().getMaxTextLength()) {
            throw new IllegalArgumentException("Text length exceeds maximum allowed: " + 
                config.getLocal().getMaxTextLength());
        }
        
        long startTime = System.currentTimeMillis();
        totalCalculations.incrementAndGet();
        
        try {
            Encoding encoding = getEncodingForModel(model);
            List<Integer> tokens = encoding.encode(text);
            
            long processingTime = System.currentTimeMillis() - startTime;
            totalProcessingTime.addAndGet(processingTime);
            
            return TokenInfo.builder()
                    .text(truncateText(text))
                    .model(config.normalizeModelName(model))
                    .inputTokens(tokens.size())
                    .totalTokens(tokens.size())
                    .estimatedCost(calculateInputCost(tokens.size(), model))
                    .processingTimeMs(processingTime)
                    .method(TokenInfo.TokenizationMethod.LOCAL_TIKTOKEN)
                    .build();
                    
        } catch (Exception e) {
            log.error("Token calculation failed for model {}: {}", model, e.getMessage());
            throw new RuntimeException("Token calculation failed", e);
        }
    }
    
    @Override
    @Cacheable(value = "tokenCalculation", key = "#text.hashCode() + '_' + #model")
    public Mono<TokenInfo> calculateTokensReactive(String text, String model) {
        return Mono.fromCallable(() -> {
            cacheHits.incrementAndGet(); // 캐시에서 호출되지 않으면 miss로 처리됨
            return calculateTokens(text, model);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(throwable -> {
            cacheMisses.incrementAndGet();
            log.error("Reactive token calculation failed", throwable);
        });
    }
    
    @Override
    public Flux<TokenInfo> calculateTokensBatch(List<String> texts, String model) {
        if (texts == null || texts.isEmpty()) {
            return Flux.empty();
        }
        
        int parallelism = Math.min(config.getLocal().getParallelThreads(), texts.size());
        
        return Flux.fromIterable(texts)
                .parallel(parallelism)
                .runOn(Schedulers.parallel())
                .map(text -> calculateTokens(text, model))
                .doOnError(throwable -> log.error("Batch token calculation failed", throwable))
                .sequential()
                .onBackpressureBuffer(1000);
    }
    
    @Override
    public Flux<TokenInfo> calculateTokensStream(Flux<String> textStream, String model) {
        return textStream
                .buffer(10) // 10개씩 배치 처리
                .flatMap(batch -> calculateTokensBatch(batch, model), 2) // 최대 2개 배치 동시 처리
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
        String normalizedModel = config.normalizeModelName(model);
        return encodingCache.containsKey(normalizedModel);
    }
    
    @Override
    public Mono<Boolean> isHealthy() {
        return Mono.fromCallable(() -> {
            try {
                // 간단한 테스트 토큰화 수행
                String testText = "Hello, world!";
                calculateTokens(testText, "gpt-3.5-turbo");
                return true;
            } catch (Exception e) {
                log.warn("Health check failed", e);
                return false;
            }
        });
    }
    
    @Override
    public void clearCache() {
        // Spring Cache를 사용하므로 캐시 매니저를 통해 클리어
        log.info("Cache clear requested for local token calculation service");
    }
    
    @Override
    public ServiceStats getStats() {
        return new LocalServiceStats();
    }
    
    private Encoding getEncodingForModel(String model) {
        String normalizedModel = config.normalizeModelName(model);
        Encoding encoding = encodingCache.get(normalizedModel);
        
        if (encoding == null) {
            // 기본 인코딩 사용
            encoding = encodingCache.get("gpt-3.5-turbo");
            if (encoding == null) {
                encoding = registry.getEncoding(EncodingType.CL100K_BASE);
            }
            log.debug("Using default encoding for unknown model: {}", model);
        }
        
        return encoding;
    }
    
    private double calculateInputCost(int tokens, String model) {
        TokenCalculationConfig.CostConfig.ModelCost cost = config.getModelCost(model);
        return (tokens / 1000.0) * cost.getInputCostPer1K();
    }
    
    private double calculateOutputCost(int tokens, String model) {
        TokenCalculationConfig.CostConfig.ModelCost cost = config.getModelCost(model);
        return (tokens / 1000.0) * cost.getOutputCostPer1K();
    }
    
    private String truncateText(String text) {
        if (text.length() <= 100) {
            return text;
        }
        return text.substring(0, 100) + "...";
    }
    
    private TokenInfo createEmptyTokenInfo(String text, String model) {
        return TokenInfo.builder()
                .text(text != null ? text : "")
                .model(config.normalizeModelName(model))
                .inputTokens(0)
                .totalTokens(0)
                .estimatedCost(0.0)
                .processingTimeMs(0L)
                .method(TokenInfo.TokenizationMethod.LOCAL_TIKTOKEN)
                .build();
    }
    
    /**
     * 로컬 서비스 통계 구현
     */
    private class LocalServiceStats implements ServiceStats {
        @Override
        public long getTotalCalculations() {
            return totalCalculations.get();
        }
        
        @Override
        public long getCacheHits() {
            return cacheHits.get();
        }
        
        @Override
        public long getCacheMisses() {
            return cacheMisses.get();
        }
        
        @Override
        public double getAverageProcessingTime() {
            long calculations = totalCalculations.get();
            if (calculations == 0) return 0.0;
            return (double) totalProcessingTime.get() / calculations;
        }
        
        @Override
        public String getServiceType() {
            return "Local Tiktoken";
        }
    }
}