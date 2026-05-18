package com.example.superMalle.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Redis Health Indicator
 * 
 * Monitors Redis connection health
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Component
@ConditionalOnBean(RedisConnectionFactory.class)
@Slf4j
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            var connection = redisConnectionFactory.getConnection();
            
            // Get Redis server information
            String ping = connection.ping();
            Properties serverInfo = connection.info("server");
            
            // Parse server info
            String redisVersion = serverInfo.getProperty("redis_version", "unknown");
            String uptime = serverInfo.getProperty("uptime_in_days", "unknown");
            String connectedClients = serverInfo.getProperty("connected_clients", "unknown");
            String usedMemory = serverInfo.getProperty("used_memory_human", "unknown");
            
            connection.close();
            
            if ("PONG".equals(ping)) {
                return Health.up()
                        .withDetail("redis", "Connected")
                        .withDetail("version", redisVersion)
                        .withDetail("uptime_days", uptime)
                        .withDetail("connected_clients", connectedClients)
                        .withDetail("used_memory", usedMemory)
                        .withDetail("status", "PONG")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Redis ping failed")
                        .withDetail("ping", ping)
                        .build();
            }
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("redis", "Disconnected")
                    .build();
        }
    }
}
