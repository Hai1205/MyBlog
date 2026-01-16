package com.example.aiservice.configs;

import com.example.aiservice.services.gemini.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Complete RAG Configuration with Gemini + PGVector (Spring AI 1.0+)
 */
@Configuration
@ConfigurationProperties(prefix = "rag")
@Data
@Slf4j
public class RAGConfig {

    private int chunkSize = 500;
    private int chunkOverlap = 100;
    private int topK = 2;

    /**
     * GeminiChatModel Bean - Custom ChatModel implementation for Gemini API
     */
    @Bean
    public ChatModel geminiChatModel(GeminiService geminiService) {
        log.info("Creating GeminiChatModel bean");
        return new GeminiChatModel(geminiService);
    }

    /**
     * ChatClient Bean - Sử dụng cho RAG Generation
     */
    // @Bean
    // public ChatClient chatClient(ChatModel chatModel) {
    //     log.info("Creating ChatClient bean with Gemini ChatModel");
    //     return ChatClient.builder(chatModel)
    //             .defaultSystem("""
    //                     You are an expert CV reviewer and career coach.
    //                     Provide detailed, actionable feedback based on best practices.
    //                     Always use specific examples and metrics when possible.
    //                     """)
    //             .build();
    // }
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        log.info("Creating ChatClient bean with Gemini ChatModel");
        return ChatClient.builder(chatModel)
                .defaultSystem("You are a CV expert. Provide concise, actionable feedback.")
                .build();
    }

    /**
     * VectorStore Bean - PGVector với Gemini Embeddings (Cập nhật 1.0+)
     */
    // @Bean
    // public VectorStore vectorStore(
    //         JdbcTemplate jdbcTemplate,
    //         EmbeddingModel embeddingModel,
    //         @Value("${PGVECTOR_DIMENSION}") int dimensions,
    //         @Value("${PGVECTOR_DISTANCE_TYPE}") String distanceTypeStr,
    //         @Value("${PGVECTOR_INDEX_TYPE}") String indexTypeStr,
    //         @Value("${PGVECTOR_REMOVE_EXISTING}") boolean removeExisting,
    //         @Value("${PGVECTOR_INITIALIZE_SCHEMA}") boolean initializeSchema) {

    //     log.info("Creating PgVectorStore (Spring AI 1.0.3+) - Dimensions: {}", dimensions);

    //     PgDistanceType distanceType = switch (distanceTypeStr.toUpperCase()) {
    //         case "COSINE_DISTANCE", "COSINE" -> PgDistanceType.COSINE_DISTANCE;
    //         case "EUCLIDEAN_DISTANCE", "L2", "L2_DISTANCE" -> PgDistanceType.EUCLIDEAN_DISTANCE;
    //         case "NEGATIVE_INNER_PRODUCT", "NIP", "INNER_PRODUCT", "IP" -> PgDistanceType.NEGATIVE_INNER_PRODUCT; // SỬA:
    //                                                                                                               // Map
    //                                                                                                               // alias
    //                                                                                                               // sang
    //                                                                                                               // NEGATIVE_INNER_PRODUCT
    //         default -> PgDistanceType.COSINE_DISTANCE;
    //     };

    //     PgIndexType indexType = switch (indexTypeStr.toUpperCase()) {
    //         case "HNSW" -> PgIndexType.HNSW;
    //         case "IVFFLAT", "IVF" -> PgIndexType.IVFFLAT;
    //         default -> PgIndexType.HNSW;
    //     };

    //     return PgVectorStore.builder(jdbcTemplate, embeddingModel)
    //             .dimensions(dimensions)
    //             .distanceType(distanceType)
    //             .indexType(indexType)
    //             .initializeSchema(initializeSchema)
    //             .removeExistingVectorStoreTable(removeExisting)
    //             .build();
    // }
    @Bean
    public VectorStore vectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            @Value("${PGVECTOR_DIMENSION}") int dimensions,
            @Value("${PGVECTOR_DISTANCE_TYPE}") String distanceTypeStr,
            @Value("${PGVECTOR_INDEX_TYPE}") String indexTypeStr,
            @Value("${PGVECTOR_REMOVE_EXISTING}") boolean removeExisting,
            @Value("${PGVECTOR_INITIALIZE_SCHEMA}") boolean initializeSchema) {

        log.info("Creating PgVectorStore - Dimensions: {}", dimensions);

        PgDistanceType distanceType = switch (distanceTypeStr.toUpperCase()) {
            case "COSINE_DISTANCE", "COSINE" -> PgDistanceType.COSINE_DISTANCE;
            case "EUCLIDEAN_DISTANCE", "L2", "L2_DISTANCE" -> PgDistanceType.EUCLIDEAN_DISTANCE;
            case "NEGATIVE_INNER_PRODUCT", "NIP", "INNER_PRODUCT", "IP" -> PgDistanceType.NEGATIVE_INNER_PRODUCT;
            default -> PgDistanceType.COSINE_DISTANCE;
        };

        // OPTIMIZED: Use HNSW for faster searches
        PgIndexType indexType = PgIndexType.HNSW; // Force HNSW regardless of config

        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(dimensions)
                .distanceType(distanceType)
                .indexType(indexType)
                .initializeSchema(initializeSchema)
                .removeExistingVectorStoreTable(removeExisting)
                .build();
    }

    /**
     * Log configuration on startup
     */
    @Bean
    public String logRAGConfiguration() {
        log.info("========================================");
        log.info("RAG Configuration Initialized");
        log.info("   - Chunk Size: {}", chunkSize);
        log.info("   - Chunk Overlap: {}", chunkOverlap);
        log.info("   - Top K: {}", topK);
        log.info("========================================");
        return "RAG_CONFIGURED";
    }
}