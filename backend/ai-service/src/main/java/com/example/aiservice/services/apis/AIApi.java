package com.example.aiservice.services.apis;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;

import com.example.aiservice.dtos.ExtractionResult;
import com.example.aiservice.dtos.requests.*;
import com.example.aiservice.dtos.responses.Response;
import com.example.aiservice.exceptions.OurException;
import com.example.aiservice.services.*;
import com.example.rediscommon.services.RateLimiterService;
import com.example.rediscommon.services.RedisService;
import com.fasterxml.jackson.databind.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Architecture:
 * - RETRIEVE: EnhancedEmbeddingService tìm relevant templates từ PGVector
 * - AUGMENT: Build prompt với examples từ knowledge base
 * - GENERATE: ChatClient (Gemini) tạo response dựa trên augmented prompt
 */
@Service
@Slf4j
public class AIApi extends BaseApi {

    // Core RAG Components
    private final ChatClient chatClient; // Gemini Chat Model
    private final EmbeddingService embeddingService; // Vector Search
    // Engineering
    private final CompactPromptBuilder promptBuilder;

    // Image Preservation
    private final ImagePreservationService imagePreservationService;

    // Supporting Services
    private final ObjectMapper objectMapper;

    // Redis Services
    private final RedisService redisService;
    private final RateLimiterService rateLimiterService;

    /**
     * Constructor - Inject all dependencies
     */
    public AIApi(
            ChatClient chatClient, // Inject từ RAGConfig
            EmbeddingService embeddingService,
            CompactPromptBuilder promptBuilder,
            ImagePreservationService imagePreservationService,
            RedisService redisService,
            RateLimiterService rateLimiterService) {

        this.chatClient = chatClient;
        this.embeddingService = embeddingService;
        this.promptBuilder = promptBuilder;
        this.imagePreservationService = imagePreservationService;
        this.redisService = redisService;
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = new ObjectMapper();
    }

    // ========================================
    // PUBLIC API METHODS
    // ========================================

