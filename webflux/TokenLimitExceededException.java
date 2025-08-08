package com.example.simple.exception;

/**
 * 토큰 한도 초과 시 발생하는 예외
 */
public class TokenLimitExceededException extends RuntimeException {
    
    private final int requestedTokens;
    private final int maxAllowedTokens;
    private final String model;
    
    public TokenLimitExceededException(String message) {
        super(message);
        this.requestedTokens = 0;
        this.maxAllowedTokens = 0;
        this.model = "";
    }
    
    public TokenLimitExceededException(String message, int requestedTokens, int maxAllowedTokens, String model) {
        super(String.format("%s (요청: %d토큰, 최대: %d토큰, 모델: %s)", 
                message, requestedTokens, maxAllowedTokens, model));
        this.requestedTokens = requestedTokens;
        this.maxAllowedTokens = maxAllowedTokens;
        this.model = model;
    }
    
    public TokenLimitExceededException(String message, Throwable cause) {
        super(message, cause);
        this.requestedTokens = 0;
        this.maxAllowedTokens = 0;
        this.model = "";
    }
    
    public int getRequestedTokens() {
        return requestedTokens;
    }
    
    public int getMaxAllowedTokens() {
        return maxAllowedTokens;
    }
    
    public String getModel() {
        return model;
    }
    
    public double getExcessRatio() {
        if (maxAllowedTokens == 0) return 0.0;
        return (double) requestedTokens / maxAllowedTokens;
    }
}