package com.example.aiservice.configs;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;

@Configuration
public class VectorStoreConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void enableExtensions() {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS hstore");
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");
            System.out.println("Extensions enabled successfully");
        } catch (Exception e) {
            System.err.println("Failed to create extensions: " + e.getMessage());
            throw new RuntimeException("Required PostgreSQL extensions are not available. Please ensure pgvector is installed on your database server.", e);
        }
    }

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            .initializeSchema(true)
            .build();
    }

    @Bean
    @Primary
    public ChatModel chatModel(ChatModel openAiChatModel) {
        return openAiChatModel;
    }
}