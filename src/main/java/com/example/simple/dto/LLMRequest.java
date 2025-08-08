package com.example.simple.dto;

import lombok.Data;

@Data
public class LLMRequest {
    private String prompt;
    private String engine; // "vllm" 또는 "sglang"
    private Integer maxTokens;
    private Double temperature;
}