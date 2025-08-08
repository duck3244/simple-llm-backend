package com.example.simple.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

/**
 * 토큰 계산 관련 설정 클래스
 */
@Configuration
@ConfigurationProperties(prefix = "token-calculation")
@Data
public class TokenCalculationConfig {
    
    /**
     * 로컬 토큰 계산 설정
     */
    private LocalConfig local = new LocalConfig();
    
    /**
     * 외부 API 설정
     */
    private ExternalApiConfig external = new ExternalApiConfig();
    
    /**
     * 캐싱 설정
     */
    private CacheConfig cache = new CacheConfig();
    
    /**
     * 비용 계산 설정
     */
    private CostConfig cost = new CostConfig();
    
    /**
     * 로컬 토큰 계산 설정
     */
    @Data
    public static class LocalConfig {
        /**
         * 로컬 토큰 계산 활성화 여부
         */
        private boolean enabled = true;
        
        /**
         * 기본 인코딩 타입
         */
        private String defaultEncoding = "cl100k_base";
        
        /**
         * 병렬 처리 스레드 수
         */
        private int parallelThreads = 4;
        
        /**
         * 최대 텍스트 길이 (문자 수)
         */
        private int maxTextLength = 100000;
    }
    
    /**
     * 외부 API 설정
     */
    @Data
    public static class ExternalApiConfig {
        /**
         * 외부 API 사용 활성화 여부
         */
        private boolean enabled = false;
        
        /**
         * Hugging Face API 설정
         */
        private HuggingFaceConfig huggingFace = new HuggingFaceConfig();
        
        /**
         * OpenAI API 설정
         */
        private OpenAIConfig openAI = new OpenAIConfig();
        
        /**
         * 요청 타임아웃
         */
        private Duration timeout = Duration.ofSeconds(10);
        
        /**
         * 재시도 횟수
         */
        private int retryAttempts = 3;
        
        /**
         * 동시 요청 제한
         */
        private int concurrencyLimit = 5;
        
        @Data
        public static class HuggingFaceConfig {
            private boolean enabled = false;
            private String baseUrl = "https://api-inference.huggingface.co";
            private String apiToken = "${HUGGINGFACE_API_TOKEN:}";
            private Duration timeout = Duration.ofSeconds(15);
        }
        
        @Data
        public static class OpenAIConfig {
            private boolean enabled = false;
            private String baseUrl = "https://api.openai.com/v1";
            private String apiToken = "${OPENAI_API_TOKEN:}";
            private Duration timeout = Duration.ofSeconds(10);
        }
    }
    
    /**
     * 캐싱 설정
     */
    @Data
    public static class CacheConfig {
        /**
         * 캐싱 활성화 여부
         */
        private boolean enabled = true;
        
        /**
         * 캐시 만료 시간
         */
        private Duration expireAfterWrite = Duration.ofHours(1);
        
        /**
         * 최대 캐시 크기
         */
        private long maximumSize = 10000;
        
        /**
         * 캐시 통계 활성화
         */
        private boolean recordStats = true;
    }
    
    /**
     * 비용 계산 설정
     */
    @Data
    public static class CostConfig {
        /**
         * 모델별 토큰 비용 (1K 토큰당 USD)
         */
        private Map<String, ModelCost> modelCosts = Map.of(
            "gpt-3.5-turbo", new ModelCost(0.0015, 0.002),
            "gpt-4", new ModelCost(0.03, 0.06),
            "gpt-4-turbo", new ModelCost(0.01, 0.03),
            "text-davinci-003", new ModelCost(0.02, 0.02),
            "claude-3-haiku", new ModelCost(0.00025, 0.00125),
            "claude-3-sonnet", new ModelCost(0.003, 0.015),
            "claude-3-opus", new ModelCost(0.015, 0.075)
        );
        
        /**
         * 기본 비용 (알 수 없는 모델의 경우)
         */
        private ModelCost defaultCost = new ModelCost(0.002, 0.002);
        
        /**
         * 모델별 비용 정보
         */
        @Data
        public static class ModelCost {
            /**
             * 입력 토큰 비용 (1K 토큰당 USD)
             */
            private final double inputCostPer1K;
            
            /**
             * 출력 토큰 비용 (1K 토큰당 USD)
             */
            private final double outputCostPer1K;
            
            public ModelCost(double inputCostPer1K, double outputCostPer1K) {
                this.inputCostPer1K = inputCostPer1K;
                this.outputCostPer1K = outputCostPer1K;
            }
        }
    }
    
    /**
     * 모델명을 정규화 (엔진명 → 표준 모델명)
     */
    public String normalizeModelName(String model) {
        if (model == null) return "gpt-3.5-turbo";
        
        // 엔진명을 모델명으로 매핑
        Map<String, String> engineToModel = Map.of(
            "vllm", "gpt-3.5-turbo",
            "sglang", "gpt-4",
            "openai", "gpt-3.5-turbo",
            "anthropic", "claude-3-sonnet"
        );
        
        return engineToModel.getOrDefault(model.toLowerCase(), model);
    }
    
    /**
     * 모델의 비용 정보 조회
     */
    public CostConfig.ModelCost getModelCost(String model) {
        String normalizedModel = normalizeModelName(model);
        return cost.getModelCosts().getOrDefault(normalizedModel, cost.getDefaultCost());
    }
}