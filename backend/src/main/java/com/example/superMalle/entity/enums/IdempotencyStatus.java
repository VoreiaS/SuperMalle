package com.example.superMalle.entity.enums;

/**
 * Status values for idempotency key tracking.
 * 
 * Security note: PROCESSING state prevents concurrent duplicate requests
 * from executing business logic simultaneously (prevents race conditions).
 */
public enum IdempotencyStatus {
    /** Request is being processed - key is locked, other requests with same key wait/reject */
    PROCESSING,
    
    /** Request completed successfully - cached response available for replay */
    COMPLETED,
    
    /** Request failed - key may be retried after backoff (prevents permanent lockout on transient errors) */
    FAILED
}
