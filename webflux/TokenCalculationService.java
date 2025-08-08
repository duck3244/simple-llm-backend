package com.example.simple.service;

import com.example.simple.dto.TokenInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 토큰 계산 서비스 인터페이스
 * 
 * 다양한 토큰 계산 방식을 지원하며, 동기와 비동기(Reactive) 방식을 모두 제공합니다.
 */
public interface TokenCalculationService {
    
    /**
     * 단일 텍스트의 토큰 수를 계산합니다 (동기 방식)
     * 
     * @param text 토큰 수를 계산할 텍스트
     * @param model 사용할 모델명
     * @return 토큰 정보
     */
    TokenInfo calculateTokens(String text, String model);
    
    /**
     * 단일 텍스트의 토큰 수를 계산합니다 (비동기 방식)
     * 
     * @param text 토큰 수를 계산할 텍스트
     * @param model 사용할 모델명
     * @return 토큰 정보를 담은 Mono
     */
    Mono<TokenInfo> calculateTokensReactive(String text, String model);
    
    /**
     * 여러 텍스트의 토큰 수를 배치로 계산합니다
     * 
     * @param texts 토큰 수를 계산할 텍스트 리스트
     * @param model 사용할 모델명
     * @return 토큰 정보 스트림
     */
    Flux<TokenInfo> calculateTokensBatch(List<String> texts, String model);
    
    /**
     * 텍스트 스트림의 토큰 수를 스트리밍 방식으로 계산합니다
     * 
     * @param textStream 텍스트 스트림
     * @param model 사용할 모델명
     * @return 토큰 정보 스트림
     */
    Flux<TokenInfo> calculateTokensStream(Flux<String> textStream, String model);
    
    /**
     * 입력과 예상 출력을 모두 고려한 토큰 수를 계산합니다
     * 
     * @param inputText 입력 텍스트
     * @param expectedOutputTokens 예상 출력 토큰 수
     * @param model 사용할 모델명
     * @return 종합 토큰 정보
     */
    Mono<TokenInfo> calculateTotalTokens(String inputText, int expectedOutputTokens, String model);
    
    /**
     * 토큰 계산 방식이 지원되는지 확인합니다
     * 
     * @param model 확인할 모델명
     * @return 지원 여부
     */
    boolean supportsModel(String model);
    
    /**
     * 서비스가 현재 사용 가능한지 확인합니다
     * 
     * @return 사용 가능 여부를 담은 Mono
     */
    Mono<Boolean> isHealthy();
    
    /**
     * 캐시를 클리어합니다
     */
    void clearCache();
    
    /**
     * 서비스 통계 정보를 반환합니다
     * 
     * @return 통계 정보
     */
    ServiceStats getStats();
    
    /**
     * 서비스 통계 정보
     */
    interface ServiceStats {
        long getTotalCalculations();
        long getCacheHits();
        long getCacheMisses();
        double getAverageProcessingTime();
        String getServiceType();
    }
}