package com.example.superMalle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for asynchronous processing
 * 
 * Configures message queues, exchanges, and bindings for:
 * - Email sending
 * - Notifications
 * - Order processing
 * - Background jobs
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Configuration
@Slf4j
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    @Value("${rabbitmq.enabled:true}")
    private boolean rabbitMqEnabled;

    // Queue names
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String ORDER_PROCESSING_QUEUE = "order.processing.queue";
    public static final String BACKGROUND_JOB_QUEUE = "background.job.queue";
    
    // Dead letter queues
    public static final String EMAIL_DLQ = "email.dlq";
    public static final String NOTIFICATION_DLQ = "notification.dlq";
    public static final String ORDER_PROCESSING_DLQ = "order.processing.dlq";
    public static final String BACKGROUND_JOB_DLQ = "background.job.dlq";
    
    // Exchange names
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String ORDER_PROCESSING_EXCHANGE = "order.processing.exchange";
    public static final String BACKGROUND_JOB_EXCHANGE = "background.job.exchange";
    
    // Dead letter exchange
    public static final String DLX = "dead.letter.exchange";

    /**
     * Message converter for JSON serialization
     */
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with message converter
     */
    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    // ==================== Dead Letter Exchange ====================
    
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    // ==================== Email Queue ====================
    
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue emailDLQ() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_QUEUE);
    }

    @Bean
    public Binding emailDLQBinding() {
        return BindingBuilder.bind(emailDLQ())
                .to(deadLetterExchange())
                .with(EMAIL_DLQ);
    }

    // ==================== Notification Queue ====================
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ)
                .build();
    }

    @Bean
    public Queue notificationDLQ() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_QUEUE);
    }

    @Bean
    public Binding notificationDLQBinding() {
        return BindingBuilder.bind(notificationDLQ())
                .to(deadLetterExchange())
                .with(NOTIFICATION_DLQ);
    }

    // ==================== Order Processing Queue ====================
    
    @Bean
    public Queue orderProcessingQueue() {
        return QueueBuilder.durable(ORDER_PROCESSING_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_PROCESSING_DLQ)
                .build();
    }

    @Bean
    public Queue orderProcessingDLQ() {
        return QueueBuilder.durable(ORDER_PROCESSING_DLQ).build();
    }

    @Bean
    public DirectExchange orderProcessingExchange() {
        return new DirectExchange(ORDER_PROCESSING_EXCHANGE);
    }

    @Bean
    public Binding orderProcessingBinding() {
        return BindingBuilder.bind(orderProcessingQueue())
                .to(orderProcessingExchange())
                .with(ORDER_PROCESSING_QUEUE);
    }

    @Bean
    public Binding orderProcessingDLQBinding() {
        return BindingBuilder.bind(orderProcessingDLQ())
                .to(deadLetterExchange())
                .with(ORDER_PROCESSING_DLQ);
    }

    // ==================== Background Job Queue ====================
    
    @Bean
    public Queue backgroundJobQueue() {
        return QueueBuilder.durable(BACKGROUND_JOB_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", BACKGROUND_JOB_DLQ)
                .build();
    }

    @Bean
    public Queue backgroundJobDLQ() {
        return QueueBuilder.durable(BACKGROUND_JOB_DLQ).build();
    }

    @Bean
    public DirectExchange backgroundJobExchange() {
        return new DirectExchange(BACKGROUND_JOB_EXCHANGE);
    }

    @Bean
    public Binding backgroundJobBinding() {
        return BindingBuilder.bind(backgroundJobQueue())
                .to(backgroundJobExchange())
                .with(BACKGROUND_JOB_QUEUE);
    }

    @Bean
    public Binding backgroundJobDLQBinding() {
        return BindingBuilder.bind(backgroundJobDLQ())
                .to(deadLetterExchange())
                .with(BACKGROUND_JOB_DLQ);
    }
}
