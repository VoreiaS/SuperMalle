package com.example.superMalle.service;

import com.example.superMalle.config.RabbitMQConfig;
import com.example.superMalle.dto.email.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmail(EmailMessage message) {
        log.info("Received email from queue: to={}, subject={}, template={}",
                message.getTo(), message.getSubject(), message.getTemplateName());

        try {
            if (message.getContext() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> context = (Map<String, Object>) message.getContext();
                emailService.sendHtmlEmail(
                        message.getTo(),
                        message.getSubject(),
                        message.getTemplateName(),
                        context
                );
            } else {
                emailService.sendHtmlEmail(
                        message.getTo(),
                        message.getSubject(),
                        message.getTemplateName(),
                        message.getContext() != null ? Map.of("data", message.getContext()) : Map.of()
                );
            }
            log.info("Email sent successfully: to={}, subject={}", message.getTo(), message.getSubject());
        } catch (Exception e) {
            log.error("Failed to send email: to={}, subject={}, error={}",
                    message.getTo(), message.getSubject(), e.getMessage(), e);
            throw new RuntimeException("Failed to process email message", e);
        }
    }
}
