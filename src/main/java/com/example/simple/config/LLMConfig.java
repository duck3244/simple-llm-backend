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
    private FailoverConfig failover = new FailoverConfig();
    
    @Data
    public static class VllmConfig {
        private boolean enabled = true;
        private String baseUrl = "http://localhost:8000";
        private Duration timeout = Duration.ofSeconds(30);
        private int maxTokens = 512;
        private double temperature = 0.7;
        
        // 편의 메서드: 초 단위로 timeout 설정
        public void setTimeout(int seconds) {
            this.timeout = Duration.ofSeconds(seconds);
        }
        
        // 편의 메서드: timeout을 초 단위로 반환
        public int getTimeoutSeconds() {
            return (int) this.timeout.getSeconds();
        }
        
        // String으로 timeout 설정 (예: "30s", "1m")
        public void setTimeout(String timeoutStr) {
            this.timeout = Duration.parse("PT" + timeoutStr.toUpperCase()
                .replace("S", "S")
                .replace("M", "M")
                .replace("H", "H"));
        }
    }
    
    @Data
    public static class SglangConfig {
        private boolean enabled = true;
        private String baseUrl = "http://localhost:30000";
        private Duration timeout = Duration.ofSeconds(30);
        private int maxTokens = 512;
        private double temperature = 0.7;
        
        // 편의 메서드: 초 단위로 timeout 설정
        public void setTimeout(int seconds) {
            this.timeout = Duration.ofSeconds(seconds);
        }
        
        // 편의 메서드: timeout을 초 단위로 반환
        public int getTimeoutSeconds() {
            return (int) this.timeout.getSeconds();
        }
        
        // String으로 timeout 설정
        public void setTimeout(String timeoutStr) {
            this.timeout = Duration.parse("PT" + timeoutStr.toUpperCase()
                .replace("S", "S")
                .replace("M", "M")
                .replace("H", "H"));
        }
    }
    
    @Data
    public static class FailoverConfig {
        private boolean enabled = false;
        private int retryAttempts = 3;
        private Duration retryDelay = Duration.ofSeconds(1);
        
        // 편의 메서드: 초 단위로 retryDelay 설정
        public void setRetryDelay(int seconds) {
            this.retryDelay = Duration.ofSeconds(seconds);
        }
        
        // 편의 메서드: retryDelay를 초 단위로 반환
        public int getRetryDelaySeconds() {
            return (int) this.retryDelay.getSeconds();
        }
        
        // String으로 retryDelay 설정
        public void setRetryDelay(String delayStr) {
            this.retryDelay = Duration.parse("PT" + delayStr.toUpperCase()
                .replace("S", "S")
                .replace("M", "M")
                .replace("H", "H"));
        }
    }
}