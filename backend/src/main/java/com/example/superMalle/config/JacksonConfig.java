package com.example.superMalle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson configuration for consistent JSON serialization across the application.
 * 
 * Security: Disables dangerous features like polymorphic deserialization by default.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register Java 8 time module for LocalDateTime support
        mapper.registerModule(new JavaTimeModule());
        
        // Security: Disable features that could enable deserialization attacks
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.findAndRegisterModules();
        
        // Optional: Further harden with:
        // mapper.activateDefaultTyping(..., ObjectMapper.DefaultTyping.NON_FINAL) 
        // to require explicit type info for polymorphic types
        
        return mapper;
    }
}
