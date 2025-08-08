package com.example.simple.controller;

import com.example.simple.config.LLMConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LLMConfig llmConfig;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @Test
    void healthEndpoint_ShouldReturnOK() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void detailedHealthEndpoint_ShouldReturnHealthStatus() throws Exception {
        // Given
        LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();
        vllmConfig.setEnabled(true);
        vllmConfig.setBaseUrl("http://localhost:8000");

        LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();
        sglangConfig.setEnabled(true);
        sglangConfig.setBaseUrl("http://localhost:30000");

        when(llmConfig.getVllm()).thenReturn(vllmConfig);
        when(llmConfig.getSglang()).thenReturn(sglangConfig);

        // When & Then
        mockMvc.perform(get("/api/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("UP"))
                .andExpect(jsonPath("$.vllm_url").value("http://localhost:8000"))
                .andExpect(jsonPath("$.sglang_url").value("http://localhost:30000"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}