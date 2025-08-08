package com.example.simple.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "llm")
@Data
public class LLMConfig {
    
    private VllmConfig vllm = new VllmConfig();
    private SglangConfig sglang = new SglangConfig();
    
    @Data
    public static class VllmConfig {
        private boolean enabled = true;
        private String baseUrl = "http://localhost:8000";
        private Duration timeout = Duration.ofSeconds(30);
        private int maxTokens = 512;
        private double temperature = 0.7;
    }
    
    @Data
    public static class SglangConfig {
        private boolean enabled = true;
        private String baseUrl = "http://localhost:30000";
        private Duration timeout = Duration.ofSeconds(30);
        private int maxTokens = 512;
        private double temperature = 0.7;
    }
}