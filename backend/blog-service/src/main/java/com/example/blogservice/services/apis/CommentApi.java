package com.example.blogservice.services.apis;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogservice.dtos.*;
import com.example.blogservice.dtos.requests.*;
import com.example.blogservice.dtos.responses.Response;
import com.example.blogservice.entities.*;
import com.example.blogservice.exceptions.OurException;
import com.example.blogservice.mappers.*;
import com.example.blogservice.repositories.commentRepositories.*;
import com.example.blogservice.repositories.blogRepositories.*;
import com.example.blogservice.services.feigns.UserFeignClient;
import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.services.ApiResponseHandler;
import com.example.rediscommon.utils.CacheKeyBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentApi extends BaseApi {
    private final CommentQueryRepository commentQueryRepository;
    private final CommentCommandRepository commentCommandRepository;
    private final BlogQueryRepository blogQueryRepository;
    private final CommentMapper commentMapper;
    private final UserFeignClient userFeignClient;
    private final RedisCacheService cacheService;
    private final ApiResponseHandler<Response> responseHandler;
    private final CacheKeyBuilder cacheKeys;

    public CommentApi(
            CommentQueryRepository commentQueryRepository,
            CommentCommandRepository commentCommandRepository,
            BlogQueryRepository blogQueryRepository,
            CommentMapper commentMapper,
            UserFeignClient userFeignClient,
            RedisCacheService cacheService,
            ApiResponseHandler<Response> responseHandler) {
        this.commentQueryRepository = commentQueryRepository;
        this.commentCommandRepository = commentCommandRepository;
        this.blogQueryRepository = blogQueryRepository;
        this.commentMapper = commentMapper;
        this.userFeignClient = userFeignClient;
        this.cacheService = cacheService;
        this.responseHandler = responseHandler;
        this.cacheKeys = CacheKeyBuilder.forService("comment");
    }

    // ========== Private Helper Methods ==========

    private UserDto validateUser(UUID userId) {
        try {
            UserDto user = userFeignClient.getUserById(userId).getUser();
            if (user == null) {
                logger.warn("User not found: userId={}", userId);
                throw new OurException("User not found", 404);
            }
            return user;
        } catch (Exception e) {
            logger.error("Error validating user: {}", e.getMessage(), e);
            throw new OurException("User validation failed", 500);
        }
    }

    private Blog validateBlog(UUID blogId) {
        return blogQueryRepository.findBlogById(blogId)
                .orElseThrow(() -> new OurException("Blog not found", 404));
    }

    // ========== Business Logic Methods ==========

    @Transactional
    public CommentDto handleAddComment(UUID blogId, UUID userId, AddCommentRequest request) {
        logger.info("Adding comment to blog={} by user={}", blogId, userId);

        // Validate user and blog
        UserDto user = validateUser(userId);
        validateBlog(blogId);

        // Create comment
        UUID commentId = UUID.randomUUID();
        Instant now = Instant.now();

        commentCommandRepository.insertComment(
                commentId,
                blogId,
                userId,
                user.getUsername(),
                request.getContent(),
                now,
                now);

        // Fetch and return created comment
        Comment savedComment = commentQueryRepository.findCommentById(commentId)
                .orElseThrow(() -> new OurException("Failed to create comment", 500));

        return commentMapper.toDto(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> handleGetBlogComments(UUID blogId) {
        logger.debug("Fetching comments for blog={}", blogId);
        validateBlog(blogId);

        return cacheService.executeWithCacheList(
                cacheKeys.forMethodWithId("handleGetBlogComments", blogId),
                CommentDto.class,
                () -> commentQueryRepository.findCommentsByBlogId(blogId)
                        .stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public CommentDto handleGetCommentById(UUID commentId) {
        logger.debug("Fetching comment by id={}", commentId);

        return cacheService.executeWithCache(
                cacheKeys.forMethodWithId("handleGetCommentById", commentId),
                CommentDto.class,
                () -> {
                    Comment comment = commentQueryRepository.findCommentById(commentId)
                            .orElseThrow(() -> new OurException("Comment not found", 404));
                    return commentMapper.toDto(comment);
                });
    }

    @Transactional
    public CommentDto handleUpdateComment(UUID commentId, UpdateCommentRequest request) {
        logger.info("Updating comment={}", commentId);

        // Validate comment exists and belongs to user
        commentQueryRepository.findCommentById(commentId)
                .orElseThrow(() -> new OurException("Comment not found", 404));

        // Update comment
        Instant now = Instant.now();
        commentCommandRepository.updateComment(
                commentId,
                request.getContent(),
                now);

        // Fetch and return updated comment
        Comment updatedComment = commentQueryRepository.findCommentById(commentId)
                .orElseThrow(() -> new OurException("Failed to update comment", 500));

        return commentMapper.toDto(updatedComment);
    }

    @Transactional
    public boolean handleDeleteComment(UUID commentId) {
        logger.info("Deleting comment={}", commentId);

        // Validate comment exists and belongs to user
        commentQueryRepository.findCommentById(commentId)
                .orElseThrow(() -> new OurException("Comment not found", 404));

        // Delete comment
        int deleted = commentCommandRepository.deleteCommentById(commentId);
        return deleted > 0;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> handleGetAllComments() {
        logger.debug("Fetching all comments");

        return cacheService.executeWithCacheList(
                cacheKeys.forMethod("handleGetAllComments"),
                CommentDto.class,
                () -> commentQueryRepository.findAllComments()
                        .stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public long handleGetTotalComments() {
        return cacheService.executeWithCachePrimitive(
                cacheKeys.forMethod("handleGetTotalComments"),
                () -> commentQueryRepository.countTotalComments());
    }

    @Transactional(readOnly = true)
    public List<CommentDto> handleGetCommentsCreatedInRange(Instant startDate, Instant endDate) {
        return cacheService.executeWithCacheList(
                cacheKeys.forMethodWithParams("handleGetCommentsCreatedInRange", startDate, endDate),
                CommentDto.class,
                () -> commentQueryRepository.findCommentsCreatedBetween(startDate, endDate)
                        .stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public long handleGetCommentsCountCreatedInRange(Instant startDate, Instant endDate) {
        return cacheService.executeWithCachePrimitive(
                cacheKeys.forMethodWithParams("handleGetCommentsCountCreatedInRange", startDate, endDate),
                () -> commentQueryRepository.countCommentsCreatedBetween(startDate, endDate));
    }

    @Transactional(readOnly = true)
    public List<CommentDto> handleGetRecentComments(int limit) {
        return cacheService.executeWithCacheList(
                cacheKeys.forMethodWithParam("handleGetRecentComments", limit),
                CommentDto.class,
                () -> commentQueryRepository.findRecentComments(PageRequest.of(0, limit))
                        .stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList()));
    }

    // ========== Public Response Methods ==========

    public Response addComment(UUID blogId, UUID userId, AddCommentRequest request) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParams("addComment", userId),
                45,
                () -> handleAddComment(blogId, userId, request),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setComment,
                "Comment added successfully",
                201);
    }

    public Response getBlogComments(UUID blogId) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("getBlogComments", blogId),
                90,
                () -> handleGetBlogComments(blogId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setComments,
                "Blog comments retrieved successfully",
                200);
    }

    public Response getCommentById(UUID commentId) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("getCommentById", commentId),
                90,
                () -> handleGetCommentById(commentId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setComment,
                "Comment retrieved successfully",
                200);
    }

    public Response updateComment(UUID commentId, UpdateCommentRequest request) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("updateComment", commentId),
                45,
                () -> handleUpdateComment(commentId, request),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setComment,
                "Comment updated successfully",
                200);
    }

    public Response deleteComment(UUID commentId) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("deleteComment", commentId),
                20,
                () -> handleDeleteComment(commentId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                null,
                "Comment deleted successfully",
                200);
    }

    public Response getAllComments() {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethod("getAllComments"),
                90,
                this::handleGetAllComments,
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setComments,
                "All comments retrieved successfully",
                200);
    }

    public Response getTotalComments() {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethod("getTotalComments"),
                90,
                () -> {
                    long total = handleGetTotalComments();
                    return java.util.Map.of("total", total);
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setAdditionalData,
                "Total comments retrieved successfully",
                200);
    }

    public Response getCommentsCreatedInRange(String startDate, String endDate) {
        Response response = responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParams("getCommentsCreatedInRange", startDate, endDate),
                90,
                () -> {
                    Instant start = Instant.parse(startDate);
                    Instant end = Instant.parse(endDate);
                    return handleGetCommentsCreatedInRange(start, end);
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setComments,
                "Comments in range retrieved successfully",
                200);

        // Add count separately for complex response
        try {
            Instant start = Instant.parse(startDate);
            Instant end = Instant.parse(endDate);
            long count = handleGetCommentsCountCreatedInRange(start, end);
            response.setAdditionalData(java.util.Map.of("count", count));
        } catch (Exception e) {
            logger.error("Error getting count: {}", e.getMessage());
        }

        return response;
    }

    public Response getRecentComments(int limit) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("getRecentComments", limit),
                90,
                () -> handleGetRecentComments(limit),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setComments,
                "Recent comments retrieved successfully",
                200);
    }
}
