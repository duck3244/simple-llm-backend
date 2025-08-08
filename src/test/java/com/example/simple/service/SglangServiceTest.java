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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SglangServiceTest {

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
    private SglangService sglangService;

    private LLMConfig.SglangConfig sglangConfig;
    private LLMRequest testRequest;

    @BeforeEach
    void setUp() {
        sglangConfig = new LLMConfig.SglangConfig();
        sglangConfig.setEnabled(true);
        sglangConfig.setBaseUrl("http://localhost:30000");
        sglangConfig.setTimeout(30);
        sglangConfig.setMaxTokens(512);
        sglangConfig.setTemperature(0.7);

        testRequest = new LLMRequest();
        testRequest.setPrompt("What is artificial intelligence?");
        testRequest.setMaxTokens(200);
        testRequest.setTemperature(0.5);

        when(llmConfig.getSglang()).thenReturn(sglangConfig);
    }

    @Test
    void generate_WithValidRequest_ShouldReturnSuccessResponse() throws Exception {
        // Given
        long startTime = System.currentTimeMillis();
        String mockJsonResponse = """
                {
                    "text": "Artificial intelligence is a field of computer science..."
                }
                """;

        setupWebClientMocks(mockJsonResponse);

        // When
        LLMResponse response = sglangService.generate(testRequest, startTime);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Artificial intelligence is a field of computer science...", response.getText());
        assertEquals("sglang", response.getEngine());
        assertTrue(response.getResponseTimeMs() >= 0);
    }

    @Test
    void generate_WhenSglangDisabled_ShouldThrowException() {
        // Given
        sglangConfig.setEnabled(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            sglangService.generate(testRequest, System.currentTimeMillis());
        });

        assertTrue(exception.getMessage().contains("SGLang이 비활성화되어 있습니다"));
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
            sglangService.generate(testRequest, startTime);
        });

        assertTrue(exception.getMessage().contains("SGLang 호출 실패"));
    }

    private void setupWebClientMocks(String mockResponse) {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(mockResponse));
    }
}