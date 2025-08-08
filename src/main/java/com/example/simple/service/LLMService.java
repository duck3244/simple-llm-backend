package com.example.simple.service;

import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {
    
    private final VllmService vllmService;
    private final SglangService sglangService;
    
    public LLMResponse generateResponse(LLMRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            String engine = request.getEngine();
            
            if ("vllm".equalsIgnoreCase(engine)) {
                return vllmService.generate(request, startTime);
            } else if ("sglang".equalsIgnoreCase(engine)) {
                return sglangService.generate(request, startTime);
            } else {
                // 기본적으로 vLLM 사용
                return vllmService.generate(request, startTime);
            }
            
        } catch (Exception e) {
            log.error("LLM 추론 실패", e);
            return LLMResponse.builder()
                    .success(false)
                    .error("추론 실패: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
}