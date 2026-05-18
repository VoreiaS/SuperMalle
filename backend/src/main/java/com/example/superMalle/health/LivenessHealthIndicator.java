package com.example.superMalle.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Liveness Health Indicator
 *
 * Indicates if the application is running and responsive
 *
 * @author SuperMalle Team
 * @version 1.0
 */
@Component("appLivenessHealthIndicator")
@Slf4j
@RequiredArgsConstructor
public class LivenessHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Simple liveness check - if we can reach this point, the app is alive
            long uptime = System.currentTimeMillis();
            
            return Health.up()
                    .withDetail("status", "Alive")
                    .withDetail("uptime_ms", uptime)
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("Liveness health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "Dead")
                    .build();
        }
    }
}
