package com.example.simple.service;

import com.example.simple.config.LLMConfig;
import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SglangService {
    
    private final LLMConfig llmConfig;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    
    public LLMResponse generate(LLMRequest request, long startTime) {
        if (!llmConfig.getSglang().isEnabled()) {
            throw new RuntimeException("SGLang이 비활성화되어 있습니다");
        }
        
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(llmConfig.getSglang().getBaseUrl())
                    .build();
            
            Map<String, Object> requestBody = createRequestBody(request);
            
            log.debug("SGLang 요청: URL={}, Body={}", llmConfig.getSglang().getBaseUrl(), requestBody);
            
            String response = webClient
                    .post()
                    .uri("/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(llmConfig.getSglang().getTimeout())
                    .block();
            
            String text = extractText(response);
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.debug("SGLang 응답 성공: responseTime={}ms, textLength={}", responseTime, text.length());
            
            return LLMResponse.builder()
                    .text(text)
                    .engine("sglang")
                    .responseTimeMs(responseTime)
                    .success(true)
                    .build();
                    
        } catch (WebClientException e) {
            log.error("SGLang 네트워크 오류: {}", e.getMessage());
            throw new RuntimeException("SGLang 네트워크 오류: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("SGLang 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("SGLang 호출 실패: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> createRequestBody(LLMRequest request) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", request.getPrompt());
        
        Map<String, Object> samplingParams = new HashMap<>();
        samplingParams.put("max_new_tokens", request.getMaxTokens() != null ? 
                request.getMaxTokens() : llmConfig.getSglang().getMaxTokens());
        samplingParams.put("temperature", request.getTemperature() != null ? 
                request.getTemperature() : llmConfig.getSglang().getTemperature());
        samplingParams.put("top_p", 0.9);
        samplingParams.put("stream", false);
        
        requestBody.put("sampling_params", samplingParams);
        
        return requestBody;
    }
    
    private String extractText(String response) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // SGLang 표준 응답 형식
            JsonNode text = jsonNode.get("text");
            if (text != null) {
                return text.asText().trim();
            }
            
            // 다른 가능한 형식들
            JsonNode output = jsonNode.get("output");
            if (output != null) {
                return output.asText().trim();
            }
            
            JsonNode choices = jsonNode.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode choiceText = choices.get(0).get("text");
                if (choiceText != null) {
                    return choiceText.asText().trim();
                }
            }
            
            throw new RuntimeException("응답에서 텍스트를 추출할 수 없습니다. Response: " + response);
            
        } catch (Exception e) {
            log.error("JSON 파싱 실패: response={}", response);
            throw new RuntimeException("응답 파싱 실패: " + e.getMessage(), e);
        }
    }
}