package com.example.superMalle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Logging Configuration
 * 
 * Configures structured logging with correlation IDs
 * Sets up request logging and response logging
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Configuration
@Slf4j
public class LoggingConfig {

    /**
     * Request logging filter
     * Logs all incoming HTTP requests with correlation IDs
     */
    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        filter.setAfterMessageSuffix("");
        return filter;
    }
}
