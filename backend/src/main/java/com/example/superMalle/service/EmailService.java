package com.example.superMalle.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.restaurant.name}")
    private String restaurantName;

    /**
     * Send simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
        }
    }

    /**
     * Send HTML email using Thymeleaf template
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(fromEmail, fromName);
            } catch (java.io.UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
            context.setVariable("restaurantName", restaurantName);

            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent to: {} using template: {}", to, templateName);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
        }
    }

    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmation(String to, String orderNumber, Double totalAmount, 
                                       String deliveryAddress, String estimatedTime) {
        Map<String, Object> variables = Map.of(
                "orderNumber", orderNumber,
                "totalAmount", totalAmount,
                "deliveryAddress", deliveryAddress,
                "estimatedTime", estimatedTime
        );
        sendHtmlEmail(to, "Order Confirmation - " + orderNumber, "emails/order-confirmation", variables);
    }

    /**
     * Send order status update email
     */
    public void sendOrderStatusUpdate(String to, String orderNumber, String status, String message) {
        Map<String, Object> variables = Map.of(
                "orderNumber", orderNumber,
                "status", status,
                "message", message
        );
        sendHtmlEmail(to, "Order Status Update - " + orderNumber, "emails/order-status-update", variables);
    }

    /**
     * Send welcome email
     */
    public void sendWelcomeEmail(String to, String name) {
        Map<String, Object> variables = Map.of(
                "name", name
        );
        sendHtmlEmail(to, "Welcome to " + restaurantName + "!", "emails/welcome", variables);
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        Map<String, Object> variables = Map.of(
                "name", name,
                "resetLink", resetLink
        );
        sendHtmlEmail(to, "Password Reset Request", "emails/password-reset", variables);
    }

    /**
     * Send loyalty points earned email
     */
    public void sendLoyaltyPointsEarned(String to, String name, Integer points, Integer newBalance) {
        Map<String, Object> variables = Map.of(
                "name", name,
                "points", points,
                "newBalance", newBalance
        );
        sendHtmlEmail(to, "You Earned Loyalty Points!", "emails/loyalty-points-earned", variables);
    }

    /**
     * Send loyalty tier upgrade email
     */
    public void sendLoyaltyTierUpgrade(String to, String name, String oldTier, String newTier) {
        Map<String, Object> variables = Map.of(
                "name", name,
                "oldTier", oldTier,
                "newTier", newTier
        );
        sendHtmlEmail(to, "Congratulations! You've Been Upgraded to " + newTier, "emails/loyalty-tier-upgrade", variables);
    }

    /**
     * Send promotional email
     */
    public void sendPromotionalEmail(String to, String subject, String title, String content, String promoCode) {
        Map<String, Object> variables = Map.of(
                "title", title,
                "content", content,
                "promoCode", promoCode
        );
        sendHtmlEmail(to, subject, "emails/promotional", variables);
    }

    /**
     * Send low stock alert email to admin
     */
    public void sendLowStockAlert(String to, String menuItemName, Integer currentQuantity, Integer reorderLevel) {
        Map<String, Object> variables = Map.of(
                "menuItemName", menuItemName,
                "currentQuantity", currentQuantity,
                "reorderLevel", reorderLevel
        );
        sendHtmlEmail(to, "Low Stock Alert: " + menuItemName, "emails/low-stock-alert", variables);
    }

    /**
     * Send order modification request email to admin
     */
    public void sendOrderModificationRequest(String to, String orderNumber, String modificationType, String customerName) {
        Map<String, Object> variables = Map.of(
                "orderNumber", orderNumber,
                "modificationType", modificationType,
                "customerName", customerName
        );
        sendHtmlEmail(to, "Order Modification Request: " + orderNumber, "emails/order-modification-request", variables);
    }
}
