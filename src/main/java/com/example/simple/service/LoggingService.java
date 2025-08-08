package com.example.simple.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.datasource.url", matchIfMissing = false)
public class LoggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    public void logLLMRequest(String engine, String prompt, String response, long responseTime, boolean success) {
        if (jdbcTemplate == null) {
            return;
        }
        
        try {
            jdbcTemplate.update(
                "INSERT INTO LLM_LOGS (ENGINE, PROMPT, RESPONSE, RESPONSE_TIME_MS, SUCCESS) VALUES (?, ?, ?, ?, ?)",
                engine, prompt, response, responseTime, success ? 1 : 0
            );
        } catch (Exception e) {
            logger.warn("로그 저장 실패", e);
        }
    }
}