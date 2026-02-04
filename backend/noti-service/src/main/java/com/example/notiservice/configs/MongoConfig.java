package com.example.notiservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.example.notiservice.repositories")
public class MongoConfig {
    
    /**
     * Configure custom converters for MongoDB
     * Useful for handling UUID and other custom types
     */
    @Bean
    public MongoCustomConversions customConversions() {
        List<Object> converters = new ArrayList<>();
        // Add custom converters here if needed
        // For example: UUID converters, Enum converters, etc.
        return new MongoCustomConversions(converters);
    }

    /**
     * Enable transaction support for MongoDB
     * Note: Requires MongoDB 4.0+ and replica set configuration
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
