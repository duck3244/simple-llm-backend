package com.example.simple.controller;

import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponse;
import com.example.simple.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class LLMController {
    
    private final LLMService llmService;
    
    @PostMapping("/generate")
    public ResponseEntity<LLMResponse> generate(@RequestBody LLMRequest request) {
        log.info("추론 요청: engine={}, prompt={}", request.getEngine(), 
                request.getPrompt().substring(0, Math.min(50, request.getPrompt().length())));
        
        LLMResponse response = llmService.generateResponse(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}