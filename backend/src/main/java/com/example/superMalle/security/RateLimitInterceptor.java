package com.example.superMalle.security;

import com.example.superMalle.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rate Limiting Interceptor
 * 
 * Intercepts requests and applies rate limiting based on IP, user, and endpoint
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        // Skip rate limiting for health checks and actuator endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.equals("/health")) {
            return true;
        }

        // Get user information if authenticated
        String userId = null;
        String role = "ANONYMOUS";
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            userId = authentication.getName();
            role = authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .findFirst()
                    .orElse("ANONYMOUS")
                    .replace("ROLE_", "");
        }

        // Check if request should be rate limited
        boolean rateLimited = rateLimitConfig.isRateLimited(request, userId, role);
        
        if (rateLimited) {
            log.warn("Rate limit exceeded for IP: {}, User: {}, Role: {}, Endpoint: {}", 
                    rateLimitConfig.getClientIpAddress(request), userId, role, path);
            
            // Set rate limit headers
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Limit", "100");
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));
            response.setContentType("application/json");
            
            String errorResponse = "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}";
            response.getWriter().write(errorResponse);
            
            return false;
        }

        // Add rate limit headers to response
        addRateLimitHeaders(response, request, userId);

        return true;
    }

    /**
     * Add rate limit headers to response
     */
    private void addRateLimitHeaders(HttpServletResponse response, HttpServletRequest request, String userId) {
        String ipAddress = rateLimitConfig.getClientIpAddress(request);
        String endpoint = request.getRequestURI();
        
        long remainingTokens = rateLimitConfig.getRemainingTokens(ipAddress, "ip");
        response.setHeader("X-RateLimit-Limit", "100");
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));
    }
}
