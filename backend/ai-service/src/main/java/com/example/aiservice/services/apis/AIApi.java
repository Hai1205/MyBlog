package com.example.aiservice.services.apis;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import org.springframework.cache.annotation.Cacheable;

import com.example.aiservice.dtos.requests.*;
import com.example.aiservice.dtos.responses.Response;
import com.example.aiservice.exceptions.OurException;
import com.example.aiservice.services.*;
import com.fasterxml.jackson.databind.*;

import java.util.*;
import java.util.stream.Collectors;
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

    // Supporting Services
    private final ObjectMapper objectMapper;

    /**
     * Constructor - Inject all dependencies
     */
    public AIApi(
            ChatClient chatClient, // Inject từ RAGConfig
            EmbeddingService embeddingService,
            CompactPromptBuilder promptBuilder) {

        this.chatClient = chatClient;
        this.embeddingService = embeddingService;
        this.promptBuilder = promptBuilder;
        this.objectMapper = new ObjectMapper();
    }

    // ========================================
    // PUBLIC API METHODS
    // ========================================

    public Response getAITitleResponse(String dataJson) {
        Response response = new Response();

        try {
            AIRequest request = objectMapper.readValue(dataJson, AIRequest.class);
            String aiTitle = handleGetAITitleResponse(request.getTitle());

            response.setStatusCode(200);
            response.setMessage("AI title generated successfully");
            response.setTitle(aiTitle);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in getAITitleResponse: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error generating AI title: " + e.getMessage());
            return response;
        }
    }

    public Response getAIDescriptionResponse(String dataJson) {
        Response response = new Response();

        try {
            AIRequest request = objectMapper.readValue(dataJson, AIRequest.class);
            String aiDescription = handleGetAIDescriptionResponse(request.getTitle(), request.getDescription());

            response.setStatusCode(200);
            response.setMessage("AI description generated successfully");
            response.setDescription(aiDescription);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in getAIDescriptionResponse: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error generating AI description: " + e.getMessage());
            return response;
        }
    }

    public Response getAIContentResponse(String dataJson) {
        Response response = new Response();

        try {
            AIRequest request = objectMapper.readValue(dataJson, AIRequest.class);
            String aiContent = handleGetAIContentResponse(request.getContent());

            response.setStatusCode(200);
            response.setMessage("AI content generated successfully");
            response.setContent(aiContent);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in getAIContentResponse: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error generating AI content: " + e.getMessage());
            return response;
        }
    }

        public String handleGetAITitleResponse(String title) {
        try {
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
            return aiTitle.trim();

        } catch (Exception e) {
            logger.error("Error getting AI title response: {}", e.getMessage(), e);
            throw new OurException("Failed to generate AI title", 500);
        }
    }

    public String handleGetAIDescriptionResponse(String title, String description) {
        try {
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
            return aiDescription.trim();

        } catch (Exception e) {
            logger.error("Error getting AI description response: {}", e.getMessage(), e);
            throw new OurException("Failed to generate AI description", 500);
        }
    }

    public String handleGetAIContentResponse(String content) {
        try {
            logger.debug("Getting AI content response");

            // RETRIEVE: Search for relevant content templates
            List<Document> relevantDocs = embeddingService.searchRelevantTemplates(
                content, "content", "blog", "general", 5);

            // Extract content from documents
            List<String> relevantExamples = relevantDocs.stream()
                .map(doc -> doc.getContent())
                .collect(Collectors.toList());

            // AUGMENT: Build prompt with retrieved examples
            String prompt = promptBuilder.buildContentPrompt(content, relevantExamples);

            // GENERATE: Use ChatClient to generate response
            String aiContent = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

            logger.info("Generated AI content, length: {}", aiContent.length());
            return aiContent.trim();

        } catch (Exception e) {
            logger.error("Error getting AI content response: {}", e.getMessage(), e);
            throw new OurException("Failed to generate AI content", 500);
        }
    }
}