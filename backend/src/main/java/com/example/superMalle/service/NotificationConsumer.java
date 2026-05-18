package com.example.superMalle.service;

import com.example.superMalle.config.RabbitMQConfig;
import com.example.superMalle.dto.notification.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationMessage message) {
        log.info("Received notification from queue: userId={}, type={}, title={}",
                message.getUserId(), message.getType(), message.getTitle());

        try {
            if (message.getUserId() != null) {
                messagingTemplate.convertAndSendToUser(
                        message.getUserId().toString(),
                        "/queue/notifications",
                        message
                );
            } else {
                messagingTemplate.convertAndSend(
                        "/topic/notifications",
                        message
                );
            }
            log.info("Notification delivered: type={}, userId={}", message.getType(), message.getUserId());
        } catch (Exception e) {
            log.error("Failed to deliver notification: type={}, userId={}, error={}",
                    message.getType(), message.getUserId(), e.getMessage(), e);
        }
    }
}
