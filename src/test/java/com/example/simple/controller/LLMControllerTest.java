package com.example.simple.controller;

import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponse;
import com.example.simple.service.LLMService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LLMController.class)
class LLMControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LLMService llmService;

    @Autowired
    private ObjectMapper objectMapper;

    private LLMRequest testRequest;
    private LLMResponse testResponse;

    @BeforeEach
    void setUp() {
        // 테스트용 요청 데이터 설정
        testRequest = new LLMRequest();
        testRequest.setPrompt("Hello, world!");
        testRequest.setEngine("vllm");
        testRequest.setMaxTokens(100);
        testRequest.setTemperature(0.7);

        // 테스트용 응답 데이터 설정
        testResponse = LLMResponse.builder()
                .text("Hello! How can I help you today?")
                .engine("vllm")
                .responseTimeMs(1500L)
                .success(true)
                .build();
    }

    @Test
    void healthEndpoint_ShouldReturnOK() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void infoEndpoint_ShouldReturnApplicationInfo() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(content().string("Simple LLM Backend - Java 11, Spring Boot 2.3.2"));
    }

    @Test
    void generateEndpoint_WithValidRequest_ShouldReturnSuccessResponse() throws Exception {
        // Given
        when(llmService.generateResponse(any(LLMRequest.class))).thenReturn(testResponse);

        // When & Then
        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.text").value("Hello! How can I help you today?"))
                .andExpect(jsonPath("$.engine").value("vllm"))
                .andExpect(jsonPath("$.response_time_ms").value(1500));
    }

    @Test
    void generateEndpoint_WithServiceError_ShouldReturnErrorResponse() throws Exception {
        // Given
        LLMResponse errorResponse = LLMResponse.builder()
                .success(false)
                .error("Service unavailable")
                .responseTimeMs(100L)
                .build();

        when(llmService.generateResponse(any(LLMRequest.class))).thenReturn(errorResponse);

        // When & Then
        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpected(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Service unavailable"));
    }

    @Test
    void generateEndpoint_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - 빈 프롬프트로 잘못된 요청 생성
        LLMRequest invalidRequest = new LLMRequest();
        invalidRequest.setPrompt("");  // 빈 프롬프트
        invalidRequest.setEngine("vllm");

        // When & Then
        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateEndpoint_WithInvalidJSON_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }
}