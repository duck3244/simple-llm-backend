package com.example.simple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMResponse {
    private String text;
    private String engine;
    private long responseTimeMs;
    private boolean success;
    private String error;
}