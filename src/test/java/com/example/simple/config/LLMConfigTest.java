package com.example.simple.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LLMConfigTest {

    @Nested
    @DisplayName("기본 프로퍼티 테스트")
    @TestPropertySource(properties = {
        "llm.vllm.enabled=true",
        "llm.vllm.base-url=http://test-vllm:8000",
        "llm.vllm.timeout=60s",
        "llm.vllm.max-tokens=1024",
        "llm.vllm.temperature=0.8",
        "llm.sglang.enabled=false",
        "llm.sglang.base-url=http://test-sglang:30000",
        "llm.sglang.timeout=45s",
        "llm.sglang.max-tokens=2048",
        "llm.sglang.temperature=0.6"
    })
    class BasicPropertiesTest {

        @Autowired
        private LLMConfig llmConfig;

        @Test
        @DisplayName("vLLM 설정이 올바르게 로드되어야 한다")
        void vllmConfig_ShouldLoadPropertiesCorrectly() {
            // When
            LLMConfig.VllmConfig vllmConfig = llmConfig.getVllm();

            // Then
            assertNotNull(vllmConfig, "vLLM 설정이 null이면 안됩니다");
            assertTrue(vllmConfig.isEnabled(), "vLLM이 활성화되어 있어야 합니다");
            assertEquals("http://test-vllm:8000", vllmConfig.getBaseUrl(), "vLLM Base URL이 일치해야 합니다");
            assertEquals(60, vllmConfig.getTimeoutSeconds(), "vLLM timeout이 일치해야 합니다");
            assertEquals(Duration.ofSeconds(60), vllmConfig.getTimeout(), "vLLM timeout Duration이 일치해야 합니다");
            assertEquals(1024, vllmConfig.getMaxTokens(), "vLLM max tokens가 일치해야 합니다");
            assertEquals(0.8, vllmConfig.getTemperature(), 0.001, "vLLM temperature가 일치해야 합니다");
        }

        @Test
        @DisplayName("SGLang 설정이 올바르게 로드되어야 한다")
        void sglangConfig_ShouldLoadPropertiesCorrectly() {
            // When
            LLMConfig.SglangConfig sglangConfig = llmConfig.getSglang();

            // Then
            assertNotNull(sglangConfig, "SGLang 설정이 null이면 안됩니다");
            assertFalse(sglangConfig.isEnabled(), "SGLang이 비활성화되어 있어야 합니다");
            assertEquals("http://test-sglang:30000", sglangConfig.getBaseUrl(), "SGLang Base URL이 일치해야 합니다");
            assertEquals(45, sglangConfig.getTimeoutSeconds(), "SGLang timeout이 일치해야 합니다");
            assertEquals(Duration.ofSeconds(45), sglangConfig.getTimeout(), "SGLang timeout Duration이 일치해야 합니다");
            assertEquals(2048, sglangConfig.getMaxTokens(), "SGLang max tokens가 일치해야 합니다");
            assertEquals(0.6, sglangConfig.getTemperature(), 0.001, "SGLang temperature가 일치해야 합니다");
        }

        @Test
        @DisplayName("Config Bean이 성공적으로 생성되어야 한다")
        void configBeans_ShouldBeCreatedSuccessfully() {
            // Then
            assertNotNull(llmConfig, "LLMConfig Bean이 생성되어야 합니다");
            assertNotNull(llmConfig.getVllm(), "VllmConfig가 생성되어야 합니다");
            assertNotNull(llmConfig.getSglang(), "SglangConfig가 생성되어야 합니다");
            assertNotNull(llmConfig.getFailover(), "FailoverConfig가 생성되어야 합니다");
        }
    }

    @Nested
    @DisplayName("기본값 테스트")
    class DefaultValuesTest {

        @Test
        @DisplayName("기본값이 올바르게 설정되어야 한다")
        void defaultValues_ShouldBeSetCorrectly() {
            // Given - 새로운 설정 객체 생성 (기본값 테스트)
            LLMConfig defaultConfig = new LLMConfig();

            // Then
            assertNotNull(defaultConfig.getVllm(), "기본 vLLM 설정이 생성되어야 합니다");
            assertNotNull(defaultConfig.getSglang(), "기본 SGLang 설정이 생성되어야 합니다");
            assertNotNull(defaultConfig.getFailover(), "기본 Failover 설정이 생성되어야 합니다");
            
            // vLLM 기본값 확인
            LLMConfig.VllmConfig vllmConfig = defaultConfig.getVllm();
            assertTrue(vllmConfig.isEnabled(), "vLLM이 기본적으로 활성화되어 있어야 합니다");
            assertEquals("http://localhost:8000", vllmConfig.getBaseUrl(), "vLLM 기본 URL이 올바르게 설정되어야 합니다");
            assertEquals(30, vllmConfig.getTimeoutSeconds(), "vLLM 기본 timeout이 올바르게 설정되어야 합니다");
            assertEquals(Duration.ofSeconds(30), vllmConfig.getTimeout(), "vLLM 기본 timeout Duration이 올바르게 설정되어야 합니다");
            assertEquals(512, vllmConfig.getMaxTokens(), "vLLM 기본 max tokens가 올바르게 설정되어야 합니다");
            assertEquals(0.7, vllmConfig.getTemperature(), 0.001, "vLLM 기본 temperature가 올바르게 설정되어야 합니다");
            
            // SGLang 기본값 확인
            LLMConfig.SglangConfig sglangConfig = defaultConfig.getSglang();
            assertTrue(sglangConfig.isEnabled(), "SGLang이 기본적으로 활성화되어 있어야 합니다");
            assertEquals("http://localhost:30000", sglangConfig.getBaseUrl(), "SGLang 기본 URL이 올바르게 설정되어야 합니다");
            assertEquals(30, sglangConfig.getTimeoutSeconds(), "SGLang 기본 timeout이 올바르게 설정되어야 합니다");
            assertEquals(Duration.ofSeconds(30), sglangConfig.getTimeout(), "SGLang 기본 timeout Duration이 올바르게 설정되어야 합니다");
            assertEquals(512, sglangConfig.getMaxTokens(), "SGLang 기본 max tokens가 올바르게 설정되어야 합니다");
            assertEquals(0.7, sglangConfig.getTemperature(), 0.001, "SGLang 기본 temperature가 올바르게 설정되어야 합니다");
            
            // Failover 기본값 확인
            LLMConfig.FailoverConfig failoverConfig = defaultConfig.getFailover();
            assertFalse(failoverConfig.isEnabled(), "Failover가 기본적으로 비활성화되어 있어야 합니다");
            assertEquals(3, failoverConfig.getRetryAttempts(), "기본 재시도 횟수가 올바르게 설정되어야 합니다");
            assertEquals(1, failoverConfig.getRetryDelaySeconds(), "기본 재시도 지연시간이 올바르게 설정되어야 합니다");
        }

        @Test
        @DisplayName("VllmConfig 객체 생성 및 기본값 테스트")
        void vllmConfig_DefaultConstructor_ShouldSetDefaultValues() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();

            // Then
            assertTrue(vllmConfig.isEnabled());
            assertEquals("http://localhost:8000", vllmConfig.getBaseUrl());
            assertEquals(Duration.ofSeconds(30), vllmConfig.getTimeout());
            assertEquals(30, vllmConfig.getTimeoutSeconds());
            assertEquals(512, vllmConfig.getMaxTokens());
            assertEquals(0.7, vllmConfig.getTemperature(), 0.001);
        }

        @Test
        @DisplayName("SglangConfig 객체 생성 및 기본값 테스트")
        void sglangConfig_DefaultConstructor_ShouldSetDefaultValues() {
            // Given
            LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();

            // Then
            assertTrue(sglangConfig.isEnabled());
            assertEquals("http://localhost:30000", sglangConfig.getBaseUrl());
            assertEquals(Duration.ofSeconds(30), sglangConfig.getTimeout());
            assertEquals(30, sglangConfig.getTimeoutSeconds());
            assertEquals(512, sglangConfig.getMaxTokens());
            assertEquals(0.7, sglangConfig.getTemperature(), 0.001);
        }
    }

    @Nested
    @DisplayName("Setter/Getter 테스트")
    class SetterGetterTest {

        @Test
        @DisplayName("VllmConfig Setter/Getter가 올바르게 동작해야 한다")
        void vllmConfig_SettersAndGetters_ShouldWork() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();

            // When
            vllmConfig.setEnabled(false);
            vllmConfig.setBaseUrl("http://custom-vllm:9000");
            vllmConfig.setTimeout(120); // 초 단위로 설정
            vllmConfig.setMaxTokens(2048);
            vllmConfig.setTemperature(0.9);

            // Then
            assertFalse(vllmConfig.isEnabled());
            assertEquals("http://custom-vllm:9000", vllmConfig.getBaseUrl());
            assertEquals(120, vllmConfig.getTimeoutSeconds());
            assertEquals(Duration.ofSeconds(120), vllmConfig.getTimeout());
            assertEquals(2048, vllmConfig.getMaxTokens());
            assertEquals(0.9, vllmConfig.getTemperature(), 0.001);
        }

        @Test
        @DisplayName("SglangConfig Setter/Getter가 올바르게 동작해야 한다")
        void sglangConfig_SettersAndGetters_ShouldWork() {
            // Given
            LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();

            // When
            sglangConfig.setEnabled(false);
            sglangConfig.setBaseUrl("http://custom-sglang:31000");
            sglangConfig.setTimeout(90); // 초 단위로 설정
            sglangConfig.setMaxTokens(1536);
            sglangConfig.setTemperature(0.3);

            // Then
            assertFalse(sglangConfig.isEnabled());
            assertEquals("http://custom-sglang:31000", sglangConfig.getBaseUrl());
            assertEquals(90, sglangConfig.getTimeoutSeconds());
            assertEquals(Duration.ofSeconds(90), sglangConfig.getTimeout());
            assertEquals(1536, sglangConfig.getMaxTokens());
            assertEquals(0.3, sglangConfig.getTemperature(), 0.001);
        }

        @Test
        @DisplayName("Duration 타입으로 timeout 설정이 동작해야 한다")
        void timeout_DurationSetter_ShouldWork() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();
            LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();

            // When
            vllmConfig.setTimeout(Duration.ofMinutes(2)); // 2분
            sglangConfig.setTimeout(Duration.ofSeconds(45)); // 45초

            // Then
            assertEquals(120, vllmConfig.getTimeoutSeconds()); // 2분 = 120초
            assertEquals(Duration.ofMinutes(2), vllmConfig.getTimeout());
            assertEquals(45, sglangConfig.getTimeoutSeconds());
            assertEquals(Duration.ofSeconds(45), sglangConfig.getTimeout());
        }

        @Test
        @DisplayName("문자열로 timeout 설정이 동작해야 한다")
        void timeout_StringSetter_ShouldWork() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();
            LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();

            // When
            vllmConfig.setTimeout("2m"); // 2분
            sglangConfig.setTimeout("45s"); // 45초

            // Then
            assertEquals(120, vllmConfig.getTimeoutSeconds()); // 2분 = 120초
            assertEquals(45, sglangConfig.getTimeoutSeconds());
        }

        @Test
        @DisplayName("FailoverConfig Setter/Getter가 올바르게 동작해야 한다")
        void failoverConfig_SettersAndGetters_ShouldWork() {
            // Given
            LLMConfig.FailoverConfig failoverConfig = new LLMConfig.FailoverConfig();

            // When
            failoverConfig.setEnabled(true);
            failoverConfig.setRetryAttempts(5);
            failoverConfig.setRetryDelay(2); // 초 단위
            
            // Then
            assertTrue(failoverConfig.isEnabled());
            assertEquals(5, failoverConfig.getRetryAttempts());
            assertEquals(2, failoverConfig.getRetryDelaySeconds());
            assertEquals(Duration.ofSeconds(2), failoverConfig.getRetryDelay());
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTest {

        @Test
        @DisplayName("Temperature 경계값 테스트")
        void temperature_BoundaryValues_ShouldBeValid() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();
            LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();

            // When & Then - 최소값 테스트
            vllmConfig.setTemperature(0.0);
            sglangConfig.setTemperature(0.0);
            assertEquals(0.0, vllmConfig.getTemperature(), 0.001);
            assertEquals(0.0, sglangConfig.getTemperature(), 0.001);

            // When & Then - 최대값 테스트
            vllmConfig.setTemperature(2.0);
            sglangConfig.setTemperature(2.0);
            assertEquals(2.0, vllmConfig.getTemperature(), 0.001);
            assertEquals(2.0, sglangConfig.getTemperature(), 0.001);
        }

        @Test
        @DisplayName("MaxTokens 경계값 테스트")
        void maxTokens_BoundaryValues_ShouldBeValid() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();
            LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();

            // When & Then - 최소값 테스트
            vllmConfig.setMaxTokens(1);
            sglangConfig.setMaxTokens(1);
            assertEquals(1, vllmConfig.getMaxTokens());
            assertEquals(1, sglangConfig.getMaxTokens());

            // When & Then - 큰 값 테스트
            vllmConfig.setMaxTokens(4096);
            sglangConfig.setMaxTokens(4096);
            assertEquals(4096, vllmConfig.getMaxTokens());
            assertEquals(4096, sglangConfig.getMaxTokens());
        }

        @Test
        @DisplayName("Timeout 경계값 테스트")
        void timeout_BoundaryValues_ShouldBeValid() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();
            LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();

            // When & Then - 최소값 테스트
            vllmConfig.setTimeout(1);
            sglangConfig.setTimeout(1);
            assertEquals(1, vllmConfig.getTimeoutSeconds());
            assertEquals(1, sglangConfig.getTimeoutSeconds());

            // When & Then - 큰 값 테스트
            vllmConfig.setTimeout(300);
            sglangConfig.setTimeout(300);
            assertEquals(300, vllmConfig.getTimeoutSeconds());
            assertEquals(300, sglangConfig.getTimeoutSeconds());
        }
    }

    @Nested
    @DisplayName("환경별 설정 테스트")
    @TestPropertySource(properties = {
        "llm.vllm.enabled=true",
        "llm.vllm.base-url=http://prod-vllm:8000",
        "llm.vllm.timeout=90s",
        "llm.vllm.max-tokens=1024",
        "llm.vllm.temperature=0.5",
        "llm.sglang.enabled=true",
        "llm.sglang.base-url=http://prod-sglang:30000",
        "llm.sglang.timeout=120s",
        "llm.sglang.max-tokens=2048",
        "llm.sglang.temperature=0.8",
        "llm.failover.enabled=true",
        "llm.failover.retry-attempts=5",
        "llm.failover.retry-delay=2s"
    })
    class EnvironmentSpecificTest {

        @Autowired
        private LLMConfig llmConfig;

        @Test
        @DisplayName("운영 환경 설정이 올바르게 로드되어야 한다")
        void productionConfig_ShouldLoadCorrectly() {
            // When
            LLMConfig.VllmConfig vllmConfig = llmConfig.getVllm();
            LLMConfig.SglangConfig sglangConfig = llmConfig.getSglang();
            LLMConfig.FailoverConfig failoverConfig = llmConfig.getFailover();

            // Then - vLLM 운영 설정 확인
            assertTrue(vllmConfig.isEnabled());
            assertEquals("http://prod-vllm:8000", vllmConfig.getBaseUrl());
            assertEquals(90, vllmConfig.getTimeoutSeconds());
            assertEquals(1024, vllmConfig.getMaxTokens());
            assertEquals(0.5, vllmConfig.getTemperature(), 0.001);

            // Then - SGLang 운영 설정 확인
            assertTrue(sglangConfig.isEnabled());
            assertEquals("http://prod-sglang:30000", sglangConfig.getBaseUrl());
            assertEquals(120, sglangConfig.getTimeoutSeconds());
            assertEquals(2048, sglangConfig.getMaxTokens());
            assertEquals(0.8, sglangConfig.getTemperature(), 0.001);

            // Then - Failover 설정 확인
            assertTrue(failoverConfig.isEnabled());
            assertEquals(5, failoverConfig.getRetryAttempts());
            assertEquals(2, failoverConfig.getRetryDelaySeconds());
        }
    }

    @Nested
    @DisplayName("비활성화 설정 테스트")
    @TestPropertySource(properties = {
        "llm.vllm.enabled=false",
        "llm.sglang.enabled=false",
        "llm.failover.enabled=false"
    })
    class DisabledConfigTest {

        @Autowired
        private LLMConfig llmConfig;

        @Test
        @DisplayName("모든 엔진과 기능이 비활성화될 수 있어야 한다")
        void allEnginesAndFeatures_CanBeDisabled() {
            // When
            LLMConfig.VllmConfig vllmConfig = llmConfig.getVllm();
            LLMConfig.SglangConfig sglangConfig = llmConfig.getSglang();
            LLMConfig.FailoverConfig failoverConfig = llmConfig.getFailover();

            // Then
            assertFalse(vllmConfig.isEnabled(), "vLLM이 비활성화되어야 합니다");
            assertFalse(sglangConfig.isEnabled(), "SGLang이 비활성화되어야 합니다");
            assertFalse(failoverConfig.isEnabled(), "Failover가 비활성화되어야 합니다");
        }
    }
}