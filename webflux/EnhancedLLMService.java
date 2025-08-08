package com.example.simple.service;

import com.example.simple.dto.*;
import com.example.simple.exception.TokenLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 토큰 계산이 통합된 강화된 LLM 서비스
 * 기존 LLM 서비스에 토큰 계산 및 제한 기능을 추가
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EnhancedLLMService {
    
    private final LLMService originalLLMService;
    private final IntegratedTokenCalculationService tokenService;
    
    // 기본 토큰 제한값들
    private static final int DEFAULT_MAX_TOKENS_PER_REQUEST = 8192;
    private static final double DEFAULT_MAX_COST_PER_REQUEST = 1.0; // $1.00
    
    /**
     * 토큰 정보가 포함된 LLM 응답을 생성합니다
     * 
     * @param request LLM 요청
     * @return 토큰 정보가 포함된 응답
     */
    public Mono<LLMResponseWithTokens> generateResponseWithTokens(LLMRequest request) {
        return generateResponseWithTokens(request, null, null);
    }
    
    /**
     * 토큰 및 비용 제한을 적용하여 LLM 응답을 생성합니다
     * 
     * @param request LLM 요청
     * @param maxTokens 최대 허용 토큰 수 (null이면 기본값 사용)
     * @param maxCost 최대 허용 비용 (null이면 기본값 사용)
     * @return 토큰 정보가 포함된 응답
     */
    public Mono<LLMResponseWithTokens> generateResponseWithTokens(LLMRequest request, 
                                                                 Integer maxTokens, 
                                                                 Double maxCost) {
        
        long startTime = System.currentTimeMillis();
        
        return validateRequestLimits(request, maxTokens, maxCost)
                .flatMap(validationResult -> {
                    if (!validationResult.isValid()) {
                        return Mono.error(new TokenLimitExceededException(
                            validationResult.getMessage(),
                            validationResult.getTokenInfo().getTotalTokens(),
                            validationResult.getMaxAllowed(),
                            validationResult.getTokenInfo().getModel()
                        ));
                    }
                    
                    // 토큰 정보 저장
                    TokenInfo requestTokenInfo = validationResult.getTokenInfo();
                    
                    // 기존 LLM 서비스 호출 (동기 방식)
                    return Mono.fromCallable(() -> originalLLMService.generateResponse(request))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(llmResponse -> {
                                if (!llmResponse.isSuccess()) {
                                    return Mono.just(createFailedResponse(llmResponse, requestTokenInfo));
                                }
                                
                                // 응답 텍스트의 토큰 수 계산
                                return tokenService.createTokenUsageSummary(
                                        request, 
                                        llmResponse.getText(), 
                                        requestTokenInfo.getModel())
                                        .map(tokenUsage -> LLMResponseWithTokens.builder()
                                                .llmResponse(llmResponse)
                                                .requestTokenInfo(requestTokenInfo)
                                                .responseTokenInfo(createResponseTokenInfo(llmResponse.getText(), requestTokenInfo.getModel()))
                                                .tokenUsage(tokenUsage)
                                                .build());
                            });
                })
                .doOnSuccess(response -> {
                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("Enhanced LLM 응답 완료: engine={}, totalTime={}ms, totalTokens={}, totalCost=${}", 
                            request.getEngine(), totalTime, 
                            response.getTokenUsage().getTotalTokens(),
                            String.format("%.4f", response.getTokenUsage().getTotalCost()));
                })
                .doOnError(ex -> {
                    long totalTime = System.currentTimeMillis() - startTime;
                    log.error("Enhanced LLM 응답 실패: engine={}, totalTime={}ms, error={}", 
                            request.getEngine(), totalTime, ex.getMessage());
                });
    }
    
    /**
     * 토큰 사용량만 미리 계산합니다 (실제 LLM 호출 없이)
     * 
     * @param request LLM 요청
     * @return 예상 토큰 사용량 정보
     */
    public Mono<TokenInfo> estimateTokenUsage(LLMRequest request) {
        return tokenService.calculateRequestTokens(request)
                .doOnSuccess(tokenInfo -> {
                    log.debug("토큰 사용량 추정: engine={}, inputTokens={}, outputTokens={}, cost=${}", 
                            request.getEngine(), tokenInfo.getInputTokens(), 
                            tokenInfo.getOutputTokens(), String.format("%.4f", tokenInfo.getEstimatedCost()));
                });
    }
    
    /**
     * 여러 요청의 토큰 사용량을 배치로 추정합니다
     * 
     * @param requests LLM 요청 리스트
     * @return 토큰 사용량 추정 스트림
     */
    public reactor.core.publisher.Flux<TokenInfo> estimateTokenUsageBatch(java.util.List<LLMRequest> requests) {
        return reactor.core.publisher.Flux.fromIterable(requests)
                .flatMap(this::estimateTokenUsage, 5) // 최대 5개 동시 처리
                .doOnNext(tokenInfo -> log.debug("배치 토큰 추정: tokens={}, cost=${}", 
                        tokenInfo.getTotalTokens(), String.format("%.4f", tokenInfo.getEstimatedCost())));
    }
    
    /**
     * 사용자별 토큰 사용량 체크 (향후 구현용 메서드)
     * 
     * @param userId 사용자 ID
     * @param request LLM 요청
     * @return 사용량 체크 결과
     */
    public Mono<Boolean> checkUserTokenQuota(String userId, LLMRequest request) {
        // TODO: 실제 구현 시 사용자별 토큰 사용량 DB 조회 및 체크
        return estimateTokenUsage(request)
                .map(tokenInfo -> {
                    // 임시 로직: 일일 10,000 토큰 제한
                    int dailyLimit = 10000;
                    int currentUsage = getCurrentUserTokenUsage(userId); // 구현 필요
                    
                    boolean withinQuota = currentUsage + tokenInfo.getTotalTokens() <= dailyLimit;
                    
                    log.debug("사용자 토큰 할당량 체크: userId={}, current={}, requested={}, limit={}, allowed={}", 
                            userId, currentUsage, tokenInfo.getTotalTokens(), dailyLimit, withinQuota);
                    
                    return withinQuota;
                });
    }
    
    /**
     * 비용 기반 요청 필터링
     * 
     * @param request LLM 요청
     * @param maxCostPerRequest 요청당 최대 비용
     * @return 비용 체크 통과 여부
     */
    public Mono<Boolean> checkCostLimit(LLMRequest request, double maxCostPerRequest) {
        return estimateTokenUsage(request)
                .map(tokenInfo -> {
                    boolean withinCostLimit = tokenInfo.getEstimatedCost() <= maxCostPerRequest;
                    
                    log.debug("비용 제한 체크: estimatedCost=${}, maxCost=${}, allowed={}", 
                            String.format("%.4f", tokenInfo.getEstimatedCost()),
                            String.format("%.4f", maxCostPerRequest), 
                            withinCostLimit);
                    
                    return withinCostLimit;
                });
    }
    
    private Mono<IntegratedTokenCalculationService.TokenValidationResult> validateRequestLimits(
            LLMRequest request, Integer maxTokens, Double maxCost) {
        
        int tokenLimit = maxTokens != null ? maxTokens : DEFAULT_MAX_TOKENS_PER_REQUEST;
        double costLimit = maxCost != null ? maxCost : DEFAULT_MAX_COST_PER_REQUEST;
        
        return tokenService.calculateRequestTokens(request)
                .map(tokenInfo -> {
                    // 토큰 제한 체크
                    if (tokenInfo.getTotalTokens() > tokenLimit) {
                        return IntegratedTokenCalculationService.TokenValidationResult.builder()
                                .valid(false)
                                .tokenInfo(tokenInfo)
                                .maxAllowed(tokenLimit)
                                .message(String.format("토큰 제한 초과: %d > %d", 
                                        tokenInfo.getTotalTokens(), tokenLimit))
                                .build();
                    }
                    
                    // 비용 제한 체크
                    if (tokenInfo.getEstimatedCost() > costLimit) {
                        return IntegratedTokenCalculationService.TokenValidationResult.builder()
                                .valid(false)
                                .tokenInfo(tokenInfo)
                                .maxAllowed(tokenLimit)
                                .message(String.format("비용 제한 초과: $%.4f > $%.4f", 
                                        tokenInfo.getEstimatedCost(), costLimit))
                                .build();
                    }
                    
                    return IntegratedTokenCalculationService.TokenValidationResult.builder()
                            .valid(true)
                            .tokenInfo(tokenInfo)
                            .maxAllowed(tokenLimit)
                            .message("모든 제한 검증 통과")
                            .build();
                });
    }
    
    private LLMResponseWithTokens createFailedResponse(LLMResponse llmResponse, TokenInfo requestTokenInfo) {
        return LLMResponseWithTokens.builder()
                .llmResponse(llmResponse)
                .requestTokenInfo(requestTokenInfo)
                .responseTokenInfo(TokenInfo.builder()
                        .text("")
                        .model(requestTokenInfo.getModel())
                        .inputTokens(0)
                        .outputTokens(0)
                        .totalTokens(0)
                        .estimatedCost(0.0)
                        .processingTimeMs(0L)
                        .method(requestTokenInfo.getMethod())
                        .build())
                .tokenUsage(LLMResponseWithTokens.TokenUsageSummary.builder()
                        .totalInputTokens(requestTokenInfo.getInputTokens())
                        .totalOutputTokens(0)
                        .totalTokens(requestTokenInfo.getInputTokens())
                        .inputTokenCost(requestTokenInfo.getEstimatedCost())
                        .outputTokenCost(0.0)
                        .totalCost(requestTokenInfo.getEstimatedCost())
                        .totalProcessingTimeMs(requestTokenInfo.getProcessingTimeMs())
                        .model(requestTokenInfo.getModel())
                        .efficiencyScore(0.0)
                        .build())
                .build();
    }
    
    private TokenInfo createResponseTokenInfo(String responseText, String model) {
        // 응답 텍스트에 대한 간단한 토큰 정보 생성
        // 실제로는 tokenService를 통해 계산해야 하지만, 여기서는 간단히 추정
        int estimatedTokens = estimateTokensSimple(responseText);
        
        return TokenInfo.builder()
                .text(responseText.length() > 100 ? responseText.substring(0, 100) + "..." : responseText)
                .model(model)
                .inputTokens(0)
                .outputTokens(estimatedTokens)
                .totalTokens(estimatedTokens)
                .estimatedCost(0.0) // 비용은 TokenUsageSummary에서 계산됨
                .processingTimeMs(0L)
                .method(TokenInfo.TokenizationMethod.SIMPLE_ESTIMATION)
                .build();
    }
    
    private int estimateTokensSimple(String text) {
        if (text == null || text.isEmpty()) return 0;
        
        // 간단한 토큰 추정: 대략 4글자당 1토큰
        return (int) Math.ceil(text.length() / 4.0);
    }
    
    private int getCurrentUserTokenUsage(String userId) {
        // TODO: 실제 구현 시 데이터베이스에서 사용자의 현재 토큰 사용량 조회
        // 여기서는 임시로 0 반환
        return 0;
    }
}