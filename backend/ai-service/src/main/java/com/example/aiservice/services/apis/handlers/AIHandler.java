package com.example.aiservice.services.apis.handlers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;

import com.example.aiservice.dtos.ExtractionResult;
import com.example.aiservice.exceptions.OurException;
import com.example.aiservice.services.*;
import com.example.aiservice.utils.MarkdownCleaner;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AIHandler {

    // Core RAG Components
    private final ChatClient chatClient;
    // Vector Search
    private final EmbeddingService embeddingService;
    // Engineering
    private final CompactPromptBuilderService promptBuilderService;
    // Image Preservation
    private final ImagePreservationService imagePreservationService;

    public AIHandler(
            ChatClient chatClient,
            EmbeddingService embeddingService,
            CompactPromptBuilderService promptBuilderService,
            ImagePreservationService imagePreservationService) {

        this.chatClient = chatClient;
        this.embeddingService = embeddingService;
        this.promptBuilderService = promptBuilderService;
        this.imagePreservationService = imagePreservationService;
    }

    public String handleAnalyzeTitle(String title) {
        try {
            log.info("Starting handleAnalyzeTitle for title={}", title);

            // RETRIEVE: Search for relevant title templates
            log.debug("Searching for relevant title templates");
            List<Document> relevantDocs = embeddingService.searchRelevantTemplates(
                    title, "title", "blog", "general", 5);
            log.debug("Found {} relevant title templates", relevantDocs.size());

            // Extract content from documents
            List<String> relevantExamples = relevantDocs.stream()
                    .map(doc -> doc.getText())
                    .collect(Collectors.toList());

            // AUGMENT: Build prompt with retrieved examples
            log.debug("Building title prompt with {} examples", relevantExamples.size());
            String prompt = promptBuilderService.buildTitlePrompt(title, relevantExamples);

            // GENERATE: Use ChatClient to generate response
            log.debug("Generating AI title using ChatClient");
            String aiTitle = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("Generated AI title: {}", aiTitle);

            // Cache result for 1 hour
            String result = aiTitle.trim();

            return result;

        } catch (Exception e) {
            log.error("Error getting AI title response: {}", e.getMessage(), e);
            throw new OurException("Failed to generate AI title", 500);
        }
    }

    public String handleAnalyzeDescription(String title, String description) {
        try {
            log.info("Starting handleAnalyzeDescription for title={}, description length={}", title,
                    description.length());

            // RETRIEVE: Search for relevant description templates
            log.debug("Searching for relevant description templates");
            List<Document> relevantDocs = embeddingService.searchRelevantTemplates(
                    title + " " + description, "description", "blog", "general", 5);
            log.debug("Found {} relevant description templates", relevantDocs.size());

            // Extract content from documents
            List<String> relevantExamples = relevantDocs.stream()
                    .map(doc -> doc.getText())
                    .collect(Collectors.toList());

            // AUGMENT: Build prompt with retrieved examples
            log.debug("Building description prompt with {} examples", relevantExamples.size());
            String prompt = promptBuilderService.buildDescriptionPrompt(title, description, relevantExamples);

            // GENERATE: Use ChatClient to generate response
            log.debug("Generating AI description using ChatClient");
            String aiDescription = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("Generated AI description: {}", aiDescription);

            // Cache result for 1 hour
            String result = aiDescription.trim();

            return result;

        } catch (Exception e) {
            log.error("Error getting AI description response: {}", e.getMessage(), e);
            throw new OurException("Failed to generate AI description", 500);
        }
    }

    public String handleAnalyzeContent(String content) {
        try {
            log.info("Starting handleAnalyzeContent for content length={}", content.length());

            // STEP 1: Extract images to prevent base64 corruption
            log.debug("Extracting images from content");
            ExtractionResult extracted = imagePreservationService.extractImages(content);
            log.info("Extracted {} images from content", extracted.getImageMap().size());

            // RETRIEVE: Search for relevant content templates (using clean content)
            log.debug("Searching for relevant content templates");
            List<Document> relevantDocs = embeddingService.searchRelevantTemplates(
                    extracted.getCleanContent(), "content", "blog", "general", 5);
            log.debug("Found {} relevant content templates", relevantDocs.size());

            // Extract content from documents
            List<String> relevantExamples = relevantDocs.stream()
                    .map(doc -> doc.getText())
                    .collect(Collectors.toList());

            // AUGMENT: Build prompt with retrieved examples (using clean content)
            log.debug("Building content prompt with {} examples", relevantExamples.size());
            String prompt = promptBuilderService.buildContentPrompt(extracted.getCleanContent(), relevantExamples);

            // GENERATE: Use ChatClient to generate response
            log.debug("Generating AI content using ChatClient");
            String aiContent = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("Generated AI content, length: {}", aiContent.length());

            // Clean markdown formatting from AI response
            log.debug("Cleaning markdown formatting from AI response");
            String cleanedContent = MarkdownCleaner.clean(aiContent.trim());

            // STEP 2: Restore original images
            log.debug("Restoring images to AI content");
            String contentWithImages = imagePreservationService.restoreImages(
                    cleanedContent, extracted.getImageMap());

            log.info("Restored images to AI content");

            return contentWithImages;

        } catch (Exception e) {
            log.error("Error getting AI content response: {}", e.getMessage(), e);
            throw new OurException("Failed to generate AI content", 500);
        }
    }
}
