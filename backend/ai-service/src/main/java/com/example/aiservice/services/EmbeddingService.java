package com.example.aiservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Enhanced Embedding Service with Gemini Embeddings + PGVector
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    // Cache configuration: 5 minutes TTL, max 100 entries
    @Cacheable(value = "embeddingCache", key = "#text.hashCode()")
    public float[] embedWithCache(String text) {
        return embeddingModel.embed(text);
    }

    /**
     * OPTIMIZED: Parallel search for multiple sections
     * Reduces latency from sequential to concurrent execution
     */
    public Map<String, List<Document>> searchMultipleSectionsParallel(
            Map<String, String> sectionQueries,
            String category,
            String level,
            int topK) {

        log.info("Starting parallel search for {} sections", sectionQueries.size());
        long startTime = System.currentTimeMillis();

        // Execute all searches in parallel
        Map<String, CompletableFuture<List<Document>>> futures = sectionQueries.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> CompletableFuture.supplyAsync(() ->
                                searchRelevantTemplates(
                                        entry.getValue(),
                                        entry.getKey(),
                                        category,
                                        level,
                                        topK))));

        // Wait for all to complete and collect results
        Map<String, List<Document>> results = futures.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().join()));

        long duration = System.currentTimeMillis() - startTime;
        log.info("Parallel search completed in {}ms", duration);

        return results;
    }

    /**
     * OPTIMIZED: Search with relaxed threshold and caching
     */
    @Cacheable(value = "searchCache", key = "#query.hashCode() + '-' + #section + '-' + #category + '-' + #level")
    public List<Document> searchRelevantTemplates(
            String query,
            String section,
            String category,
            String level,
            int topK) {

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        try {
            // Build minimal filter - only essential criteria
            StringBuilder filterExpr = new StringBuilder();
            filterExpr.append(String.format("section == '%s'", section));

            // Only add category if specified
            if (category != null && !category.isEmpty() && !"general".equals(category)) {
                filterExpr.append(String.format(" AND category == '%s'", category));
            }

            // OPTIMIZATION: Relaxed rating threshold
            filterExpr.append(" AND rating >= 3"); // Changed from 4 to 3

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(0.3) // OPTIMIZATION: Lowered from 0.5 to 0.3
                    .filterExpression(filterExpr.toString())
                    .build();

            List<Document> results = vectorStore.similaritySearch(searchRequest);

            log.debug("Found {} results for section: {}", results.size(), section);
            return results;

        } catch (Exception e) {
            log.error("Search failed for section {}: {}", section, e.getMessage());
            return List.of();
        }
    }

    /**
     * OPTIMIZED: Batch ingest with progress tracking
     */
    public void batchIngestTemplates(List<Document> documents) {
        log.info("Batch ingesting {} templates", documents.size());

        if (documents.isEmpty()) {
            return;
        }

        try {
            // Split into smaller batches to avoid timeout
            int batchSize = 50;
            for (int i = 0; i < documents.size(); i += batchSize) {
                int end = Math.min(i + batchSize, documents.size());
                List<Document> batch = documents.subList(i, end);
                
                vectorStore.add(batch);
                log.info("Ingested batch {}/{}", end, documents.size());
            }

            log.info("Batch ingest completed successfully");

        } catch (Exception e) {
            log.error("Batch ingest failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch ingest templates", e);
        }
    }

    /**
     * Simple ingest without caching
     */
    public void ingestTemplate(String id, String content, Map<String, Object> metadata) {
        Document document = new Document(id, content, metadata);
        vectorStore.add(List.of(document));
    }

    /**
     * Delete template
     */
    public void deleteTemplate(String id) {
        try {
            vectorStore.delete(List.of(id));
        } catch (Exception e) {
            log.error("Failed to delete template {}: {}", id, e.getMessage());
        }
    }

    /**
     * Update template
     */
    public void updateTemplate(String id, String newContent, Map<String, Object> metadata) {
        try {
            deleteTemplate(id);
            ingestTemplate(id, newContent, metadata);
        } catch (Exception e) {
            log.error("Failed to update template {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update template", e);
        }
    }
}