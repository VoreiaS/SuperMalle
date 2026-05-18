package com.example.superMalle.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as idempotent - duplicate requests with the same key within the TTL window
 * will return the cached result instead of re-executing the business logic.
 * 
 * <p>Security considerations:
 * <ul>
 *   <li>Idempotency keys must be unique per user/session to prevent cross-user replay</li>
 *   <li>Keys should be cryptographically random (UUID v4 or secure random hex)</li>
 *   <li>TTL should match business requirements (e.g., 24h for payments, 1h for cart ops)</li>
 *   <li>Store keys in a transactional, indexed table to prevent race conditions</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>
 * {@code
 * @Idempotent(key = "#idempotencyKey", ttlHours = 24, entity = "payment")
 * public PaymentResponse processPayment(@Header("Idempotency-Key") String idempotencyKey, ...) {
 *     // Business logic here - will only execute once per key within TTL
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    
    /**
     * SpEL expression to extract the idempotency key from method parameters.
     * Examples: "#idempotencyKey", "#request.idempotencyKey", "#header('Idempotency-Key')"
     */
    String key();
    
    /**
     * Time-to-live in hours for the idempotency record.
     * After this period, the key can be reused (prevents unbounded table growth).
     * Default: 24 hours (suitable for payment operations).
     */
    int ttlHours() default 24;
    
    /**
     * Logical entity name for grouping idempotency records (e.g., "payment", "order", "coupon").
     * Used for auditing and selective cleanup.
     */
    String entity();
    
    /**
     * Whether to enforce strict key format validation (UUID v4 or 32-char hex).
     * Recommended: true for production to prevent weak/replayable keys.
     */
    boolean strictKeyFormat() default true;
}
