package com.example.rediscommon.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Global Jackson configuration for all microservices
 * This prevents JSON serialization with class names and ensures clean JSON
 * output
 */
@Configuration
public class JacksonConfig {

    /**
     * Primary ObjectMapper bean for HTTP REST endpoints
     * Disables type information to avoid class names in JSON responses
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Register Java 8 date/time module
        objectMapper.registerModule(new JavaTimeModule());

        // Disable typing to avoid class names in JSON
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Disable default typing completely for REST responses
        objectMapper.deactivateDefaultTyping();

        return objectMapper;
    }
}
