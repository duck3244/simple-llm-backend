package com.example.simple.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

@Data
public class LLMRequest {
    
    @NotBlank(message = "프롬프트는 필수입니다")
    private String prompt;
    
    private String engine = "vllm"; // 기본값: vllm
    
    @Min(value = 1, message = "최대 토큰 수는 1 이상이어야 합니다")
    @Max(value = 4096, message = "최대 토큰 수는 4096 이하여야 합니다")
    private Integer maxTokens;
    
    @DecimalMin(value = "0.0", message = "Temperature는 0.0 이상이어야 합니다")
    @DecimalMax(value = "2.0", message = "Temperature는 2.0 이하여야 합니다")
    private Double temperature;
}