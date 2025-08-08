package com.example.simple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 정보가 포함된 LLM 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMResponseWithTokens {
    
    /**
     * 기본 LLM 응답
     */
    private LLMResponse llmResponse;
    
    /**
     * 요청 토큰 정보 (입력 프롬프트)
     */
    private TokenInfo requestTokenInfo;
    
    /**
     * 응답 토큰 정보 (생성된 텍스트)
     */
    private TokenInfo responseTokenInfo;
    
    /**
     * 총 토큰 사용량 정보
     */
    private TokenUsageSummary tokenUsage;
    
    /**
     * 토큰 사용량 요약 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsageSummary {
        
        /**
         * 총 입력 토큰 수
         */
        private int totalInputTokens;
        
        /**
         * 총 출력 토큰 수
         */
        private int totalOutputTokens;
        
        /**
         * 총 토큰 수
         */
        private int totalTokens;
        
        /**
         * 총 비용 (USD)
         */
        private double totalCost;
        
        /**
         * 입력 토큰 비용
         */
        private double inputTokenCost;
        
        /**
         * 출력 토큰 비용
         */
        private double outputTokenCost;
        
        /**
         * 토큰 계산 총 처리 시간 (밀리초)
         */
        private long totalProcessingTimeMs;
        
        /**
         * 사용된 모델명
         */
        private String model;
        
        /**
         * 토큰 효율성 점수 (0-100)
         */
        private double efficiencyScore;
        
        /**
         * 비용 효율성 계산
         */
        public double getCostEfficiency() {
            if (totalTokens == 0) return 0.0;
            return totalCost / totalTokens * 1000; // 1K 토큰당 비용
        }
        
        /**
         * 압축률 계산 (출력/입력 토큰 비율)
         */
        public double getCompressionRatio() {
            if (totalInputTokens == 0) return 0.0;
            return (double) totalOutputTokens / totalInputTokens;
        }
    }
}