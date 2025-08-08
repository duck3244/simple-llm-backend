package com.example.simple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 토큰 계산 결과 정보를 담는 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {
    
    /**
     * 분석된 텍스트 (길면 일부만 표시)
     */
    private String text;
    
    /**
     * 사용된 모델명
     */
    private String model;
    
    /**
     * 입력 텍스트의 토큰 수
     */
    private int inputTokens;
    
    /**
     * 예상 출력 토큰 수
     */
    private int outputTokens;
    
    /**
     * 총 토큰 수 (입력 + 출력)
     */
    private int totalTokens;
    
    /**
     * 예상 비용 (USD)
     */
    private double estimatedCost;
    
    /**
     * 토큰 계산 처리 시간 (밀리초)
     */
    private long processingTimeMs;
    
    /**
     * 사용된 토큰화 방법
     */
    private TokenizationMethod method;
    
    /**
     * 계산 시각
     */
    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();
    
    /**
     * 토큰화 방법 열거형
     */
    public enum TokenizationMethod {
        /**
         * 로컬 Tiktoken 라이브러리 사용
         */
        LOCAL_TIKTOKEN("Local Tiktoken Library"),
        
        /**
         * Hugging Face API 사용
         */
        HUGGINGFACE_API("Hugging Face Tokenizer API"),
        
        /**
         * OpenAI API 사용
         */
        OPENAI_API("OpenAI Tokenizer API"),
        
        /**
         * SentencePiece 라이브러리 사용
         */
        SENTENCEPIECE("SentencePiece Library"),
        
        /**
         * 간단한 공백 기반 추정
         */
        SIMPLE_ESTIMATION("Simple Word Count Estimation");
        
        private final String description;
        
        TokenizationMethod(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 토큰당 평균 문자 수 계산
     */
    public double getAverageCharsPerToken() {
        if (inputTokens == 0) return 0.0;
        return (double) text.length() / inputTokens;
    }
    
    /**
     * 토큰 밀도 계산 (토큰/문자)
     */
    public double getTokenDensity() {
        if (text.length() == 0) return 0.0;
        return (double) inputTokens / text.length();
    }
    
    /**
     * 비용 효율성 계산 (문자당 비용)
     */
    public double getCostPerCharacter() {
        if (text.length() == 0) return 0.0;
        return estimatedCost / text.length();
    }
}