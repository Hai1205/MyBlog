package com.example.rediscommon.configs;

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

            // Parse username and password from userInfo (format: username:password)
            if (userInfo != null && !userInfo.isEmpty()) {
                String[] credentials = userInfo.split(":", 2);
                if (credentials.length == 2) {
                    config.setUsername(credentials[0]);
                    config.setPassword(credentials[1]);
                } else {
                    // Only password provided
                    config.setPassword(userInfo);
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
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
