package com.example.rediscommon.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.URI;
import java.time.Duration;

/**
 * Redis configuration for all microservices
 * Provides RedisTemplate with proper serialization settings
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url}")
    private String redisUrl;

    @Value("${spring.data.redis.timeout}")
    private long timeout;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            URI uri = URI.create(redisUrl);

            // Parse connection details from URL
            String host = uri.getHost();
            int port = uri.getPort();
            String userInfo = uri.getUserInfo();

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);

            // Parse username and password from userInfo (format: username:password or
            // :password)
            if (userInfo != null && !userInfo.isEmpty()) {
                // Handle format ":password" when username is empty (e.g.,
                // redis://:pass@host:port)
                if (userInfo.startsWith(":")) {
                    String password = userInfo.substring(1);
                    if (!password.isEmpty()) {
                        config.setPassword(password);
                    }
                } else {
                    String[] credentials = userInfo.split(":", 2);
                    if (credentials.length == 2) {
                        if (!credentials[0].isEmpty()) {
                            config.setUsername(credentials[0]);
                        }
                        if (!credentials[1].isEmpty()) {
                            config.setPassword(credentials[1]);
                        }
                    } else if (!credentials[0].isEmpty()) {
                        // Only password provided without colon
                        config.setPassword(credentials[0]);
                    }
                }
            }

            // Configure Lettuce client
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                    .commandTimeout(Duration.ofMillis(timeout))
                    .build();

            return new LettuceConnectionFactory(config, clientConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure Redis connection: " + e.getMessage(), e);
        }
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Enable default typing for proper deserialization from Redis
        // This adds type information to JSON so Redis can deserialize back to correct
        // types
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Create JSON serializer with custom ObjectMapper
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        // Use JSON serializer for values
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
