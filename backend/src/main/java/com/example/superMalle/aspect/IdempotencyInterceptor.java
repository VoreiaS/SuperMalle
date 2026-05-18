package com.example.superMalle.aspect;

import com.example.superMalle.annotation.Idempotent;
import com.example.superMalle.entity.IdempotencyKey;
import com.example.superMalle.entity.enums.IdempotencyStatus;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.repository.IdempotencyKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * AOP interceptor for @Idempotent annotation.
 * 
 * Security guarantees:
 * 1. Key format validation (UUID v4 or 32-char hex) when strictKeyFormat=true
 * 2. User scoping: keys are scoped to authenticated user ID
 * 3. Payload hashing: same key with different body = rejected
 * 4. Pessimistic locking: prevents race conditions on duplicate requests
 * 5. TTL enforcement: prevents unbounded table growth
 * 
 * Thread-safety: Uses database-level pessimistic locking, not in-memory locks.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyInterceptor {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    @Around("@annotation(idempotent)")
    public Object handleIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        
        // Extract idempotency key from method parameters using SpEL
        String idempotencyKey = extractKey(joinPoint, idempotent.key());
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException("Idempotency key is required for " + methodName);
        }

        // Validate key format if strict mode enabled
        if (idempotent.strictKeyFormat() && !isValidKeyFormat(idempotencyKey)) {
            throw new BadRequestException("Invalid idempotency key format. Use UUID v4 or 32-char hex.");
        }

        // Get authenticated user ID for scoping
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BadRequestException("Authentication required for idempotent operations");
        }

        // Hash the request payload for integrity verification
        String requestHash = hashRequestPayload(joinPoint);

        // Try to acquire lock and check existing record (atomic operation)
        IdempotencyKey existing = idempotencyKeyRepository
                .findByKeyAndEntityAndUserIdWithLock(idempotencyKey, idempotent.entity(), userId)
                .orElse(null);

        if (existing != null) {
            // Key already exists - handle based on status
            return handleExistingRecord(existing, requestHash, idempotencyKey, methodName);
        }

        // No existing record - create new one in PROCESSING state
        IdempotencyKey newRecord = IdempotencyKey.builder()
                .key(idempotencyKey)
                .entity(idempotent.entity())
                .userId(userId)
                .requestHash(requestHash)
                .status(IdempotencyStatus.PROCESSING)
                .expiresAt(LocalDateTime.now().plusHours(idempotent.ttlHours()))
                .build();
        
        idempotencyKeyRepository.save(newRecord);

        try {
            // Execute the actual business logic
            Object result = joinPoint.proceed();
            
            // Cache successful response
            cacheSuccessfulResponse(newRecord, result);
            return result;
            
        } catch (Exception e) {
            // Mark as failed - allows retry after backoff
            markAsFailed(newRecord, e.getMessage());
            throw e;
        }
    }

    private String extractKey(ProceedingJoinPoint joinPoint, String spelExpression) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        
        for (int i = 0; i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        
        // Support header extraction via request attributes
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        context.setVariable("request", request);
        
        try {
            return PARSER.parseExpression(spelExpression).getValue(context, String.class);
        } catch (Exception e) {
            log.error("Failed to extract idempotency key from expression '{}': {}", spelExpression, e.getMessage());
            return null;
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == "anonymousUser") {
            return null;
        }
        // Assuming CustomUserDetails with getId() method
        if (auth.getPrincipal() instanceof com.example.superMalle.security.CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    private String hashRequestPayload(ProceedingJoinPoint joinPoint) {
        try {
            // Serialize all method arguments to JSON for hashing
            String payload = objectMapper.writeValueAsString(joinPoint.getArgs());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // This should never happen - SHA-256 is guaranteed by JCA
            log.error("SHA-256 algorithm not available - critical security failure", e);
            throw new IllegalStateException("SHA-256 not available", e);
        } catch (Exception e) {
            // Fallback: use UUID if serialization fails (less secure but prevents crash)
            log.warn("Request hashing failed, using fallback: {}", e.getMessage());
            return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        }
    }

    private boolean isValidKeyFormat(String key) {
        // UUID v4 pattern: 8-4-4-4-12 hex digits
        if (key.matches("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")) {
            return true;
        }
        // 32-char hex string (UUID without dashes)
        if (key.matches("^[0-9a-f]{32}$")) {
            return true;
        }
        return false;
    }

    private Object handleExistingRecord(IdempotencyKey record, String requestHash, 
                                      String key, String methodName) throws Exception {
        // Check if expired
        if (record.isExpired()) {
            log.info("Idempotency key {} expired, allowing retry", key);
            // Delete expired record and let caller create new one
            idempotencyKeyRepository.delete(record);
            throw new RetryIdempotencyException("Idempotency key expired, please retry with new key");
        }

        // Check payload integrity - same key, different body = potential attack
        if (!record.payloadMatches(requestHash)) {
            log.warn("Idempotency key {} used with different payload - possible replay attack", key);
            throw new BadRequestException("Idempotency key conflict: same key used with different request data");
        }

        // Handle based on status
        return switch (record.getStatus()) {
            case COMPLETED -> {
                log.info("Replaying cached response for idempotent key {}", key);
                yield objectMapper.readValue(record.getResponseBody(), Object.class);
            }
            case PROCESSING -> {
                // Another request is still processing - wait or reject
                log.warn("Concurrent request with key {} still processing", key);
                throw new BadRequestException("Request with this idempotency key is still being processed");
            }
            case FAILED -> {
                // Allow retry after backoff (client should implement exponential backoff)
                log.info("Allowing retry for failed idempotency key {}", key);
                throw new RetryIdempotencyException("Previous request failed, please retry");
            }
        };
    }

    private void cacheSuccessfulResponse(IdempotencyKey record, Object response) {
        try {
            record.setResponseBody(objectMapper.writeValueAsString(response));
            record.setResponseStatus(200);
            record.setStatus(IdempotencyStatus.COMPLETED);
            idempotencyKeyRepository.save(record);
        } catch (Exception e) {
            log.error("Failed to cache idempotent response for key {}: {}", record.getKey(), e.getMessage());
            // Don't fail the request if caching fails - just log
        }
    }

    private void markAsFailed(IdempotencyKey record, String errorMessage) {
        try {
            record.setStatus(IdempotencyStatus.FAILED);
            record.setErrorMessage(errorMessage != null ? errorMessage.substring(0, Math.min(500, errorMessage.length())) : "Unknown error");
            idempotencyKeyRepository.save(record);
        } catch (Exception e) {
            log.error("Failed to mark idempotency key {} as failed: {}", record.getKey(), e.getMessage());
        }
    }

    /**
     * Custom exception to signal that client should retry with a new idempotency key.
     */
    public static class RetryIdempotencyException extends Exception {
        public RetryIdempotencyException(String message) {
            super(message);
        }
    }
}
