package com.example.simple.controller;

import com.example.simple.config.LLMConfig;
import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponse;
import com.example.simple.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.Valid;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * README.md와 일치하는 API 엔드포인트를 제공하는 통합 컨트롤러
 * 
 * 엔드포인트:
 * - GET /api/health - 기본 헬스체크
 * - GET /api/health/detailed - 상세 헬스체크  
 * - GET /api/info - 애플리케이션 정보
 * - POST /api/generate - LLM 추론 요청
 * - GET /api/stats - 통계 정보 (Oracle DB 사용시)
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ApiController {
    
    private final LLMService llmService;
    private final LLMConfig llmConfig;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${spring.application.name:Simple LLM Backend}")
    private String applicationName;
    
    @Value("${info.app.version:0.0.1-SNAPSHOT}")
    private String applicationVersion;
    
    // ===========================================
    // 헬스체크 엔드포인트
    // ===========================================
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
    
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // 애플리케이션 상태
        health.put("application", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        // vLLM 상태 확인
        if (llmConfig.getVllm().isEnabled()) {
            boolean vllmHealthy = checkEngineHealth(
                llmConfig.getVllm().getBaseUrl(), 
                "/health"
            );
            health.put("vllm", vllmHealthy ? "UP" : "DOWN");
            health.put("vllm_url", llmConfig.getVllm().getBaseUrl());
        } else {
            health.put("vllm", "DISABLED");
        }
        
        // SGLang 상태 확인
        if (llmConfig.getSglang().isEnabled()) {
            boolean sglangHealthy = checkEngineHealth(
                llmConfig.getSglang().getBaseUrl(), 
                "/health"
            );
            health.put("sglang", sglangHealthy ? "UP" : "DOWN");
            health.put("sglang_url", llmConfig.getSglang().getBaseUrl());
        } else {
            health.put("sglang", "DISABLED");
        }
        
        return ResponseEntity.ok(health);
    }
    
    private boolean checkEngineHealth(String baseUrl, String healthPath) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
            
            String response = webClient
                    .get()
                    .uri(healthPath)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            return response != null;
            
        } catch (Exception e) {
            log.debug("Engine health check failed for {}: {}", baseUrl, e.getMessage());
            return false;
        }
    }
    
    // ===========================================
    // 애플리케이션 정보 엔드포인트
    // ===========================================
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        
        // 기본 애플리케이션 정보
        info.put("name", applicationName);
        info.put("version", applicationVersion);
        info.put("description", "Simple LLM Backend - Java 11, Spring Boot 2.3.2");
        
        // 시스템 정보
        Map<String, Object> system = new HashMap<>();
        system.put("java_version", System.getProperty("java.version"));
        system.put("java_vendor", System.getProperty("java.vendor"));
        system.put("os_name", System.getProperty("os.name"));
        system.put("os_version", System.getProperty("os.version"));
        system.put("os_arch", System.getProperty("os.arch"));
        info.put("system", system);
        
        // LLM 엔진 정보
        Map<String, Object> engines = new HashMap<>();
        engines.put("vllm_enabled", llmConfig.getVllm().isEnabled());
        engines.put("vllm_url", llmConfig.getVllm().getBaseUrl());
        engines.put("sglang_enabled", llmConfig.getSglang().isEnabled());
        engines.put("sglang_url", llmConfig.getSglang().getBaseUrl());
        info.put("engines", engines);
        
        // 빌드 정보
        Map<String, Object> build = new HashMap<>();
        build.put("spring_boot_version", "2.3.2.RELEASE");
        build.put("build_time", System.currentTimeMillis());
        info.put("build", build);
        
        return ResponseEntity.ok(info);
    }
    
    // ===========================================
    // LLM 추론 엔드포인트
    // ===========================================
    
    @PostMapping("/generate")
    public ResponseEntity<LLMResponse> generate(@Valid @RequestBody LLMRequest request) {
        // 프롬프트 로깅 (보안을 위해 일부만 표시)
        String promptPreview = request.getPrompt().length() > 50 ? 
            request.getPrompt().substring(0, 50) + "..." : request.getPrompt();
        
        log.info("추론 요청: engine={}, prompt=[{}], maxTokens={}, temperature={}", 
                request.getEngine(), promptPreview, request.getMaxTokens(), request.getTemperature());
        
        try {
            LLMResponse response = llmService.generateResponse(request);
            
            if (response.isSuccess()) {
                log.info("추론 성공: engine={}, responseTime={}ms, textLength={}", 
                        response.getEngine(), response.getResponseTimeMs(), 
                        response.getText() != null ? response.getText().length() : 0);
                return ResponseEntity.ok(response);
            } else {
                log.warn("추론 실패: engine={}, error={}", request.getEngine(), response.getError());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("추론 중 예외 발생: engine={}, error={}", request.getEngine(), e.getMessage(), e);
            
            LLMResponse errorResponse = LLMResponse.builder()
                    .success(false)
                    .error("서버 오류: " + e.getMessage())
                    .responseTimeMs(0L)
                    .build();
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}