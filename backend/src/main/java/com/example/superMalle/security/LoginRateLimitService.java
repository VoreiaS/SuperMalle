package com.example.superMalle.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting service for login attempts.
 * Prevents brute-force password attacks by limiting login attempts per IP address.
 *
 * Limits:
 * - 5 failed login attempts per minute per IP
 * - After 5 failures, the IP is blocked for 5 minutes
 */
@Service
public class LoginRateLimitService {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Try to consume a login attempt token.
     * @param ipAddress The client's IP address
     * @return true if the request is allowed, false if rate-limited
     */
    public boolean tryConsume(String ipAddress) {
        Bucket bucket = buckets.computeIfAbsent(ipAddress, this::createNewBucket);
        return bucket.tryConsume(1);
    }

    /**
     * Reset the rate limit for an IP (e.g., after successful login).
     */
    public void resetLimit(String ipAddress) {
        buckets.remove(ipAddress);
    }

    /**
     * Get remaining attempts for an IP.
     */
    public int getRemainingAttempts(String ipAddress) {
        Bucket bucket = buckets.get(ipAddress);
        return bucket != null ? (int) bucket.getAvailableTokens() : 5;
    }

    private Bucket createNewBucket(String ipAddress) {
        Bandwidth bandwidth = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(5)));
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}
