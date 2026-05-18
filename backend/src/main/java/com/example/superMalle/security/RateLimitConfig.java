package com.example.superMalle.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration
 * 
 * Provides per-endpoint rate limiting with different limits for different user roles
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Component
@Slf4j
public class RateLimitConfig {

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate.limit.default.requests:100}")
    private int defaultRequests;

    @Value("${rate.limit.default.duration:1}")
    private int defaultDurationMinutes;

    @Value("${rate.limit.admin.requests:1000}")
    private int adminRequests;

    @Value("${rate.limit.staff.requests:500}")
    private int staffRequests;

    @Value("${rate.limit.customer.requests:100}")
    private int customerRequests;

    // Cache buckets per IP address
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    // Cache buckets per user ID
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    // Cache buckets per endpoint
    private final Map<String, Bucket> endpointBuckets = new ConcurrentHashMap<>();

    /**
     * Create a new bucket with specified capacity and refill rate
     */
    private Bucket createBucket(int capacity, int durationMinutes) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofMinutes(durationMinutes))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Get or create bucket for IP address
     */
    public Bucket getIpBucket(String ipAddress) {
        if (!rateLimitEnabled) {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(Integer.MAX_VALUE)
                    .refillIntervally(Integer.MAX_VALUE, Duration.ofMinutes(1))
                    .build();
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        }

        return ipBuckets.computeIfAbsent(ipAddress, key -> 
                createBucket(defaultRequests, defaultDurationMinutes));
    }

    /**
     * Get or create bucket for user
     */
    public Bucket getUserBucket(String userId, String role) {
        if (!rateLimitEnabled) {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(Integer.MAX_VALUE)
                    .refillIntervally(Integer.MAX_VALUE, Duration.ofMinutes(1))
                    .build();
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        }

        int requests = switch (role) {
            case "ADMIN" -> adminRequests;
            case "STAFF" -> staffRequests;
            case "CUSTOMER" -> customerRequests;
            default -> defaultRequests;
        };

        return userBuckets.computeIfAbsent(userId, key -> 
                createBucket(requests, defaultDurationMinutes));
    }

    /**
     * Get or create bucket for endpoint
     */
    public Bucket getEndpointBucket(String endpoint) {
        if (!rateLimitEnabled) {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(Integer.MAX_VALUE)
                    .refillIntervally(Integer.MAX_VALUE, Duration.ofMinutes(1))
                    .build();
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        }

        // Different limits for different endpoints
        int requests = switch (endpoint) {
            case "/api/v1/auth/login", "/api/v1/auth/register" -> 20;
            case "/api/v1/auth/refresh" -> 10;
            case "/api/v1/auth/forgot-password" -> 5;
            case "/api/v1/auth/reset-password" -> 10;
            case "/api/v1/orders" -> 50;
            case "/api/v1/menu/items" -> 200;
            default -> defaultRequests;
        };

        return endpointBuckets.computeIfAbsent(endpoint, key -> 
                createBucket(requests, defaultDurationMinutes));
    }

    /**
     * Check if request should be rate limited
     */
    public boolean isRateLimited(HttpServletRequest request, String userId, String role) {
        if (!rateLimitEnabled) {
            return false;
        }

        String ipAddress = getClientIpAddress(request);
        String endpoint = request.getRequestURI();

        // Check IP-based rate limiting
        Bucket ipBucket = getIpBucket(ipAddress);
        if (ipBucket.tryConsume(1)) {
            log.debug("IP {} allowed for endpoint {}", ipAddress, endpoint);
        } else {
            log.warn("IP {} rate limited for endpoint {}", ipAddress, endpoint);
            return true;
        }

        // Check user-based rate limiting (if user is authenticated)
        if (userId != null && !userId.isEmpty()) {
            Bucket userBucket = getUserBucket(userId, role);
            if (userBucket.tryConsume(1)) {
                log.debug("User {} ({}) allowed for endpoint {}", userId, role, endpoint);
            } else {
                log.warn("User {} ({}) rate limited for endpoint {}", userId, role, endpoint);
                return true;
            }
        }

        // Check endpoint-based rate limiting
        Bucket endpointBucket = getEndpointBucket(endpoint);
        if (endpointBucket.tryConsume(1)) {
            log.debug("Endpoint {} allowed", endpoint);
        } else {
            log.warn("Endpoint {} rate limited", endpoint);
            return true;
        }

        return false;
    }

    /**
     * Get client IP address from request
     */
    public String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Handle multiple IPs in X-Forwarded-For
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }

    /**
     * Get remaining tokens for a bucket
     */
    public long getRemainingTokens(String identifier, String type) {
        Bucket bucket = switch (type) {
            case "ip" -> ipBuckets.get(identifier);
            case "user" -> userBuckets.get(identifier);
            case "endpoint" -> endpointBuckets.get(identifier);
            default -> null;
        };
        
        return bucket != null ? bucket.getAvailableTokens() : 0;
    }

    /**
     * Clear all buckets (for testing)
     */
    public void clearAllBuckets() {
        ipBuckets.clear();
        userBuckets.clear();
        endpointBuckets.clear();
        log.info("Cleared all rate limit buckets");
    }
}
