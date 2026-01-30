package com.example.blogservice.services.apis;

import org.springframework.stereotype.Service;

import com.example.blogservice.dtos.*;
import com.example.blogservice.dtos.requests.*;
import com.example.blogservice.dtos.responses.Response;
import com.example.blogservice.exceptions.OurException;
import com.example.blogservice.services.ValidateService;
import com.example.blogservice.services.apis.handlers.CommentHandler;
import com.example.rediscommon.services.RateLimiterService;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CommentApi {

    private final ObjectMapper objectMapper;
    private final RateLimiterService rateLimiterService;
    private final CommentHandler commentHandler;
    private final ValidateService validateService;
    private final CacheKeyBuilder cacheKeys;

    public CommentApi(
            RateLimiterService rateLimiterService,
            CommentHandler commentHandler,
            ValidateService validateService) {
        this.rateLimiterService = rateLimiterService;
        this.commentHandler = commentHandler;
        this.validateService = validateService;
        this.objectMapper = new ObjectMapper();
        this.cacheKeys = CacheKeyBuilder.forService("comment");
    }

    private long requestStart(String message) {
        log.info(message);
        return System.currentTimeMillis();
    }

    private void requestEnd(long startTime) {
        long endTime = System.currentTimeMillis();
        log.info("Completed request in {} ms", endTime - startTime);
    }

    private void checkRateLimit(String rateLimitKey, int maxRequests, int timeWindowSeconds) {
        if (!rateLimiterService.isAllowed(rateLimitKey, maxRequests, timeWindowSeconds)) {
            throw new OurException("Rate limit exceeded. Please try again later.", 429);
        }
    }

    public Response addComment(UUID blogId, UUID userId, String dataJson) {
        long startTime = requestStart("Add comment attempt for blog: " + blogId + " user: " + userId);

        try {
            AddCommentRequest request = objectMapper.readValue(dataJson, AddCommentRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithParams("addComment", blogId, userId);
            checkRateLimit(rateLimitKey, 45, 60);

            CommentDto comment = commentHandler.handleAddComment(blogId, userId, request.getContent());

            Response response = new Response("Comment added successfully", 201);
            response.setComment(comment);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getBlogComments(UUID blogId) {
        long startTime = requestStart("Get blog comments attempt for blog: " + blogId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithId("getBlogComments", blogId);
            checkRateLimit(rateLimitKey, 45, 60);

            List<CommentDto> comments = commentHandler.handleGetBlogComments(blogId);

            Response response = new Response("Blog comments retrieved successfully");
            response.setComments(comments);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getCommentById(UUID commentId) {
        long startTime = requestStart("Get comment by id attempt: " + commentId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithId("getCommentById", commentId);
            checkRateLimit(rateLimitKey, 45, 60);

            CommentDto comment = validateService.validateComment(commentId);

            Response response = new Response("Comment retrieved successfully");
            response.setComment(comment);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response updateComment(UUID commentId, String dataJson) {
        long startTime = requestStart("Update comment attempt: " + commentId);

        try {
            UpdateCommentRequest request = objectMapper.readValue(dataJson, UpdateCommentRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithId("updateComment", commentId);
            checkRateLimit(rateLimitKey, 45, 60);

            CommentDto comment = commentHandler.handleUpdateComment(commentId, request);

            Response response = new Response("Comment updated successfully");
            response.setComment(comment);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response deleteComment(UUID commentId) {
        long startTime = requestStart("Delete comment attempt: " + commentId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithId("deleteComment", commentId);
            checkRateLimit(rateLimitKey, 45, 60);

            commentHandler.handleDeleteComment(commentId);

            return new Response("Comment deleted successfully");
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getAllComments() {
        long startTime = requestStart("Get all comments attempt");

        try {
            String rateLimitKey = cacheKeys.forMethod("getAllComments");
            checkRateLimit(rateLimitKey, 45, 60);

            List<CommentDto> comments = commentHandler.handleGetAllComments();

            Response response = new Response("All comments retrieved successfully");
            response.setComments(comments);
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
