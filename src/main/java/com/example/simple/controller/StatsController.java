package com.example.simple.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@ConditionalOnProperty(name = "spring.datasource.url", matchIfMissing = false)
public class StatsController {
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStats() {
        if (jdbcTemplate == null) {
            return ResponseEntity.ok(Map.of("message", "데이터베이스가 설정되지 않았습니다"));
        }
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 전체 요청 수
            Integer totalRequests = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM LLM_LOGS", Integer.class
            );
            stats.put("total_requests", totalRequests);
            
            // 성공률
            Double successRate = jdbcTemplate.queryForObject(
                "SELECT ROUND(AVG(SUCCESS) * 100, 2) FROM LLM_LOGS", Double.class
            );
            stats.put("success_rate", successRate);
            
            // 평균 응답시간
            Double avgResponseTime = jdbcTemplate.queryForObject(
                "SELECT ROUND(AVG(RESPONSE_TIME_MS), 2) FROM LLM_LOGS WHERE SUCCESS = 1", Double.class
            );
            stats.put("avg_response_time_ms", avgResponseTime);
            
            // 엔진별 통계
            List<Map<String, Object>> engineStats = jdbcTemplate.queryForList(
                "SELECT ENGINE, COUNT(*) as REQUEST_COUNT, " +
                "ROUND(AVG(RESPONSE_TIME_MS), 2) as AVG_RESPONSE_TIME " +
                "FROM LLM_LOGS WHERE SUCCESS = 1 GROUP BY ENGINE"
            );
            stats.put("engine_stats", engineStats);
            
            // 최근 24시간 통계
            Integer recentRequests = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM LLM_LOGS WHERE REQUEST_TIME >= SYSDATE - 1", Integer.class
            );
            stats.put("requests_last_24h", recentRequests);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", "통계 조회 실패: " + e.getMessage()));
        }
    }
}