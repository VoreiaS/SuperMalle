package com.example.superMalle.exception;

/**
 * Exception thrown when rate limit is exceeded
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
public class RateLimitExceededException extends RuntimeException {

    private final String identifier;
    private final String type;

    public RateLimitExceededException(String message) {
        super(message);
        this.identifier = null;
        this.type = null;
    }

    public RateLimitExceededException(String message, String identifier, String type) {
        super(message);
        this.identifier = identifier;
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getType() {
        return type;
    }
}
