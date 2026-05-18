package com.example.superMalle.service;

import com.example.superMalle.config.RabbitMQConfig;
import com.example.superMalle.entity.Order;
import com.example.superMalle.entity.enums.OrderStatus;
import com.example.superMalle.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProcessingConsumer {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = RabbitMQConfig.ORDER_PROCESSING_QUEUE)
    public void handleOrderProcessing(Map<String, Object> message) {
        Long orderId = message != null ? (Long) message.get("orderId") : null;
        if (orderId == null) {
            log.warn("Received order processing message without orderId");
            return;
        }

        log.info("Processing order asynchronously: orderId={}", orderId);

        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                log.warn("Order not found for async processing: orderId={}", orderId);
                return;
            }

            if (order.getStatus() != OrderStatus.PENDING) {
                log.info("Order {} is no longer pending, skipping async processing", orderId);
                return;
            }

            log.info("Async order processing complete for orderId={}", orderId);
        } catch (Exception e) {
            log.error("Failed to process order asynchronously: orderId={}, error={}",
                    orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to process order", e);
        }
    }
}
