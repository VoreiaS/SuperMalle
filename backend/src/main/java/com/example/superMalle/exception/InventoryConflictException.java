package com.example.superMalle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an order cannot be placed due to inventory concurrency conflict.
 * Client should retry the request with exponential backoff.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InventoryConflictException extends RuntimeException {
    
    public InventoryConflictException(String message) {
        super(message);
    }
    
    public InventoryConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
