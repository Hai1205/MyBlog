package com.example.aiservice.services.apis;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.example.aiservice.dtos.requests.*;
import com.example.aiservice.dtos.responses.Response;
import com.example.aiservice.exceptions.OurException;
import com.example.aiservice.services.apis.handlers.AIHandler;
import com.example.rediscommon.services.RateLimiterService;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.fasterxml.jackson.databind.*;

@Slf4j
@Service
public class AIApi {

    private final ObjectMapper objectMapper;
    private final RateLimiterService rateLimiterService;
    private final AIHandler aiHandler;
    private final CacheKeyBuilder cacheKeys;

    public AIApi(
            RateLimiterService rateLimiterService,
            AIHandler aiHandler) {

        this.rateLimiterService = rateLimiterService;
        this.aiHandler = aiHandler;
        this.objectMapper = new ObjectMapper();
        this.cacheKeys = CacheKeyBuilder.forService("ai");
    }

    private long requestStart(String message) {
        log.info(message);
        return System.currentTimeMillis();
    }

    private void requestEnd(long startTime) {
        long endTime = System.currentTimeMillis();
        log.info("Completed request in {} ms", endTime - startTime);
    }

    private void checkRateLimit(String rateLimitKey, int requests, int windowSeconds) {
        if (!rateLimiterService.isAllowed(rateLimitKey, requests, windowSeconds)) {
            throw new OurException("Rate limit exceeded. Please try again later.", 429);
        }
    }

    public Response analyzeTitle(String dataJson) {
        long startTime = requestStart("Analyze title attempt");

        try {
            AIRequest request = objectMapper.readValue(dataJson, AIRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithParam("analyzeTitle", request.getTitle());
            checkRateLimit(rateLimitKey, 45, 60);

            String aiTitle = aiHandler.handleAnalyzeTitle(request.getTitle());

            Response response = new Response("AI title generated successfully");
            response.setTitle(aiTitle);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response analyzeDescription(String dataJson) {
        long startTime = requestStart("Analyze description attempt");

        try {
            AIRequest request = objectMapper.readValue(dataJson, AIRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithParams("analyzeDescription", request.getTitle(), request.getDescription());
            checkRateLimit(rateLimitKey, 45, 60);

            String aiDescription = aiHandler.handleAnalyzeDescription(request.getTitle(), request.getDescription());

            Response response = new Response("AI description generated successfully");
            response.setDescription(aiDescription);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response analyzeContent(String dataJson) {
        long startTime = requestStart("Analyze content attempt");

        try {
            AIRequest request = objectMapper.readValue(dataJson, AIRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithParam("analyzeContent", request.getContent().hashCode());
            checkRateLimit(rateLimitKey, 45, 60);

            String aiContent = aiHandler.handleAnalyzeContent(request.getContent());

            Response response = new Response("AI content generated successfully");
            response.setContent(aiContent);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

}