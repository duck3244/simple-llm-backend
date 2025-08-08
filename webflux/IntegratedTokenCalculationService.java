package com.example.simple.service;

import com.example.simple.config.TokenCalculationConfig;
import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponseWithTokens;
import com.example.simple.dto.TokenInfo;
import com.example.simple.exception.TokenLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 로컬 및 외부 토큰 계산 서비스를 통합하는 서비스
 * 사용자 요청에 따라 적절한 토큰 계산 방식을 선택하고 관리
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IntegratedTokenCalculationService {
    
    private final LocalTokenCalculationService localService;
    private final ExternalTokenCalculationService externalService;
    private final TokenCalculationConfig config;
    
    // 기본 토큰 제한값들
    private static final int DEFAULT_MAX_TOKENS_PER_REQUEST = 8192;
    private static final int DEFAULT_MAX_TOKENS_PER_USER_DAILY = 100000;
    
    /**
     * 단일 텍스트의 토큰 수를 계산합니다
     * 
     * @param text 계산할 텍스트
     * @param model 사용할 모델
     * @param useExternal 외부 API 사용 여부
     * @return 토큰 정보
     */
    public Mono<TokenInfo> calculateTokens(String text, String model, boolean useExternal) {
        if (useExternal && config.getExternal().isEnabled() && externalService != null) {
            return externalService.calculateTokensReactive(text, model)
                    .onErrorResume(ex -> {
                        log.warn("External service failed for model {}, falling back to local: {}", 
                                model, ex.getMessage());
                        return localService.calculateTokensReactive(text, model);
                    });
        } else {
            return localService.calculateTokensReactive(text, model);
        }
    }
    
    /**
     * LLM 요청의 토큰 수를 사전 계산합니다
     * 
     * @param request LLM 요청
     * @return 토큰 정보 (입력 + 예상 출력)
     */
    public Mono<TokenInfo> calculateRequestTokens(LLMRequest request) {
        String model = mapEngineToModel(request.getEngine());
        int maxResponseTokens = request.getMaxTokens() != null ? 
                request.getMaxTokens() : getDefaultMaxTokens(model);
        
        return localService.calculateTotalTokens(
                request.getPrompt(), 
                maxResponseTokens, 
                model
        ).doOnNext(tokenInfo -> {
            log.debug("Calculated tokens for request: input={}, output={}, total={}", 
                    tokenInfo.getInputTokens(), 
                    tokenInfo.getOutputTokens(), 
                    tokenInfo.getTotalTokens());
        });
    }
    
    /**
     * 토큰 제한을 확인하고 LLM 요청을 처리합니다
     * 
     * @param request LLM 요청
     * @param maxTokensAllowed 허용된 최대 토큰 수
     * @return 토큰 제한 검증 결과
     */
    public Mono<TokenValidationResult> validateTokenLimits(LLMRequest request, Integer maxTokensAllowed) {
        return calculateRequestTokens(request)
                .map(tokenInfo -> {
                    int allowedTokens = maxTokensAllowed != null ? 
                            maxTokensAllowed : DEFAULT_MAX_TOKENS_PER_REQUEST;
                    
                    boolean withinLimit = tokenInfo.getTotalTokens() <= allowedTokens;
                    
                    return TokenValidationResult.builder()
                            .valid(withinLimit)
                            .tokenInfo(tokenInfo)
                            .maxAllowed(allowedTokens)
                            .message(withinLimit ? 
                                    "Token limit validation passed" : 
                                    String.format("Token limit exceeded: %d > %d", 
                                            tokenInfo.getTotalTokens(), allowedTokens))
                            .build();
                });
    }
    
    /**
     * 배치 토큰 계산
     * 
     * @param texts 텍스트 리스트
     * @param model 모델명
     * @param useExternal 외부 API 사용 여부
     * @return 토큰 정보 스트림
     */
    public Flux<TokenInfo> calculateTokensBatch(List<String> texts, String model, boolean useExternal) {
        if (useExternal && config.getExternal().isEnabled() && externalService != null) {
            return externalService.calculateTokensBatch(texts, model)
                    .onErrorResume(ex -> {
                        log.warn("External batch service failed, falling back to local: {}", ex.getMessage());
                        return localService.calculateTokensBatch(texts, model);
                    });
        } else {
            return localService.calculateTokensBatch(texts, model);
        }
    }
    
    /**
     * 실시간 응답의 토큰 수를 계산합니다
     * 
     * @param responseText 생성된 응답 텍스트
     * @param model 사용된 모델
     * @return 응답 토큰 정보
     */
    public Mono<TokenInfo> calculateResponseTokens(String responseText, String model) {
        return localService.calculateTokensReactive(responseText, model)
                .map(tokenInfo -> tokenInfo.toBuilder()
                        .outputTokens(tokenInfo.getInputTokens()) // 응답의 경우 입력이 곧 출력
                        .inputTokens(0)
                        .build());
    }
    
    /**
     * 완전한 LLM 응답에 토큰 정보를 결합합니다
     * 
     * @param request 원본 요청
     * @param responseText 생성된 응답
     * @param model 사용된 모델
     * @return 토큰 정보가 포함된 응답
     */
    public Mono<LLMResponseWithTokens.TokenUsageSummary> createTokenUsageSummary(
            LLMRequest request, String responseText, String model) {
        
        return Mono.zip(
                calculateRequestTokens(request),
                calculateResponseTokens(responseText, model)
        ).map(tuple -> {
            TokenInfo requestTokens = tuple.getT1();
            TokenInfo responseTokens = tuple.getT2();
            
            int totalInputTokens = requestTokens.getInputTokens();
            int totalOutputTokens = responseTokens.getOutputTokens();
            double inputCost = calculateInputCost(totalInputTokens, model);
            double outputCost = calculateOutputCost(totalOutputTokens, model);
            
            return LLMResponseWithTokens.TokenUsageSummary.builder()
                    .totalInputTokens(totalInputTokens)
                    .totalOutputTokens(totalOutputTokens)
                    .totalTokens(totalInputTokens + totalOutputTokens)
                    .inputTokenCost(inputCost)
                    .outputTokenCost(outputCost)
                    .totalCost(inputCost + outputCost)
                    .totalProcessingTimeMs(requestTokens.getProcessingTimeMs() + responseTokens.getProcessingTimeMs())
                    .model(model)
                    .efficiencyScore(calculateEfficiencyScore(totalInputTokens, totalOutputTokens))
                    .build();
        });
    }
    
    /**
     * 서비스 상태를 확인합니다
     * 
     * @return 서비스 상태 정보
     */
    public Mono<ServiceHealthStatus> getHealthStatus() {
        return Mono.zip(
                localService.isHealthy(),
                externalService != null ? externalService.isHealthy() : Mono.just(false)
        ).map(tuple -> ServiceHealthStatus.builder()
                .localServiceHealthy(tuple.getT1())
                .externalServiceHealthy(tuple.getT2())
                .externalServiceEnabled(config.getExternal().isEnabled())
                .overallHealthy(tuple.getT1()) // 로컬 서비스가 주요 서비스
                .build());
    }
    
    /**
     * 통합 서비스 통계를 반환합니다
     * 
     * @return 통합 통계 정보
     */
    public CombinedServiceStats getCombinedStats() {
        TokenCalculationService.ServiceStats localStats = localService.getStats();
        TokenCalculationService.ServiceStats externalStats = 
                externalService != null ? externalService.getStats() : null;
        
        return CombinedServiceStats.builder()
                .localStats(localStats)
                .externalStats(externalStats)
                .totalCalculations(localStats.getTotalCalculations() + 
                        (externalStats != null ? externalStats.getTotalCalculations() : 0))
                .build();
    }
    
    private String mapEngineToModel(String engine) {
        if (engine == null) return "gpt-3.5-turbo";
        
        return config.normalizeModelName(engine);
    }
    
    private int getDefaultMaxTokens(String model) {
        // 모델별 기본 최대 토큰 수
        return switch (model.toLowerCase()) {
            case "gpt-4", "gpt-4-turbo" -> 1024;
            case "gpt-3.5-turbo" -> 512;
            case "claude-3-opus" -> 1024;
            case "claude-3-sonnet", "claude-3-haiku" -> 512;
            default -> 512;
        };
    }
    
    private double calculateInputCost(int tokens, String model) {
        TokenCalculationConfig.CostConfig.ModelCost cost = config.getModelCost(model);
        return (tokens / 1000.0) * cost.getInputCostPer1K();
    }
    
    private double calculateOutputCost(int tokens, String model) {
        TokenCalculationConfig.CostConfig.ModelCost cost = config.getModelCost(model);
        return (tokens / 1000.0) * cost.getOutputCostPer1K();
    }
    
    private double calculateEfficiencyScore(int inputTokens, int outputTokens) {
        if (inputTokens == 0) return 0.0;
        
        // 효율성 점수: 출력/입력 비율을 기반으로 0-100 점수 계산
        double ratio = (double) outputTokens / inputTokens;
        
        // 1:1 비율을 100점으로 하고, 비율에 따라 점수 조정
        if (ratio <= 1.0) {
            return ratio * 100;
        } else {
            // 출력이 입력보다 많으면 점수를 낮춤
            return Math.max(0, 100 - (ratio - 1) * 20);
        }
    }
    
    /**
     * 토큰 검증 결과
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final TokenInfo tokenInfo;
        private final int maxAllowed;
        private final String message;
        
        private TokenValidationResult(boolean valid, TokenInfo tokenInfo, int maxAllowed, String message) {
            this.valid = valid;
            this.tokenInfo = tokenInfo;
            this.maxAllowed = maxAllowed;
            this.message = message;
        }
        
        public static TokenValidationResultBuilder builder() {
            return new TokenValidationResultBuilder();
        }
        
        public boolean isValid() { return valid; }
        public TokenInfo getTokenInfo() { return tokenInfo; }
        public int getMaxAllowed() { return maxAllowed; }
        public String getMessage() { return message; }
        
        public static class TokenValidationResultBuilder {
            private boolean valid;
            private TokenInfo tokenInfo;
            private int maxAllowed;
            private String message;
            
            public TokenValidationResultBuilder valid(boolean valid) {
                this.valid = valid;
                return this;
            }
            
            public TokenValidationResultBuilder tokenInfo(TokenInfo tokenInfo) {
                this.tokenInfo = tokenInfo;
                return this;
            }
            
            public TokenValidationResultBuilder maxAllowed(int maxAllowed) {
                this.maxAllowed = maxAllowed;
                return this;
            }
            
            public TokenValidationResultBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            public TokenValidationResult build() {
                return new TokenValidationResult(valid, tokenInfo, maxAllowed, message);
            }
        }
    }
    
    /**
     * 서비스 상태 정보
     */
    public static class ServiceHealthStatus {
        private final boolean localServiceHealthy;
        private final boolean externalServiceHealthy;
        private final boolean externalServiceEnabled;
        private final boolean overallHealthy;
        
        private ServiceHealthStatus(boolean localServiceHealthy, boolean externalServiceHealthy, 
                                  boolean externalServiceEnabled, boolean overallHealthy) {
            this.localServiceHealthy = localServiceHealthy;
            this.externalServiceHealthy = externalServiceHealthy;
            this.externalServiceEnabled = externalServiceEnabled;
            this.overallHealthy = overallHealthy;
        }
        
        public static ServiceHealthStatusBuilder builder() {
            return new ServiceHealthStatusBuilder();
        }
        
        public boolean isLocalServiceHealthy() { return localServiceHealthy; }
        public boolean isExternalServiceHealthy() { return externalServiceHealthy; }
        public boolean isExternalServiceEnabled() { return externalServiceEnabled; }
        public boolean isOverallHealthy() { return overallHealthy; }
        
        public static class ServiceHealthStatusBuilder {
            private boolean localServiceHealthy;
            private boolean externalServiceHealthy;
            private boolean externalServiceEnabled;
            private boolean overallHealthy;
            
            public ServiceHealthStatusBuilder localServiceHealthy(boolean localServiceHealthy) {
                this.localServiceHealthy = localServiceHealthy;
                return this;
            }
            
            public ServiceHealthStatusBuilder externalServiceHealthy(boolean externalServiceHealthy) {
                this.externalServiceHealthy = externalServiceHealthy;
                return this;
            }
            
            public ServiceHealthStatusBuilder externalServiceEnabled(boolean externalServiceEnabled) {
                this.externalServiceEnabled = externalServiceEnabled;
                return this;
            }
            
            public ServiceHealthStatusBuilder overallHealthy(boolean overallHealthy) {
                this.overallHealthy = overallHealthy;
                return this;
            }
            
            public ServiceHealthStatus build() {
                return new ServiceHealthStatus(localServiceHealthy, externalServiceHealthy, 
                                             externalServiceEnabled, overallHealthy);
            }
        }
    }
    
    /**
     * 통합 서비스 통계
     */
    public static class CombinedServiceStats {
        private final TokenCalculationService.ServiceStats localStats;
        private final TokenCalculationService.ServiceStats externalStats;
        private final long totalCalculations;
        
        private CombinedServiceStats(TokenCalculationService.ServiceStats localStats, 
                                   TokenCalculationService.ServiceStats externalStats, 
                                   long totalCalculations) {
            this.localStats = localStats;
            this.externalStats = externalStats;
            this.totalCalculations = totalCalculations;
        }
        
        public static CombinedServiceStatsBuilder builder() {
            return new CombinedServiceStatsBuilder();
        }
        
        public TokenCalculationService.ServiceStats getLocalStats() { return localStats; }
        public TokenCalculationService.ServiceStats getExternalStats() { return externalStats; }
        public long getTotalCalculations() { return totalCalculations; }
        
        public static class CombinedServiceStatsBuilder {
            private TokenCalculationService.ServiceStats localStats;
            private TokenCalculationService.ServiceStats externalStats;
            private long totalCalculations;
            
            public CombinedServiceStatsBuilder localStats(TokenCalculationService.ServiceStats localStats) {
                this.localStats = localStats;
                return this;
            }
            
            public CombinedServiceStatsBuilder externalStats(TokenCalculationService.ServiceStats externalStats) {
                this.externalStats = externalStats;
                return this;
            }
            
            public CombinedServiceStatsBuilder totalCalculations(long totalCalculations) {
                this.totalCalculations = totalCalculations;
                return this;
            }
            
            public CombinedServiceStats build() {
                return new CombinedServiceStats(localStats, externalStats, totalCalculations);
            }
        }
    }
}