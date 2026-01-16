package com.example.aiservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Enhanced Embedding Service with Gemini Embeddings + PGVector
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    /**
     * Test embedding - verify Gemini embedding works
     * 
     * @return Vector dimensions and sample values
     */
    public Map<String, Object> testEmbedding(String text) {
        try {
            log.info("Testing embedding for text: {}", text.substring(0, Math.min(50, text.length())));

            // Call Gemini embedding
            float[] embedding = embeddingModel.embed(text);

            // Convert to List for response
            List<Float> embeddingList = new ArrayList<>();
            for (float f : embedding) {
                embeddingList.add(f);
            }

            log.info("Embedding generated: dimension={}", embedding.length);

            return Map.of(
                    "dimension", embedding.length,
                    "sampleFirst5", embeddingList.subList(0, Math.min(5, embeddingList.size())),
                    "sampleLast5", embeddingList.subList(
                            Math.max(0, embeddingList.size() - 5),
                            embeddingList.size()),
                    "success", true);

        } catch (Exception e) {
            log.error("Embedding test failed: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage());
        }
    }

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

    /**
     * Alternative: Test with EmbeddingResponse for more details
     */
    public Map<String, Object> testEmbeddingDetailed(String text) {
        try {
            log.info("Testing detailed embedding...");

            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));

            if (response.getResults().isEmpty()) {
                return Map.of("success", false, "error", "No embedding results");
            }

            float[] embedding = response.getResults().get(0).getOutput();

            log.info("Embedding response: dimension={}, metadata={}",
                    embedding.length,
                    response.getMetadata());

            // Convert first 10 values for preview
            List<Float> preview = new ArrayList<>();
            for (int i = 0; i < Math.min(10, embedding.length); i++) {
                preview.add(embedding[i]);
            }

            return Map.of(
                    "success", true,
                    "dimension", embedding.length,
                    "preview", preview,
                    "metadata", response.getMetadata() != null ? response.getMetadata() : Map.of());

        } catch (Exception e) {
            log.error("Detailed embedding test failed: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage());
        }
    }

    /**
     * Ingest template into vector store
     */
    // public void ingestTemplate(String id, String content, Map<String, Object> metadata) {
    //     log.debug("Ingesting template {} into vector store", id);

    //     Document document = new Document(id, content, metadata);
    //     vectorStore.add(List.of(document));

    //     log.debug("Template {} ingested successfully", id);
    // }

    /**
     * Batch ingest multiple templates
     */
    // public void batchIngestTemplates(List<Document> documents) {
    //     log.info("Batch ingesting {} templates", documents.size());

    //     if (documents.isEmpty()) {
    //         log.warn("No documents to ingest");
    //         return;
    //     }

    //     try {
    //         long startTime = System.currentTimeMillis();

    //         vectorStore.add(documents);

    //         long duration = System.currentTimeMillis() - startTime;
    //         log.info("Batch ingest completed: {} templates in {}ms", documents.size(), duration);

    //     } catch (Exception e) {
    //         log.error("Batch ingest failed: {}", e.getMessage(), e);
    //         throw new RuntimeException("Failed to batch ingest templates", e);
    //     }
    // }

    /**
     * Search relevant templates with filters - CORE RAG RETRIEVE
     */
    // public List<Document> searchRelevantTemplates(
    //         String query,
    //         String section,
    //         String category,
    //         String level,
    //         int topK) {

    //     log.debug("Searching templates: section={}, category={}, level={}, topK={}",
    //             section, category, level, topK);

    //     if (query == null || query.trim().isEmpty()) {
    //         log.warn("Empty query, returning empty results");
    //         return List.of();
    //     }

    //     try {
    //         long startTime = System.currentTimeMillis();

    //         // Build filter expression - PGVector syntax
    //         StringBuilder filterExpr = new StringBuilder();
    //         filterExpr.append(String.format("section == '%s'", section));

    //         if (category != null && !category.isEmpty()) {
    //             filterExpr.append(String.format(" AND category == '%s'", category));
    //         }

    //         if (level != null && !level.isEmpty()) {
    //             filterExpr.append(String.format(" AND level == '%s'", level));
    //         }

    //         // Only high-quality templates
    //         filterExpr.append(" AND rating >= 4");

    //         log.debug("Filter expression: {}", filterExpr.toString());

    //         SearchRequest searchRequest = SearchRequest.builder()
    //                 .query(query)
    //                 .topK(topK)
    //                 .similarityThreshold(0.5) // Lowered from 0.7
    //                 .filterExpression(filterExpr.toString())
    //                 .build();

    //         List<Document> results = vectorStore.similaritySearch(searchRequest);

    //         long duration = System.currentTimeMillis() - startTime;

    //         if (results.isEmpty()) {
    //             log.warn("No results found for query. Consider:");
    //             log.warn("   - Lowering similarity threshold (current: 0.5)");
    //             log.warn("   - Removing some filters");
    //             log.warn("   - Checking if templates are actually in vector store");
    //             return List.of();
    //         }

    //         log.info("Found {} relevant templates in {}ms", results.size(), duration);

    //         // Log top results for debugging
    //         if (!results.isEmpty() && log.isDebugEnabled()) {
    //             log.debug("Top result preview:");
    //             Document topDoc = results.get(0);
    //             String preview = topDoc.getText().substring(0, Math.min(100, topDoc.getText().length()));
    //             log.debug("   Content: {}...", preview);
    //             log.debug("   Metadata: {}", topDoc.getMetadata());
    //         }

    //         return results;

    //     } catch (Exception e) {
    //         log.error("Search failed: {}", e.getMessage(), e);
    //         log.error("   Query: {}", query.substring(0, Math.min(100, query.length())));
    //         return List.of();
    //     }
    // }

    /**
     * Delete template
     */
    // public void deleteTemplate(String id) {
    //     try {
    //         vectorStore.delete(List.of(id));
    //         log.info("Deleted template {} from vector store", id);
    //     } catch (Exception e) {
    //         log.error("Failed to delete template {}: {}", id, e.getMessage(), e);
    //     }
    // }

    /**
     * Update template (delete + re-insert)
     */
    // public void updateTemplate(String id, String newContent, Map<String, Object> metadata) {
    //     try {
    //         deleteTemplate(id);
    //         ingestTemplate(id, newContent, metadata);
    //         log.info("Updated template {} in vector store", id);
    //     } catch (Exception e) {
    //         log.error("Failed to update template {}: {}", id, e.getMessage(), e);
    //         throw new RuntimeException("Failed to update template", e);
    //     }
    // }
}