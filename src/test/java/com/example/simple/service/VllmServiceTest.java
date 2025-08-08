package com.example.simple.service;

import com.example.simple.config.LLMConfig;
import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VllmServiceTest {

    @Mock
    private LLMConfig llmConfig;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private VllmService vllmService;

    private LLMConfig.VllmConfig vllmConfig;
    private LLMRequest testRequest;

    @BeforeEach
    void setUp() {
        vllmConfig = new LLMConfig.VllmConfig();
        vllmConfig.setEnabled(true);
        vllmConfig.setBaseUrl("http://localhost:8000");
        vllmConfig.setTimeout(30);
        vllmConfig.setMaxTokens(512);
        vllmConfig.setTemperature(0.7);

        testRequest = new LLMRequest();
        testRequest.setPrompt("Hello, world!");
        testRequest.setMaxTokens(100);
        testRequest.setTemperature(0.5);

        when(llmConfig.getVllm()).thenReturn(vllmConfig);
    }

    @Test
    void generate_WithValidRequest_ShouldReturnSuccessResponse() throws Exception {
        // Given
        long startTime = System.currentTimeMillis();
        String mockJsonResponse = """
                {
                    "choices": [
                        {
                            "text": "Hello! How can I help you today?"
                        }
                    ]
                }
                """;

        setupWebClientMocks(mockJsonResponse);

        // When
        LLMResponse response = vllmService.generate(testRequest, startTime);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Hello! How can I help you today?", response.getText());
        assertEquals("vllm", response.getEngine());
        assertTrue(response.getResponseTimeMs() >= 0);
    }

    @Test
    void generate_WhenVllmDisabled_ShouldThrowException() {
        // Given
        vllmConfig.setEnabled(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vllmService.generate(testRequest, System.currentTimeMillis());
        });

        assertTrue(exception.getMessage().contains("vLLM이 비활성화되어 있습니다"));
    }

    @Test
    void generate_WithWebClientError_ShouldThrowException() {
        // Given
        long startTime = System.currentTimeMillis();
        
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Connection failed")));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vllmService.generate(testRequest, startTime);
        });

        assertTrue(exception.getMessage().contains("vLLM 호출 실패"));
    }

    @Test
    void generate_WithTimeout_ShouldThrowException() {
        // Given
        long startTime = System.currentTimeMillis();
        
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.delay(Duration.ofSeconds(35)).then(Mono.just("response")));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vllmService.generate(testRequest, startTime);
        });

        assertTrue(exception.getMessage().contains("vLLM 호출 실패"));
    }

    private void setupWebClientMocks(String mockResponse) throws Exception {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(mockResponse));

        // ObjectMapper mock 설정은 실제 구현에 따라 조정 필요
        // 여기서는 간단하게 extractText 메서드가 정상 동작한다고 가정
    }
}