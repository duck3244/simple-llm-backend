package com.example.simple.exception;

import com.example.simple.dto.LLMResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<LLMResponse> handleException(Exception e) {
        log.error("예상하지 못한 오류", e);
        
        LLMResponse response = LLMResponse.builder()
                .success(false)
                .error("서버 오류: " + e.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}