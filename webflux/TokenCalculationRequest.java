package com.example.simple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 토큰 계산 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenCalculationRequest {
    
    /**
     * 토큰 수를 계산할 텍스트
     */
    @NotBlank(message = "텍스트는 필수입니다")
    @Size(max = 100000, message = "텍스트는 100,000자를 초과할 수 없습니다")
    private String text;
    
    /**
     * 사용할 모델명 (기본값: gpt-3.5-turbo)
     */
    @Builder.Default
    private String model = "gpt-3.5-turbo";
    
    /**
     * 외부 API 사용 여부 (기본값: false - 로컬 처리)
     */
    @Builder.Default
    private boolean useExternal = false;
    
    /**
     * 예상 출력 토큰 수 (응답 길이 예측용)
     */
    private Integer expectedOutputTokens;
    
    /**
     * 상세 분석 여부 (토큰 밀도, 비용 등 추가 정보 포함)
     */
    @Builder.Default
    private boolean includeDetailedAnalysis = false;
}

/**
 * 배치 토큰 계산 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TokenBatchRequest {
    
    /**
     * 토큰 수를 계산할 텍스트 리스트
     */
    @Size(max = 100, message = "한 번에 최대 100개의 텍스트까지 처리 가능합니다")
    private List<@NotBlank String> texts;
    
    /**
     * 사용할 모델명
     */
    @Builder.Default
    private String model = "gpt-3.5-turbo";
    
    /**
     * 외부 API 사용 여부
     */
    @Builder.Default
    private boolean useExternal = false;
    
    /**
     * 병렬 처리 수준 (1-10)
     */
    @Builder.Default
    private int parallelism = 5;
}

/**
 * LLM 요청의 토큰 추정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TokenEstimationRequest {
    
    /**
     * LLM 요청 정보
     */
    private LLMRequest llmRequest;
    
    /**
     * 상세 비용 분석 포함 여부
     */
    @Builder.Default
    private boolean includeCostBreakdown = false;
    
    /**
     * 토큰 사용량 히스토리 포함 여부
     */
    @Builder.Default
    private boolean includeUsageHistory = false;
}