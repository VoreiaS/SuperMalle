package com.example.superMalle.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

/**
 * Resilience Configuration
 * 
 * Configures circuit breakers, retries, and time limiters
 * for external service calls and critical operations
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Configuration
@Slf4j
public class ResilienceConfig {

    // Circuit breaker names
    public static final String STRIPE_CIRCUIT_BREAKER = "stripeCircuitBreaker";
    public static final String EMAIL_CIRCUIT_BREAKER = "emailCircuitBreaker";
    public static final String NOTIFICATION_CIRCUIT_BREAKER = "notificationCircuitBreaker";
    public static final String DATABASE_CIRCUIT_BREAKER = "databaseCircuitBreaker";
    public static final String EXTERNAL_API_CIRCUIT_BREAKER = "externalApiCircuitBreaker";

    // Retry names
    public static final String STRIPE_RETRY = "stripeRetry";
    public static final String EMAIL_RETRY = "emailRetry";
    public static final String NOTIFICATION_RETRY = "notificationRetry";
    public static final String DATABASE_RETRY = "databaseRetry";

    // Time limiter names
    public static final String STRIPE_TIME_LIMITER = "stripeTimeLimiter";
    public static final String EMAIL_TIME_LIMITER = "emailTimeLimiter";
    public static final String DATABASE_TIME_LIMITER = "databaseTimeLimiter";

    /**
     * Circuit breaker registry with custom configurations
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        log.info("Configuring circuit breakers");

        CircuitBreakerConfig stripeConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .build();

        CircuitBreakerConfig emailConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(5)
                .slidingWindowSize(20)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .build();

        CircuitBreakerConfig notificationConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(70)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .slidingWindowSize(20)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .build();

        CircuitBreakerConfig databaseConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(40)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(2)
                .slidingWindowSize(5)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .build();

        CircuitBreakerConfig externalApiConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .build();

        // Create registry with configurations
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(
                Map.of(
                        STRIPE_CIRCUIT_BREAKER, stripeConfig,
                        EMAIL_CIRCUIT_BREAKER, emailConfig,
                        NOTIFICATION_CIRCUIT_BREAKER, notificationConfig,
                        DATABASE_CIRCUIT_BREAKER, databaseConfig,
                        EXTERNAL_API_CIRCUIT_BREAKER, externalApiConfig
                )
        );

        // Add event listeners
        registry.circuitBreaker(STRIPE_CIRCUIT_BREAKER).getEventPublisher()
                .onStateTransition(event -> log.info("Stripe circuit breaker state transition: {}", event));
        registry.circuitBreaker(EMAIL_CIRCUIT_BREAKER).getEventPublisher()
                .onStateTransition(event -> log.info("Email circuit breaker state transition: {}", event));
        registry.circuitBreaker(NOTIFICATION_CIRCUIT_BREAKER).getEventPublisher()
                .onStateTransition(event -> log.info("Notification circuit breaker state transition: {}", event));
        registry.circuitBreaker(DATABASE_CIRCUIT_BREAKER).getEventPublisher()
                .onStateTransition(event -> log.info("Database circuit breaker state transition: {}", event));
        registry.circuitBreaker(EXTERNAL_API_CIRCUIT_BREAKER).getEventPublisher()
                .onStateTransition(event -> log.info("External API circuit breaker state transition: {}", event));

        return registry;
    }

    /**
     * Retry registry with custom configurations
     */
    @Bean
    public RetryRegistry retryRegistry() {
        log.info("Configuring retry mechanisms");

        RetryConfig stripeRetryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryOnException(e -> e instanceof RuntimeException)
                .build();

        RetryConfig emailRetryConfig = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofMillis(2000))
                .retryOnException(e -> e instanceof RuntimeException)
                .build();

        RetryConfig notificationRetryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryOnException(e -> e instanceof RuntimeException)
                .build();

        RetryConfig databaseRetryConfig = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(500))
                .retryOnException(e -> e instanceof RuntimeException)
                .build();

        // Create registry with configurations
        RetryRegistry registry = RetryRegistry.of(
                Map.of(
                        STRIPE_RETRY, stripeRetryConfig,
                        EMAIL_RETRY, emailRetryConfig,
                        NOTIFICATION_RETRY, notificationRetryConfig,
                        DATABASE_RETRY, databaseRetryConfig
                )
        );

        // Add event listeners
        registry.retry(STRIPE_RETRY).getEventPublisher()
                .onRetry(event -> log.info("Stripe retry attempt: {}", event.getNumberOfRetryAttempts()));
        registry.retry(EMAIL_RETRY).getEventPublisher()
                .onRetry(event -> log.info("Email retry attempt: {}", event.getNumberOfRetryAttempts()));
        registry.retry(NOTIFICATION_RETRY).getEventPublisher()
                .onRetry(event -> log.info("Notification retry attempt: {}", event.getNumberOfRetryAttempts()));
        registry.retry(DATABASE_RETRY).getEventPublisher()
                .onRetry(event -> log.info("Database retry attempt: {}", event.getNumberOfRetryAttempts()));

        return registry;
    }

    /**
     * Time limiter for Stripe operations
     */
    @Bean
    public TimeLimiter stripeTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();
        return TimeLimiter.of(config);
    }

    /**
     * Time limiter for email operations
     */
    @Bean
    public TimeLimiter emailTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .cancelRunningFuture(true)
                .build();
        return TimeLimiter.of(config);
    }

    /**
     * Time limiter for database operations
     */
    @Bean
    public TimeLimiter databaseTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();
        return TimeLimiter.of(config);
    }
}
