package com.example.superMalle.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Readiness Health Indicator
 *
 * Indicates if the application is ready to accept traffic
 * Checks database, Redis, and RabbitMQ connectivity
 *
 * @author SuperMalle Team
 * @version 1.0
 */
@Component("appReadinessHealthIndicator")
@Slf4j
@RequiredArgsConstructor
public class ReadinessHealthIndicator implements HealthIndicator {

    private final DatabaseHealthIndicator databaseHealthIndicator;
    private final Optional<RedisHealthIndicator> redisHealthIndicator;
    private final Optional<RabbitMQHealthIndicator> rabbitMQHealthIndicator;

    @Override
    public Health health() {
        try {
            // Check database health
            Health dbHealth = databaseHealthIndicator.health();
            
            // Redis is optional
            boolean redisUp = redisHealthIndicator
                    .map(indicator -> indicator.health().getStatus().equals(Status.UP))
                    .orElse(true);
            
            // Application is ready if all critical services are up
            boolean dbUp = dbHealth.getStatus().equals(Status.UP);
            boolean rabbitMQUp = rabbitMQHealthIndicator
                    .map(indicator -> indicator.health().getStatus().equals(Status.UP))
                    .orElse(true); // RabbitMQ is optional
            
            if (dbUp && redisUp && rabbitMQUp) {
                Health.Builder builder = Health.up()
                        .withDetail("database", "Ready")
                        .withDetail("status", "Ready");
                
                redisHealthIndicator.ifPresent(indicator -> 
                        builder.withDetail("redis", "Ready"));
                
                rabbitMQHealthIndicator.ifPresent(indicator -> 
                        builder.withDetail("rabbitmq", "Ready"));
                
                return builder.build();
            } else {
                Health.Builder builder = Health.down()
                        .withDetail("database", dbUp ? "Ready" : "Not Ready")
                        .withDetail("status", "Not Ready");
                
                redisHealthIndicator.ifPresent(indicator -> 
                        builder.withDetail("redis", redisUp ? "Ready" : "Not Ready"));
                
                rabbitMQHealthIndicator.ifPresent(indicator -> 
                        builder.withDetail("rabbitmq", rabbitMQUp ? "Ready" : "Not Ready"));
                
                return builder.build();
            }
        } catch (Exception e) {
            log.error("Readiness health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "Not Ready")
                    .build();
        }
    }
}
