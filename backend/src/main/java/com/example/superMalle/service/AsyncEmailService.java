package com.example.superMalle.service;

import com.example.superMalle.config.RabbitMQConfig;
import com.example.superMalle.dto.email.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Asynchronous Email Service
 * 
 * Sends emails asynchronously using RabbitMQ message queue
 * Improves performance by offloading email sending to background workers
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncEmailService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Send email asynchronously
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param templateName Email template name
     * @param context Template context variables
     */
    @Async
    public void sendEmailAsync(String to, String subject, String templateName, Object context) {
        try {
            EmailMessage emailMessage = EmailMessage.builder()
                    .to(to)
                    .subject(subject)
                    .templateName(templateName)
                    .context(context)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_QUEUE,
                    emailMessage
            );

            log.info("Email queued for sending: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("Failed to queue email: to={}, subject={}", to, subject, e);
            throw new RuntimeException("Failed to queue email", e);
        }
    }

    /**
     * Send order confirmation email asynchronously
     * 
     * @param to Recipient email address
     * @param orderId Order ID
     * @param orderDetails Order details
     */
    @Async
    public void sendOrderConfirmationAsync(String to, Long orderId, Object orderDetails) {
        try {
            EmailMessage emailMessage = EmailMessage.builder()
                    .to(to)
                    .subject("Order Confirmation - Order #" + orderId)
                    .templateName("order-confirmation")
                    .context(orderDetails)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_QUEUE,
                    emailMessage
            );

            log.info("Order confirmation email queued: to={}, orderId={}", to, orderId);
        } catch (Exception e) {
            log.error("Failed to queue order confirmation email: to={}, orderId={}", to, orderId, e);
            throw new RuntimeException("Failed to queue order confirmation email", e);
        }
    }

    /**
     * Send welcome email asynchronously
     * 
     * @param to Recipient email address
     * @param userName User name
     */
    @Async
    public void sendWelcomeEmailAsync(String to, String userName) {
        try {
            EmailMessage emailMessage = EmailMessage.builder()
                    .to(to)
                    .subject("Welcome to SuperMalle Restaurant!")
                    .templateName("welcome")
                    .context(Map.of("userName", userName))
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_QUEUE,
                    emailMessage
            );

            log.info("Welcome email queued: to={}, userName={}", to, userName);
        } catch (Exception e) {
            log.error("Failed to queue welcome email: to={}, userName={}", to, userName, e);
            throw new RuntimeException("Failed to queue welcome email", e);
        }
    }

    /**
     * Send order status update email asynchronously
     * 
     * @param to Recipient email address
     * @param orderId Order ID
     * @param status New order status
     */
    @Async
    public void sendOrderStatusUpdateAsync(String to, Long orderId, String status) {
        try {
            EmailMessage emailMessage = EmailMessage.builder()
                    .to(to)
                    .subject("Order Status Update - Order #" + orderId)
                    .templateName("order-status-update")
                    .context(Map.of(
                            "orderId", orderId,
                            "status", status
                    ))
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_QUEUE,
                    emailMessage
            );

            log.info("Order status update email queued: to={}, orderId={}, status={}", to, orderId, status);
        } catch (Exception e) {
            log.error("Failed to queue order status update email: to={}, orderId={}, status={}", 
                    to, orderId, status, e);
            throw new RuntimeException("Failed to queue order status update email", e);
        }
    }

    /**
     * Send promotional email asynchronously
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param content Email content
     */
    @Async
    public void sendPromotionalEmailAsync(String to, String subject, String content) {
        try {
            EmailMessage emailMessage = EmailMessage.builder()
                    .to(to)
                    .subject(subject)
                    .templateName("promotional")
                    .context(Map.of("content", content))
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_QUEUE,
                    emailMessage
            );

            log.info("Promotional email queued: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("Failed to queue promotional email: to={}, subject={}", to, subject, e);
            throw new RuntimeException("Failed to queue promotional email", e);
        }
    }
}
