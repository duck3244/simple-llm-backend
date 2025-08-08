package com.example.simple.controller;

import com.example.simple.dto.*;
import com.example.simple.service.IntegratedTokenCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Map;

/**
 * 토큰 계산 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@Slf4j
public class TokenController {
    
    private final IntegratedTokenCalculationService tokenService;
    
    /**
     * 단일 텍스트의 토큰 수를 계산합니다
     * 
     * @param request 토큰 계산 요청
     * @return 토큰 정보
     */
    @PostMapping("/calculate")
    public Mono<ResponseEntity<TokenInfo>> calculateTokens(
            @Valid @RequestBody TokenCalculationRequest request) {
        
        log.info("토큰 계산 요청: model={}, textLength={}, useExternal={}", 
                request.getModel(), request.getText().length(), request.isUseExternal());
        
        return tokenService.calculateTokens(
                request.getText(), 
                request.getModel(), 
                request.isUseExternal())
                .map(tokenInfo -> {
                    log.debug("토큰 계산 완료: tokens={}, cost=${}", 
                            tokenInfo.getTotalTokens(), tokenInfo.getEstimatedCost());
                    return ResponseEntity.ok(tokenInfo);
                })
                .onErrorResume(ex -> {
                    log.error("토큰 계산 실패: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
    
    /**
     * 여러 텍스트의 토큰 수를 배치로 계산합니다
     * 
     * @param request 배치 토큰 계산 요청
     * @return 토큰 정보 스트림
     */
    @PostMapping("/calculate-batch")
    public Flux<TokenInfo> calculateTokensBatch(
            @Valid @RequestBody TokenBatchRequest request) {
        
        log.info("배치 토큰 계산 요청: model={}, textCount={}, parallelism={}", 
                request.getModel(), request.getTexts().size(), request.getParallelism());
        
        return tokenService.calculateTokensBatch(
                request.getTexts(), 
                request.getModel(), 
                request.isUseExternal())
                .doOnNext(tokenInfo -> log.debug("배치 토큰 계산: tokens={}", tokenInfo.getTotalTokens()))
                .onBackpressureBuffer(1000)
                .doOnError(ex -> log.error("배치 토큰 계산 실패: {}", ex.getMessage(), ex));
    }
    
    /**
     * LLM 요청의 토큰 사용량을 추정합니다
     * 
     * @param request LLM 요청
     * @return 토큰 사용량 추정 정보
     */
    @PostMapping("/estimate-request")
    public Mono<ResponseEntity<TokenInfo>> estimateRequestTokens(
            @Valid @RequestBody LLMRequest request) {
        
        log.info("LLM 요청 토큰 추정: engine={}, promptLength={}, maxTokens={}", 
                request.getEngine(), request.getPrompt().length(), request.getMaxTokens());
        
        return tokenService.calculateRequestTokens(request)
                .map(tokenInfo -> {
                    log.debug("토큰 추정 완료: input={}, output={}, total={}, cost=${}", 
                            tokenInfo.getInputTokens(), tokenInfo.getOutputTokens(), 
                            tokenInfo.getTotalTokens(), tokenInfo.getEstimatedCost());
                    return ResponseEntity.ok(tokenInfo);
                })
                .onErrorResume(ex -> {
                    log.error("토큰 추정 실패: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
    
    /**
     * 토큰 제한을 검증합니다
     * 
     * @param request LLM 요청
     * @param maxTokens 최대 허용 토큰 수 (선택사항)
     * @return 토큰 제한 검증 결과
     */
    @PostMapping("/validate")
    public Mono<ResponseEntity<IntegratedTokenCalculationService.TokenValidationResult>> validateTokenLimits(
            @Valid @RequestBody LLMRequest request,
            @RequestParam(required = false) Integer maxTokens) {
        
        log.info("토큰 제한 검증: engine={}, maxTokens={}", request.getEngine(), maxTokens);
        
        return tokenService.validateTokenLimits(request, maxTokens)
                .map(result -> {
                    log.debug("토큰 제한 검증 결과: valid={}, tokens={}/{}", 
                            result.isValid(), result.getTokenInfo().getTotalTokens(), result.getMaxAllowed());
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(ex -> {
                    log.error("토큰 제한 검증 실패: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
    
    /**
     * 토큰 계산 서비스의 상태를 확인합니다
     * 
     * @return 서비스 상태 정보
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<IntegratedTokenCalculationService.ServiceHealthStatus>> getServiceHealth() {
        return tokenService.getHealthStatus()
                .map(status -> {
                    log.debug("토큰 서비스 상태: overall={}, local={}, external={}", 
                            status.isOverallHealthy(), status.isLocalServiceHealthy(), status.isExternalServiceHealthy());
                    return ResponseEntity.ok(status);
                });
    }
    
    /**
     * 토큰 계산 서비스의 통계 정보를 반환합니다
     * 
     * @return 서비스 통계 정보
     */
    @GetMapping("/stats")
    public ResponseEntity<IntegratedTokenCalculationService.CombinedServiceStats> getServiceStats() {
        IntegratedTokenCalculationService.CombinedServiceStats stats = tokenService.getCombinedStats();
        
        log.debug("토큰 서비스 통계: totalCalculations={}", stats.getTotalCalculations());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 지원되는 모델 목록을 반환합니다
     * 
     * @return 지원 모델 정보
     */
    @GetMapping("/supported-models")
    public ResponseEntity<Map<String, Object>> getSupportedModels() {
        Map<String, Object> supportedModels = Map.of(
            "local_models", new String[]{
                "gpt-3.5-turbo", "gpt-4", "gpt-4-turbo", 
                "text-davinci-003", "claude-3-haiku", 
                "claude-3-sonnet", "claude-3-opus"
            },
            "engine_mapping", Map.of(
                "vllm", "gpt-3.5-turbo",
                "sglang", "gpt-4",
                "openai", "gpt-3.5-turbo",
                "anthropic", "claude-3-sonnet"
            ),
            "cost_info", Map.of(
                "gpt-3.5-turbo", Map.of("input", 0.0015, "output", 0.002),
                "gpt-4", Map.of("input", 0.03, "output", 0.06),
                "claude-3-sonnet", Map.of("input", 0.003, "output", 0.015)
            )
        );
        
        return ResponseEntity.ok(supportedModels);
    }
    
    /**
     * 텍스트의 기본 통계 정보를 반환합니다 (토큰 계산 없이)
     * 
     * @param text 분석할 텍스트
     * @return 텍스트 통계 정보
     */
    @PostMapping("/text-stats")
    public ResponseEntity<Map<String, Object>> getTextStats(@RequestBody String text) {
        if (text == null) {
            text = "";
        }
        
        Map<String, Object> stats = Map.of(
            "character_count", text.length(),
            "word_count", text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length,
            "line_count", text.split("\n").length,
            "estimated_tokens_simple", estimateTokensSimple(text),
            "estimated_reading_time_minutes", Math.max(1, text.split("\\s+").length / 200) // 평균 200단어/분
        );
        
        log.debug("텍스트 통계: chars={}, words={}, estimated_tokens={}", 
                stats.get("character_count"), stats.get("word_count"), stats.get("estimated_tokens_simple"));
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 간단한 토큰 수 추정 (정확한 계산 없이)
     * 일반적으로 영어는 4글자당 1토큰, 한국어는 2-3글자당 1토큰
     */
    private int estimateTokensSimple(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // 영어와 한국어를 구분하여 추정
        long englishChars = text.chars().filter(c -> c < 128).count();
        long koreanChars = text.length() - englishChars;
        
        // 영어: 4글자당 1토큰, 한국어: 2.5글자당 1토큰
        return (int) Math.ceil(englishChars / 4.0 + koreanChars / 2.5);
    }
}