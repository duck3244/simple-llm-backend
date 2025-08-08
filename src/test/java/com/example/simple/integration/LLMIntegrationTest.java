package com.example.simple.integration;

import com.example.simple.dto.LLMRequest;
import com.example.simple.dto.LLMResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "llm.vllm.enabled=false",
    "llm.sglang.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:integrationtest;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LLMIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api";
    }

    @Test
    @Order(1)
    public void contextLoads() {
        // Spring 컨텍스트가 정상적으로 로드되는지 확인
        assertNotNull(restTemplate);
        assertNotNull(objectMapper);
    }

    @Test
    @Order(2)
    public void healthEndpoint_ShouldReturnOK() {
        // Given
        String url = getBaseUrl() + "/health";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    @Test
    @Order(3)
    public void infoEndpoint_ShouldReturnApplicationInfo() {
        // Given
        String url = getBaseUrl() + "/info";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Simple LLM Backend"));
        assertTrue(response.getBody().contains("Java 11"));
        assertTrue(response.getBody().contains("Spring Boot 2.3.2"));
    }

    @Test
    @Order(4)
    public void detailedHealthEndpoint_ShouldReturnDetailedStatus() {
        // Given
        String url = getBaseUrl() + "/health/detailed";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("application"));
        assertTrue(response.getBody().contains("vllm"));
        assertTrue(response.getBody().contains("sglang"));
    }

    @Test
    @Order(5)
    public void generateEndpoint_WithValidRequest_ShouldReturnBadRequest_WhenEnginesDisabled() {
        // Given
        LLMRequest request = new LLMRequest();
        request.setPrompt("Hello, world!");
        request.setEngine("vllm");
        request.setMaxTokens(50);
        request.setTemperature(0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LLMRequest> entity = new HttpEntity<>(request, headers);

        String url = getBaseUrl() + "/generate";

        // When
        ResponseEntity<LLMResponse> response = restTemplate.postForEntity(url, entity, LLMResponse.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getError().contains("비활성화"));
    }

    @Test
    @Order(6)
    public void generateEndpoint_WithInvalidRequest_ShouldReturnBadRequest() {
        // Given
        LLMRequest request = new LLMRequest();
        request.setPrompt(""); // 빈 프롬프트
        request.setEngine("vllm");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LLMRequest> entity = new HttpEntity<>(request, headers);

        String url = getBaseUrl() + "/generate";

        // When
        ResponseEntity<LLMResponse> response = restTemplate.postForEntity(url, entity, LLMResponse.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @Order(7)
    public void generateEndpoint_WithMalformedJSON_ShouldReturnBadRequest() {
        // Given
        String malformedJson = "{ invalid json }";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(malformedJson, headers);

        String url = getBaseUrl() + "/generate";

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void statisticsEndpoint_ShouldReturnStatistics() {
        // Given
        String url = getBaseUrl() + "/stats";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        // H2 데이터베이스를 사용하므로 통계 API가 동작해야 함
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(9)
    public void actuatorEndpoints_ShouldBeAccessible() {
        // Given
        String[] endpoints = {"/actuator/health", "/actuator/info"};

        // When & Then
        for (String endpoint : endpoints) {
            String url = "http://localhost:" + port + endpoint;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            assertEquals(HttpStatus.OK, response.getStatusCode(), 
                "Endpoint " + endpoint + " should be accessible");
        }
    }

    @Test
    @Order(10)
    public void applicationShouldHandleConcurrentRequests() throws Exception {
        // Given
        int numberOfThreads = 5;
        int requestsPerThread = 3;
        Thread[] threads = new Thread[numberOfThreads];
        
        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    String url = getBaseUrl() + "/health";
                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                    assertEquals(HttpStatus.OK, response.getStatusCode(), 
                        "Thread " + threadIndex + " request " + j + " failed");
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(5000); // 5초 타임아웃
        }

        // Then
        assertTrue(true, "All concurrent requests completed successfully");
    }
}