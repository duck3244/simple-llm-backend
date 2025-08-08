// controller/HealthController.java
package com.example.simple.controller;

import com.example.simple.config.LLMConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    @Autowired
    private LLMConfig llmConfig;
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
    
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // 애플리케이션 상태
        health.put("application", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        // vLLM 상태 확인
        if (llmConfig.getVllm().isEnabled()) {
            boolean vllmHealthy = checkEngineHealth(llmConfig.getVllm().getBaseUrl(), "/health");
            health.put("vllm", vllmHealthy ? "UP" : "DOWN");
            health.put("vllm_url", llmConfig.getVllm().getBaseUrl());
        } else {
            health.put("vllm", "DISABLED");
        }
        
        // SGLang 상태 확인
        if (llmConfig.getSglang().isEnabled()) {
            boolean sglangHealthy = checkEngineHealth(llmConfig.getSglang().getBaseUrl(), "/health");
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
            logger.debug("Engine health check failed for {}: {}", baseUrl, e.getMessage());
            return false;
        }
    }
}