package com.example.simple.service;

import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LLMServiceTest {

    @Mock
    private VllmService vllmService;

    @Mock
    private SglangService sglangService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private LLMService llmService;

    private LLMRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = new LLMRequest();
        testRequest.setPrompt("Test prompt");
        testRequest.setEngine("vllm");
        testRequest.setMaxTokens(100);
        testRequest.setTemperature(0.7);
    }

    @Test
    void generateResponse_WithVllmEngine_ShouldCallVllmService() {
        // Given
        testRequest.setEngine("vllm");
        LLMResponse expectedResponse = LLMResponse.builder()
                .text("Test response")
                .engine("vllm")
                .success(true)
                .responseTimeMs(1000L)
                .build();

        when(vllmService.generate(eq(testRequest), anyLong())).thenReturn(expectedResponse);

        // When
        LLMResponse actualResponse = llmService.generateResponse(testRequest);

        // Then
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("Test response", actualResponse.getText());
        assertEquals("vllm", actualResponse.getEngine());
        verify(vllmService, times(1)).generate(eq(testRequest), anyLong());
        verify(sglangService, never()).generate(any(), anyLong());
    }

    @Test
    void generateResponse_WithSglangEngine_ShouldCallSglangService() {
        // Given
        testRequest.setEngine("sglang");
        LLMResponse expectedResponse = LLMResponse.builder()
                .text("Test response")
                .engine("sglang")
                .success(true)
                .responseTimeMs(1200L)
                .build();

        when(sglangService.generate(eq(testRequest), anyLong())).thenReturn(expectedResponse);

        // When
        LLMResponse actualResponse = llmService.generateResponse(testRequest);

        // Then
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("Test response", actualResponse.getText());
        assertEquals("sglang", actualResponse.getEngine());
        verify(sglangService, times(1)).generate(eq(testRequest), anyLong());
        verify(vllmService, never()).generate(any(), anyLong());
    }

    @Test
    void generateResponse_WithUnknownEngine_ShouldDefaultToVllm() {
        // Given
        testRequest.setEngine("unknown");
        LLMResponse expectedResponse = LLMResponse.builder()
                .text("Default response")
                .engine("vllm")
                .success(true)
                .responseTimeMs(800L)
                .build();

        when(vllmService.generate(eq(testRequest), anyLong())).thenReturn(expectedResponse);

        // When
        LLMResponse actualResponse = llmService.generateResponse(testRequest);

        // Then
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("Default response", actualResponse.getText());
        verify(vllmService, times(1)).generate(eq(testRequest), anyLong());
    }

    @Test
    void generateResponse_WithServiceException_ShouldReturnErrorResponse() {
        // Given
        testRequest.setEngine("vllm");
        when(vllmService.generate(eq(testRequest), anyLong()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When
        LLMResponse actualResponse = llmService.generateResponse(testRequest);

        // Then
        assertNotNull(actualResponse);
        assertFalse(actualResponse.isSuccess());
        assertTrue(actualResponse.getError().contains("추론 실패"));
        assertTrue(actualResponse.getError().contains("Service unavailable"));
        assertTrue(actualResponse.getResponseTimeMs() > 0);
    }

    @Test
    void generateResponse_WithLoggingService_ShouldLogSuccessfulRequest() {
        // Given
        testRequest.setEngine("vllm");
        LLMResponse successResponse = LLMResponse.builder()
                .text("Success response")
                .engine("vllm")
                .success(true)
                .responseTimeMs(1000L)
                .build();

        when(vllmService.generate(eq(testRequest), anyLong())).thenReturn(successResponse);

        // When
        llmService.generateResponse(testRequest);

        // Then
        verify(loggingService, times(1)).logLLMRequest(
                eq("vllm"),
                eq("Test prompt"),
                eq("Success response"),
                eq(1000L),
                eq(true)
        );
    }

    @Test
    void generateResponse_WithLoggingService_ShouldLogFailedRequest() {
        // Given
        testRequest.setEngine("vllm");
        when(vllmService.generate(eq(testRequest), anyLong()))
                .thenThrow(new RuntimeException("Service error"));

        // When
        llmService.generateResponse(testRequest);

        // Then
        verify(loggingService, times(1)).logLLMRequest(
                eq("vllm"),
                eq("Test prompt"),
                eq(null),
                anyLong(),
                eq(false)
        );
    }
}