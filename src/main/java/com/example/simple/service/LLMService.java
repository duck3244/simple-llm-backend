package com.example.simple.service;

import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {
    
    private final VllmService vllmService;
    private final SglangService sglangService;
    
    @Autowired(required = false)
    private LoggingService loggingService;
    
    public LLMResponse generateResponse(LLMRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 요청 검증
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return createErrorResponse("프롬프트가 비어있습니다", startTime);
        }
        
        try {
            String engine = request.getEngine();
            LLMResponse response;
            
            if ("vllm".equalsIgnoreCase(engine)) {
                response = vllmService.generate(request, startTime);
            } else if ("sglang".equalsIgnoreCase(engine)) {
                response = sglangService.generate(request, startTime);
            } else {
                // 기본적으로 vLLM 사용
                log.info("Unknown engine '{}', defaulting to vLLM", engine);
                response = vllmService.generate(request, startTime);
            }
            
            // 성공 로깅
            if (loggingService != null) {
                loggingService.logLLMRequest(
                    response.getEngine(),
                    request.getPrompt(),
                    response.getText(),
                    response.getResponseTimeMs(),
                    response.isSuccess()
                );
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("LLM 추론 실패: engine={}, error={}", request.getEngine(), e.getMessage(), e);
            
            // 실패 로깅
            if (loggingService != null) {
                loggingService.logLLMRequest(
                    request.getEngine(),
                    request.getPrompt(),
                    null,
                    System.currentTimeMillis() - startTime,
                    false
                );
            }
            
            return createErrorResponse("추론 실패: " + e.getMessage(), startTime);
        }
    }
    
    private LLMResponse createErrorResponse(String error, long startTime) {
        return LLMResponse.builder()
                .success(false)
                .error(error)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }
}