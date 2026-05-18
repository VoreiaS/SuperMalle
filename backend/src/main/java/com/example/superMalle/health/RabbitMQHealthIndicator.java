package com.example.superMalle.health;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ Health Indicator
 *
 * Monitors RabbitMQ connection health
 *
 * @author SuperMalle Team
 * @version 1.0
 */
@Component
@ConditionalOnBean(ConnectionFactory.class)
@Slf4j
@RequiredArgsConstructor
public class RabbitMQHealthIndicator implements HealthIndicator {

    private final ConnectionFactory connectionFactory;

    @Override
    public Health health() {
        try {
            // Get connection information from factory
            String host = connectionFactory.getHost();
            int port = connectionFactory.getPort();
            
            // Create a test connection
            Connection connection = connectionFactory.newConnection();
            boolean isOpen = connection.isOpen();
            
            connection.close();
            
            if (isOpen) {
                return Health.up()
                        .withDetail("rabbitmq", "Connected")
                        .withDetail("host", host)
                        .withDetail("port", port)
                        .withDetail("status", "Open")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "RabbitMQ connection is not open")
                        .build();
            }
        } catch (Exception e) {
            log.error("RabbitMQ health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("rabbitmq", "Disconnected")
                    .build();
        }
    }
}
