package com.example.simple.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LLMConfigTest {

    @Nested
    @DisplayName("기본 프로퍼티 테스트")
    @TestPropertySource(properties = {
        "llm.vllm.enabled=true",
        "llm.vllm.base-url=http://test-vllm:8000",
        "llm.vllm.timeout=60",
        "llm.vllm.max-tokens=1024",
        "llm.vllm.temperature=0.8",
        "llm.sglang.enabled=false",
        "llm.sglang.base-url=http://test-sglang:30000",
        "llm.sglang.timeout=45",
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
            assertEquals(60, vllmConfig.getTimeout(), "vLLM timeout이 일치해야 합니다");
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
            assertEquals(45, sglangConfig.getTimeout(), "SGLang timeout이 일치해야 합니다");
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
            
            // vLLM 기본값 확인
            LLMConfig.VllmConfig vllmConfig = defaultConfig.getVllm();
            assertTrue(vllmConfig.isEnabled(), "vLLM이 기본적으로 활성화되어 있어야 합니다");
            assertEquals("http://localhost:8000", vllmConfig.getBaseUrl(), "vLLM 기본 URL이 올바르게 설정되어야 합니다");
            assertEquals(30, vllmConfig.getTimeout(), "vLLM 기본 timeout이 올바르게 설정되어야 합니다");
            assertEquals(512, vllmConfig.getMaxTokens(), "vLLM 기본 max tokens가 올바르게 설정되어야 합니다");
            assertEquals(0.7, vllmConfig.getTemperature(), 0.001, "vLLM 기본 temperature가 올바르게 설정되어야 합니다");
            
            // SGLang 기본값 확인
            LLMConfig.SglangConfig sglangConfig = defaultConfig.getSglang();
            assertTrue(sglangConfig.isEnabled(), "SGLang이 기본적으로 활성화되어 있어야 합니다");
            assertEquals("http://localhost:30000", sglangConfig.getBaseUrl(), "SGLang 기본 URL이 올바르게 설정되어야 합니다");
            assertEquals(30, sglangConfig.getTimeout(), "SGLang 기본 timeout이 올바르게 설정되어야 합니다");
            assertEquals(512, sglangConfig.getMaxTokens(), "SGLang 기본 max tokens가 올바르게 설정되어야 합니다");
            assertEquals(0.7, sglangConfig.getTemperature(), 0.001, "SGLang 기본 temperature가 올바르게 설정되어야 합니다");
        }

        @Test
        @DisplayName("VllmConfig 객체 생성 및 기본값 테스트")
        void vllmConfig_DefaultConstructor_ShouldSetDefaultValues() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();

            // Then
            assertTrue(vllmConfig.isEnabled());
            assertEquals("http://localhost:8000", vllmConfig.getBaseUrl());
            assertEquals(30, vllmConfig.getTimeout());
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
            assertEquals(30, sglangConfig.getTimeout());
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
            vllmConfig.setTimeout(120);
            vllmConfig.setMaxTokens(2048);
            vllmConfig.setTemperature(0.9);

            // Then
            assertFalse(vllmConfig.isEnabled());
            assertEquals("http://custom-vllm:9000", vllmConfig.getBaseUrl());
            assertEquals(120, vllmConfig.getTimeout());
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
            sglangConfig.setTimeout(90);
            sglangConfig.setMaxTokens(1536);
            sglangConfig.setTemperature(0.3);

            // Then
            assertFalse(sglangConfig.isEnabled());
            assertEquals("http://custom-sglang:31000", sglangConfig.getBaseUrl());
            assertEquals(90, sglangConfig.getTimeout());
            assertEquals(1536, sglangConfig.getMaxTokens());
            assertEquals(0.3, sglangConfig.getTemperature(), 0.001);
        }

        @Test
        @DisplayName("LLMConfig Setter/Getter가 올바르게 동작해야 한다")
        void llmConfig_SettersAndGetters_ShouldWork() {
            // Given
            LLMConfig llmConfig = new LLMConfig();
            LLMConfig.VllmConfig customVllmConfig = new LLMConfig.VllmConfig();
            LLMConfig.SglangConfig customSglangConfig = new LLMConfig.SglangConfig();

            // When
            llmConfig.setVllm(customVllmConfig);
            llmConfig.setSglang(customSglangConfig);

            // Then
            assertSame(customVllmConfig, llmConfig.getVllm());
            assertSame(customSglangConfig, llmConfig.getSglang());
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
            assertEquals(1, vllmConfig.getTimeout());
            assertEquals(1, sglangConfig.getTimeout());

            // When & Then - 큰 값 테스트
            vllmConfig.setTimeout(300);
            sglangConfig.setTimeout(300);
            assertEquals(300, vllmConfig.getTimeout());
            assertEquals(300, sglangConfig.getTimeout());
        }
    }

    @Nested
    @DisplayName("환경별 설정 테스트")
    @TestPropertySource(properties = {
        "llm.vllm.enabled=true",
        "llm.vllm.base-url=http://prod-vllm:8000",
        "llm.vllm.timeout=90",
        "llm.vllm.max-tokens=1024",
        "llm.vllm.temperature=0.5",
        "llm.sglang.enabled=true",
        "llm.sglang.base-url=http://prod-sglang:30000",
        "llm.sglang.timeout=120",
        "llm.sglang.max-tokens=2048",
        "llm.sglang.temperature=0.8"
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

            // Then - vLLM 운영 설정 확인
            assertTrue(vllmConfig.isEnabled());
            assertEquals("http://prod-vllm:8000", vllmConfig.getBaseUrl());
            assertEquals(90, vllmConfig.getTimeout());
            assertEquals(1024, vllmConfig.getMaxTokens());
            assertEquals(0.5, vllmConfig.getTemperature(), 0.001);

            // Then - SGLang 운영 설정 확인
            assertTrue(sglangConfig.isEnabled());
            assertEquals("http://prod-sglang:30000", sglangConfig.getBaseUrl());
            assertEquals(120, sglangConfig.getTimeout());
            assertEquals(2048, sglangConfig.getMaxTokens());
            assertEquals(0.8, sglangConfig.getTemperature(), 0.001);
        }
    }

    @Nested
    @DisplayName("비활성화 설정 테스트")
    @TestPropertySource(properties = {
        "llm.vllm.enabled=false",
        "llm.sglang.enabled=false"
    })
    class DisabledConfigTest {

        @Autowired
        private LLMConfig llmConfig;

        @Test
        @DisplayName("모든 엔진이 비활성화될 수 있어야 한다")
        void allEngines_CanBeDisabled() {
            // When
            LLMConfig.VllmConfig vllmConfig = llmConfig.getVllm();
            LLMConfig.SglangConfig sglangConfig = llmConfig.getSglang();

            // Then
            assertFalse(vllmConfig.isEnabled(), "vLLM이 비활성화되어야 합니다");
            assertFalse(sglangConfig.isEnabled(), "SGLang이 비활성화되어야 합니다");
        }
    }

    @Nested
    @DisplayName("잘못된 설정값 테스트")
    class InvalidConfigTest {

        @Test
        @DisplayName("음수 timeout 값 처리 테스트")
        void negativeTimeout_ShouldBeHandled() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();

            // When
            vllmConfig.setTimeout(-1);

            // Then
            assertEquals(-1, vllmConfig.getTimeout(), "음수 값도 설정될 수 있어야 합니다 (검증은 서비스 레이어에서)");
        }

        @Test
        @DisplayName("음수 maxTokens 값 처리 테스트")
        void negativeMaxTokens_ShouldBeHandled() {
            // Given
            LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();

            // When
            sglangConfig.setMaxTokens(-1);

            // Then
            assertEquals(-1, sglangConfig.getMaxTokens(), "음수 값도 설정될 수 있어야 합니다 (검증은 서비스 레이어에서)");
        }

        @Test
        @DisplayName("음수 temperature 값 처리 테스트")
        void negativeTemperature_ShouldBeHandled() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();

            // When
            vllmConfig.setTemperature(-0.1);

            // Then
            assertEquals(-0.1, vllmConfig.getTemperature(), 0.001, "음수 값도 설정될 수 있어야 합니다 (검증은 서비스 레이어에서)");
        }

        @Test
        @DisplayName("null URL 값 처리 테스트")
        void nullUrl_ShouldBeHandled() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();
            LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();

            // When
            vllmConfig.setBaseUrl(null);
            sglangConfig.setBaseUrl(null);

            // Then
            assertNull(vllmConfig.getBaseUrl(), "null URL도 설정될 수 있어야 합니다");
            assertNull(sglangConfig.getBaseUrl(), "null URL도 설정될 수 있어야 합니다");
        }

        @Test
        @DisplayName("빈 문자열 URL 값 처리 테스트")
        void emptyUrl_ShouldBeHandled() {
            // Given
            LLMConfig.VllmConfig vllmConfig = new LLMConfig.VllmConfig();
            LLMConfig.SglangConfig sglangConfig = new LLMConfig.SglangConfig();

            // When
            vllmConfig.setBaseUrl("");
            sglangConfig.setBaseUrl("");

            // Then
            assertEquals("", vllmConfig.getBaseUrl(), "빈 문자열 URL도 설정될 수 있어야 합니다");
            assertEquals("", sglangConfig.getBaseUrl(), "빈 문자열 URL도 설정될 수 있어야 합니다");
        }
    }

    @Nested
    @DisplayName("구성 검증 테스트")
    class ConfigurationValidationTest {

        @Test
        @DisplayName("설정 객체가 독립적이어야 한다")
        void configObjects_ShouldBeIndependent() {
            // Given
            LLMConfig config1 = new LLMConfig();
            LLMConfig config2 = new LLMConfig();

            // When
            config1.getVllm().setEnabled(false);
            config2.getVllm().setEnabled(true);

            // Then
            assertFalse(config1.getVllm().isEnabled(), "config1의 vLLM이 비활성화되어야 합니다");
            assertTrue(config2.getVllm().isEnabled(), "config2의 vLLM이 활성화되어야 합니다");
        }

        @Test
        @DisplayName("중첩 설정 객체가 독립적이어야 한다")
        void nestedConfigObjects_ShouldBeIndependent() {
            // Given
            LLMConfig.VllmConfig vllmConfig1 = new LLMConfig.VllmConfig();
            LLMConfig.VllmConfig vllmConfig2 = new LLMConfig.VllmConfig();

            // When
            vllmConfig1.setBaseUrl("http://server1:8000");
            vllmConfig2.setBaseUrl("http://server2:8000");

            // Then
            assertEquals("http://server1:8000", vllmConfig1.getBaseUrl());
            assertEquals("http://server2:8000", vllmConfig2.getBaseUrl());
            assertNotEquals(vllmConfig1.getBaseUrl(), vllmConfig2.getBaseUrl());
        }
    }

    @Nested
    @DisplayName("toString 및 객체 동등성 테스트")
    class ObjectMethodsTest {

        @Test
        @DisplayName("설정 객체들이 올바르게 생성되어야 한다")
        void configObjects_ShouldBeProperlyConstructed() {
            // Given
            LLMConfig llmConfig = new LLMConfig();

            // Then
            assertNotNull(llmConfig.getVllm(), "vLLM 설정이 null이면 안됩니다");
            assertNotNull(llmConfig.getSglang(), "SGLang 설정이 null이면 안됩니다");
            
            // 각 설정 객체가 실제로 다른 인스턴스인지 확인
            assertNotSame(llmConfig.getVllm(), llmConfig.getSglang(), "vLLM과 SGLang 설정은 다른 객체여야 합니다");
        }

        @Test
        @DisplayName("설정 변경이 독립적으로 동작해야 한다")
        void configChanges_ShouldBeIndependent() {
            // Given
            LLMConfig llmConfig = new LLMConfig();
            
            // When
            llmConfig.getVllm().setEnabled(false);
            llmConfig.getSglang().setEnabled(true);

            // Then
            assertFalse(llmConfig.getVllm().isEnabled(), "vLLM 비활성화가 반영되어야 합니다");
            assertTrue(llmConfig.getSglang().isEnabled(), "SGLang 활성화가 반영되어야 합니다");
        }
    }
}