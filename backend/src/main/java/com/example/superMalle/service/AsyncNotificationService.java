package com.example.superMalle.service;

import com.example.superMalle.config.RabbitMQConfig;
import com.example.superMalle.dto.notification.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Asynchronous Notification Service
 * 
 * Sends notifications asynchronously using RabbitMQ message queue
 * Supports multiple notification types (in-app, push, SMS)
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncNotificationService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Send notification asynchronously
     * 
     * @param userId User ID
     * @param type Notification type
     * @param title Notification title
     * @param message Notification message
     * @param data Additional data
     */
    @Async
    public void sendNotificationAsync(Long userId, String type, String title, String message, Map<String, Object> data) {
        try {
            NotificationMessage notificationMessage = NotificationMessage.builder()
                    .userId(userId)
                    .type(type)
                    .title(title)
                    .message(message)
                    .data(data)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_QUEUE,
                    notificationMessage
            );

            log.info("Notification queued: userId={}, type={}, title={}", userId, type, title);
        } catch (Exception e) {
            log.error("Failed to queue notification: userId={}, type={}, title={}", userId, type, title, e);
            throw new RuntimeException("Failed to queue notification", e);
        }
    }

    /**
     * Send order status notification asynchronously
     * 
     * @param userId User ID
     * @param orderId Order ID
     * @param status New order status
     */
    @Async
    public void sendOrderStatusNotificationAsync(Long userId, Long orderId, String status) {
        try {
            NotificationMessage notificationMessage = NotificationMessage.builder()
                    .userId(userId)
                    .type("ORDER_STATUS_UPDATE")
                    .title("Order Status Updated")
                    .message("Your order #" + orderId + " status is now: " + status)
                    .data(Map.of(
                            "orderId", orderId,
                            "status", status
                    ))
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_QUEUE,
                    notificationMessage
            );

            log.info("Order status notification queued: userId={}, orderId={}, status={}", userId, orderId, status);
        } catch (Exception e) {
            log.error("Failed to queue order status notification: userId={}, orderId={}, status={}", 
                    userId, orderId, status, e);
            throw new RuntimeException("Failed to queue order status notification", e);
        }
    }

    /**
     * Send low stock notification asynchronously
     * 
     * @param menuItemId Menu item ID
     * @param itemName Item name
     * @param currentQuantity Current quantity
     * @param reorderLevel Reorder level
     */
    @Async
    public void sendLowStockNotificationAsync(Long menuItemId, String itemName, int currentQuantity, int reorderLevel) {
        try {
            NotificationMessage notificationMessage = NotificationMessage.builder()
                    .userId(null) // System notification
                    .type("LOW_STOCK_ALERT")
                    .title("Low Stock Alert")
                    .message("Item '" + itemName + "' is running low on stock. Current: " + currentQuantity + ", Reorder level: " + reorderLevel)
                    .data(Map.of(
                            "menuItemId", menuItemId,
                            "itemName", itemName,
                            "currentQuantity", currentQuantity,
                            "reorderLevel", reorderLevel
                    ))
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_QUEUE,
                    notificationMessage
            );

            log.info("Low stock notification queued: menuItemId={}, itemName={}, currentQuantity={}", 
                    menuItemId, itemName, currentQuantity);
        } catch (Exception e) {
            log.error("Failed to queue low stock notification: menuItemId={}, itemName={}", menuItemId, itemName, e);
            throw new RuntimeException("Failed to queue low stock notification", e);
        }
    }

    /**
     * Send loyalty points notification asynchronously
     * 
     * @param userId User ID
     * @param pointsEarned Points earned
     * @param totalPoints Total points balance
     */
    @Async
    public void sendLoyaltyPointsNotificationAsync(Long userId, int pointsEarned, int totalPoints) {
        try {
            NotificationMessage notificationMessage = NotificationMessage.builder()
                    .userId(userId)
                    .type("LOYALTY_POINTS_EARNED")
                    .title("Loyalty Points Earned!")
                    .message("You earned " + pointsEarned + " points! Total balance: " + totalPoints)
                    .data(Map.of(
                            "pointsEarned", pointsEarned,
                            "totalPoints", totalPoints
                    ))
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_QUEUE,
                    notificationMessage
            );

            log.info("Loyalty points notification queued: userId={}, pointsEarned={}, totalPoints={}", 
                    userId, pointsEarned, totalPoints);
        } catch (Exception e) {
            log.error("Failed to queue loyalty points notification: userId={}, pointsEarned={}", 
                    userId, pointsEarned, e);
            throw new RuntimeException("Failed to queue loyalty points notification", e);
        }
    }

    /**
     * Send promotional notification asynchronously
     * 
     * @param userId User ID (null for broadcast)
     * @param title Notification title
     * @param message Notification message
     * @param promotionId Promotion ID
     */
    @Async
    public void sendPromotionalNotificationAsync(Long userId, String title, String message, Long promotionId) {
        try {
            NotificationMessage notificationMessage = NotificationMessage.builder()
                    .userId(userId)
                    .type("PROMOTION")
                    .title(title)
                    .message(message)
                    .data(Map.of("promotionId", promotionId))
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_QUEUE,
                    notificationMessage
            );

            log.info("Promotional notification queued: userId={}, title={}", userId, title);
        } catch (Exception e) {
            log.error("Failed to queue promotional notification: userId={}, title={}", userId, title, e);
            throw new RuntimeException("Failed to queue promotional notification", e);
        }
    }
}