    public Response analyzeTitle(String dataJson) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: API Create/Update - 30-60 req/min
            if (!rateLimiterService.isAllowed("ai:title", 45, 60)) {
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            AIRequest request = objectMapper.readValue(dataJson, AIRequest.class);
            String aiTitle = handleanalyzeTitle(request.getTitle());

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("AI title generated successfully");
            response.setTitle(aiTitle);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in analyzeTitle: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error generating AI title: " + e.getMessage());
            return response;
        }
    }

    public Response analyzeDescription(String dataJson) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: API Create/Update - 30-60 req/min
            if (!rateLimiterService.isAllowed("ai:description", 45, 60)) {
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            AIRequest request = objectMapper.readValue(dataJson, AIRequest.class);
            String aiDescription = handleanalyzeDescription(request.getTitle(), request.getDescription());

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("AI description generated successfully");
            response.setDescription(aiDescription);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in analyzeDescription: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error generating AI description: " + e.getMessage());
            return response;
        }
    }

    public Response analyzeContent(String dataJson) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: API Create/Update - 30-60 req/min
            if (!rateLimiterService.isAllowed("ai:content", 45, 60)) {
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            AIRequest request = objectMapper.readValue(dataJson, AIRequest.class);
            String aiContent = handleanalyzeContent(request.getContent());

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("AI content generated successfully");
            response.setContent(aiContent);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in analyzeContent: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error generating AI content: " + e.getMessage());
            return response;
        }
    }

    public String handleanalyzeTitle(String title) {
        try {
            String cacheKey = "ai:title:" + title.hashCode();

            // Check cache first
            if (redisService.hasKey(cacheKey)) {
                logger.info("Retrieving AI title from cache for: {}", title);
                return (String) redisService.get(cacheKey);
            }

            logger.debug("Getting AI title response for: {}", title);

            // RETRIEVE: Search for relevant title templates
            List<Document> relevantDocs = embeddingService.searchRelevantTemplates(
                    title, "title", "blog", "general", 5);

            // Extract content from documents
            List<String> relevantExamples = relevantDocs.stream()
                    .map(doc -> doc.getContent())
                    .collect(Collectors.toList());

            // AUGMENT: Build prompt with retrieved examples
            String prompt = promptBuilder.buildTitlePrompt(title, relevantExamples);

            // GENERATE: Use ChatClient to generate response
            String aiTitle = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            logger.info("Generated AI title: {}", aiTitle);

            // Cache result for 1 hour
            String result = aiTitle.trim();
            redisService.set(cacheKey, result, 1, TimeUnit.HOURS);

            return result;

        } catch (Exception e) {
            logger.error("Error getting AI title response: {}", e.getMessage(), e);
            throw new OurException("Failed to generate AI title", 500);
        }
    }

    public String handleanalyzeDescription(String title, String description) {
        try {
            String cacheKey = "ai:description:" + (title + description).hashCode();

            // Check cache first
            if (redisService.hasKey(cacheKey)) {
                logger.info("Retrieving AI description from cache for title: {}", title);
                return (String) redisService.get(cacheKey);
            }

            logger.debug("Getting AI description response for title: {}", title);

            // RETRIEVE: Search for relevant description templates
            List<Document> relevantDocs = embeddingService.searchRelevantTemplates(
                    title + " " + description, "description", "blog", "general", 5);

            // Extract content from documents
            List<String> relevantExamples = relevantDocs.stream()
                    .map(doc -> doc.getContent())
                    .collect(Collectors.toList());

            // AUGMENT: Build prompt with retrieved examples
            String prompt = promptBuilder.buildDescriptionPrompt(title, description, relevantExamples);

            // GENERATE: Use ChatClient to generate response
            String aiDescription = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            logger.info("Generated AI description: {}", aiDescription);

            // Cache result for 1 hour
            String result = aiDescription.trim();
            redisService.set(cacheKey, result, 1, TimeUnit.HOURS);

            return result;

        } catch (Exception e) {
            logger.error("Error getting AI description response: {}", e.getMessage(), e);
            throw new OurException("Failed to generate AI description", 500);
        }
    }

    public String handleanalyzeContent(String content) {
        try {
            String cacheKey = "ai:content:" + content.hashCode();

            // Check cache first
            if (redisService.hasKey(cacheKey)) {
                logger.info("Retrieving AI content from cache");
                return (String) redisService.get(cacheKey);
            }

            logger.debug("Getting AI content response");

            // STEP 1: Extract images to prevent base64 corruption
            ExtractionResult extracted = imagePreservationService.extractImages(content);
            logger.info("Extracted {} images from content", extracted.getImageMap().size());

            // RETRIEVE: Search for relevant content templates (using clean content)
            List<Document> relevantDocs = embeddingService.searchRelevantTemplates(
                    extracted.getCleanContent(), "content", "blog", "general", 5);

            // Extract content from documents
            List<String> relevantExamples = relevantDocs.stream()
                    .map(doc -> doc.getContent())
                    .collect(Collectors.toList());

            // AUGMENT: Build prompt with retrieved examples (using clean content)
            String prompt = promptBuilder.buildContentPrompt(extracted.getCleanContent(), relevantExamples);

            // GENERATE: Use ChatClient to generate response
            String aiContent = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            logger.info("Generated AI content, length: {}", aiContent.length());

            // STEP 2: Restore original images
            String contentWithImages = imagePreservationService.restoreImages(
                    aiContent.trim(), extracted.getImageMap());

            logger.info("Restored images to AI content");

            // Cache result for 1 hour
            redisService.set(cacheKey, contentWithImages, 1, TimeUnit.HOURS);

            return contentWithImages;

        } catch (Exception e) {
            logger.error("Error getting AI content response: {}", e.getMessage(), e);
            throw new OurException("Failed to generate AI content", 500);
        }
    }
}