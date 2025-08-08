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
public class VllmService {
    
    private final LLMConfig llmConfig;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    
    public LLMResponse generate(LLMRequest request, long startTime) {
        if (!llmConfig.getVllm().isEnabled()) {
            throw new RuntimeException("vLLM이 비활성화되어 있습니다");
        }
        
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(llmConfig.getVllm().getBaseUrl())
                    .build();
            
            Map<String, Object> requestBody = createRequestBody(request);
            
            log.debug("vLLM 요청: URL={}, Body={}", llmConfig.getVllm().getBaseUrl(), requestBody);
            
            String response = webClient
                    .post()
                    .uri("/v1/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(llmConfig.getVllm().getTimeout())
                    .block();
            
            String text = extractText(response);
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.debug("vLLM 응답 성공: responseTime={}ms, textLength={}", responseTime, text.length());
            
            return LLMResponse.builder()
                    .text(text)
                    .engine("vllm")
                    .responseTimeMs(responseTime)
                    .success(true)
                    .build();
                    
        } catch (WebClientException e) {
            log.error("vLLM 네트워크 오류: {}", e.getMessage());
            throw new RuntimeException("vLLM 네트워크 오류: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("vLLM 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("vLLM 호출 실패: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> createRequestBody(LLMRequest request) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "default");
        requestBody.put("prompt", request.getPrompt());
        requestBody.put("max_tokens", request.getMaxTokens() != null ? 
                request.getMaxTokens() : llmConfig.getVllm().getMaxTokens());
        requestBody.put("temperature", request.getTemperature() != null ? 
                request.getTemperature() : llmConfig.getVllm().getTemperature());
        requestBody.put("stream", false);
        
        return requestBody;
    }
    
    private String extractText(String response) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode choices = jsonNode.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode text = choices.get(0).get("text");
                if (text != null) {
                    return text.asText().trim();
                }
            }
            
            // choices가 없거나 비어있는 경우, 다른 형식 시도
            JsonNode textNode = jsonNode.get("text");
            if (textNode != null) {
                return textNode.asText().trim();
            }
            
            throw new RuntimeException("응답에서 텍스트를 추출할 수 없습니다. Response: " + response);
            
        } catch (Exception e) {
            log.error("JSON 파싱 실패: response={}", response);
            throw new RuntimeException("응답 파싱 실패: " + e.getMessage(), e);
        }
    }
}