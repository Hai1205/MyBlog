package com.example.blogservice.services.apis;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.blogservice.dtos.*;
import com.example.blogservice.dtos.requests.*;
import com.example.blogservice.dtos.responses.*;
import com.example.blogservice.dtos.responses.views.BlogView;
import com.example.blogservice.exceptions.OurException;
import com.example.blogservice.mappers.*;
import com.example.blogservice.services.apis.handlers.BlogHandler;
import com.example.rediscommon.services.RateLimiterService;
import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BlogApi {

    private final ObjectMapper objectMapper;
    private final RateLimiterService rateLimiterService;
    private final BlogHandler blogHandler;
    private final BlogMapper blogMapper;
    private final CacheKeyBuilder cacheKeys;

    public BlogApi(
            BlogMapper blogMapper,
            RateLimiterService rateLimiterService,
            RedisCacheService cacheService,
            CommentApi commentApi,
            BlogHandler blogHandler) {
        this.blogMapper = blogMapper;
        this.rateLimiterService = rateLimiterService;
        this.blogHandler = blogHandler;
        this.objectMapper = new ObjectMapper();
        this.cacheKeys = CacheKeyBuilder.forService("blog");
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

    public Response createBlog(UUID userId, String dataJson, MultipartFile thumbnail) {
        long startTime = requestStart("Create blog attempt for user: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithId("createBlog", userId);
            checkRateLimit(rateLimitKey, 45, 60);

            CreateBlogRequest request = objectMapper.readValue(dataJson, CreateBlogRequest.class);
            BlogDto blog = blogHandler.handleCreateBlog(
                    userId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getCategory(),
                    request.getContent(),
                    request.getIsVisibility(),
                    thumbnail);

            log.info("Blog created successfully: blogId={}", blog.getId());

            Response response = new Response("Blog created successfully", 201);
            response.setBlog(blog);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response duplicateBlog(UUID blogId, UUID userId) {
        long startTime = requestStart("Duplicate blog attempt for blog: " + blogId + " user: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParams("duplicateBlog", blogId, userId);
            checkRateLimit(rateLimitKey, 45, 60);

            BlogDto blog = blogHandler.handleDuplicateBlog(blogId, userId);

            log.info("Blog duplicated successfully: blogId={}", blogId);

            Response response = new Response("Blog duplicated successfully", 201);
            response.setBlog(blog);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getAllBlogs(Boolean isVisibility, Boolean isView) {
        long startTime = requestStart("Get all blogs attempt with visibility: " + isVisibility);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("getAllBlogs",
                    isVisibility == null ? "all" : isVisibility.toString());
            checkRateLimit(rateLimitKey, 45, 60);

            List<BlogDto> blogs = blogHandler.handleGetAllBlogs(isVisibility);

            log.info("All blogs retrieved: count={}", blogs.size());

            Response response = new Response("Blogs retrieved successfully");
            if (isView != null && isView) {
                List<BlogView> blogViews = blogs.stream()
                        .map(blogMapper::toView)
                        .collect(Collectors.toList());
                response.setBlogViews(blogViews);
            } else {
                response.setBlogs(blogs);
            }
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getBlog(UUID blogId, UUID userId) {
        long startTime = requestStart("Get blog attempt for blog: " + blogId + " user: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParams("getBlog", blogId, userId);
            checkRateLimit(rateLimitKey, 45, 60);

            BlogDto blog = blogHandler.handleGetBlog(blogId, userId);

            log.info("Blog retrieved successfully: blogId={}", blogId);

            Response response = new Response("Blog retrieved successfully");
            response.setBlog(blog);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getUserBlogs(UUID userId) {
        long startTime = requestStart("Get user blogs attempt for user: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithId("getUserBlogs", userId);
            checkRateLimit(rateLimitKey, 45, 60);

            List<BlogDto> blogs = blogHandler.handleGetUserBlogs(userId);

            log.info("User blogs retrieved: count={}", blogs.size());

            Response response = new Response("User blogs retrieved successfully");
            response.setBlogs(blogs);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response updateBlog(UUID blogId, String dataJson, MultipartFile thumbnail) {
        long startTime = requestStart("Update blog attempt for blog: " + blogId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithId("updateBlog", blogId);
            checkRateLimit(rateLimitKey, 45, 60);

            UpdateBlogRequest request = objectMapper.readValue(dataJson, UpdateBlogRequest.class);
            BlogDto blog = blogHandler.handleUpdateBlog(
                    blogId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getCategory(),
                    request.getContent(),
                    request.getIsVisibility(),
                    thumbnail);

            log.info("Blog updated successfully: blogId={}", blogId);

            Response response = new Response("Blog updated successfully");
            response.setBlog(blog);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response deleteBlog(UUID blogId) {
        long startTime = requestStart("Delete blog attempt for blog: " + blogId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithId("deleteBlog", blogId);
            checkRateLimit(rateLimitKey, 45, 60);

            blogHandler.handleDeleteBlog(blogId);

            log.info("Blog deleted successfully: blogId={}", blogId);

            return new Response("Blog deleted successfully");
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response saveBlog(UUID userId, UUID blogId) {
        long startTime = requestStart("Save blog attempt for blog: " + blogId + " user: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParams("saveBlog", blogId, userId);
            checkRateLimit(rateLimitKey, 45, 60);

            BlogDto blog = blogHandler.handleSaveBlog(blogId, userId);

            log.info("Blog saved successfully: blogId={} userId={}", blogId, userId);

            Response response = new Response("Blog saved successfully");
            response.setBlog(blog);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response unsaveBlog(UUID userId, UUID blogId) {
        long startTime = requestStart("Unsave blog attempt for blog: " + blogId + " user: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParams("unsaveBlog", blogId, userId);
            checkRateLimit(rateLimitKey, 45, 60);

            blogHandler.handleUnsaveBlog(blogId, userId);

            log.info("Blog unsaved successfully: blogId={} userId={}", blogId, userId);

            return new Response("Blog unsaved successfully");
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getUserSavedBlogs(UUID userId) {
        long startTime = requestStart("Get user saved blogs attempt for user: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithId("getUserSavedBlogs", userId);
            checkRateLimit(rateLimitKey, 45, 60);

            List<BlogDto> blogs = blogHandler.handleGetUserSavedBlogs(userId);

            log.info("User saved blogs retrieved: count={}", blogs.size());

            Response response = new Response("Saved blogs retrieved successfully");
            response.setBlogs(blogs);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Error getting saved blogs: {}", e.getMessage(), e);
            return new Response("Failed to get saved blogs", 500);
        } finally {
            requestEnd(startTime);
        }
    }
}