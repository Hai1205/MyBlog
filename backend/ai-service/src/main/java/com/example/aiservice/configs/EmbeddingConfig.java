package com.example.aiservice.configs;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmbeddingConfig {

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    @Value("${EMBEDDING_API_URL}")
    private String embeddingApiUrl;

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return new GeminiEmbeddingModelConfig(geminiApiKey, embeddingApiUrl);
    }
}
