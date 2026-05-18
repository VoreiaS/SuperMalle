package com.example.superMalle.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Database Health Indicator
 * 
 * Monitors database connection health
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            // Test database connection
            String result = jdbcTemplate.queryForObject("SELECT 1", String.class);
            
            if ("1".equals(result)) {
                // Get database metadata
                String databaseName = jdbcTemplate.queryForObject(
                        "SELECT current_database()", String.class);
                String databaseVersion = jdbcTemplate.queryForObject(
                        "SELECT version()", String.class);
                
                return Health.up()
                        .withDetail("database", "Connected")
                        .withDetail("name", databaseName)
                        .withDetail("version", databaseVersion)
                        .withDetail("status", "Healthy")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Database query returned unexpected result")
                        .build();
            }
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("database", "Disconnected")
                    .build();
        }
    }
}
