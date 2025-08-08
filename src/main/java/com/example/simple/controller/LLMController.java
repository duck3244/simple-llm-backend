package com.example.simple.controller;

import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponse;
import com.example.simple.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class LLMController {
    
    private final LLMService llmService;
    
    @PostMapping("/generate")
    public ResponseEntity<LLMResponse> generate(@Valid @RequestBody LLMRequest request) {
        // 프롬프트 로깅 (보안을 위해 일부만 표시)
        String promptPreview = request.getPrompt().length() > 50 ? 
            request.getPrompt().substring(0, 50) + "..." : request.getPrompt();
        
        log.info("추론 요청: engine={}, prompt=[{}], maxTokens={}, temperature={}", 
                request.getEngine(), promptPreview, request.getMaxTokens(), request.getTemperature());
        
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
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> info() {
        return ResponseEntity.ok("Simple LLM Backend - Java 11, Spring Boot 2.3.2");
    }
}