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

    public CommentApi(
            CommentQueryRepository commentQueryRepository,
            CommentCommandRepository commentCommandRepository,
            BlogQueryRepository blogQueryRepository,
            CommentMapper commentMapper,
            UserFeignClient userFeignClient) {
        this.commentQueryRepository = commentQueryRepository;
        this.commentCommandRepository = commentCommandRepository;
        this.blogQueryRepository = blogQueryRepository;
        this.commentMapper = commentMapper;
        this.userFeignClient = userFeignClient;
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
        validateUser(userId);
        validateBlog(blogId);

        // Create comment
        UUID commentId = UUID.randomUUID();
        Instant now = Instant.now();

        commentCommandRepository.insertComment(
                commentId,
                blogId,
                userId,
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

        // Validate blog exists
        validateBlog(blogId);

        return commentQueryRepository.findCommentsByBlogId(blogId)
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommentDto handleGetCommentById(UUID commentId) {
        logger.debug("Fetching comment by id={}", commentId);

        Comment comment = commentQueryRepository.findCommentById(commentId)
                .orElseThrow(() -> new OurException("Comment not found", 404));

        return commentMapper.toDto(comment);
    }

    @Transactional
    public CommentDto handleUpdateComment(UUID commentId, UUID userId, UpdateCommentRequest request) {
        logger.info("Updating comment={} by user={}", commentId, userId);

        // Validate user
        validateUser(userId);

        // Validate comment exists and belongs to user
        Comment existingComment = commentQueryRepository.findCommentById(commentId)
                .orElseThrow(() -> new OurException("Comment not found", 404));

        if (!existingComment.getUserId().equals(userId)) {
            throw new OurException("Unauthorized to update this comment", 403);
        }

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
    public boolean handleDeleteComment(UUID commentId, UUID userId) {
        logger.info("Deleting comment={} by user={}", commentId, userId);

        // Validate user
        validateUser(userId);

        // Validate comment exists and belongs to user
        Comment existingComment = commentQueryRepository.findCommentById(commentId)
                .orElseThrow(() -> new OurException("Comment not found", 404));

        if (!existingComment.getUserId().equals(userId)) {
            throw new OurException("Unauthorized to delete this comment", 403);
        }

        // Delete comment
        int deleted = commentCommandRepository.deleteCommentById(commentId);
        return deleted > 0;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> handleGetAllComments() {
        logger.debug("Fetching all comments");

        return commentQueryRepository.findAllComments()
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long handleGetTotalComments() {
        return commentQueryRepository.countTotalComments();
    }

    @Transactional(readOnly = true)
    public List<CommentDto> handleGetCommentsCreatedInRange(Instant startDate, Instant endDate) {
        return commentQueryRepository.findCommentsCreatedBetween(startDate, endDate)
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long handleGetCommentsCountCreatedInRange(Instant startDate, Instant endDate) {
        return commentQueryRepository.countCommentsCreatedBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> handleGetRecentComments(int limit) {
        return commentQueryRepository.findRecentComments(PageRequest.of(0, limit))
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    // ========== Public Response Methods ==========

    public Response addComment(UUID blogId, UUID userId, AddCommentRequest request) {
        Response response = new Response();

        try {
            CommentDto comment = handleAddComment(blogId, userId, request);

            response.setStatusCode(201);
            response.setMessage("Comment added successfully");
            response.setComment(comment);
            return response;
        } catch (OurException e) {
            logger.error("Error in addComment: {}", e.getMessage(), e);
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in addComment: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to add comment");
            return response;
        }
    }

    public Response getBlogComments(UUID blogId) {
        Response response = new Response();

        try {
            List<CommentDto> comments = handleGetBlogComments(blogId);

            response.setStatusCode(200);
            response.setMessage("Blog comments retrieved successfully");
            response.setComments(comments);
            return response;
        } catch (OurException e) {
            logger.error("Error in getBlogComments: {}", e.getMessage(), e);
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in getBlogComments: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to get blog comments");
            return response;
        }
    }

    public Response getCommentById(UUID commentId) {
        Response response = new Response();

        try {
            CommentDto comment = handleGetCommentById(commentId);

            response.setStatusCode(200);
            response.setMessage("Comment retrieved successfully");
            response.setComment(comment);
            return response;
        } catch (OurException e) {
            logger.error("Error in getCommentById: {}", e.getMessage(), e);
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in getCommentById: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to get comment");
            return response;
        }
    }

    public Response updateComment(UUID commentId, UUID userId, UpdateCommentRequest request) {
        Response response = new Response();

        try {
            CommentDto comment = handleUpdateComment(commentId, userId, request);

            response.setStatusCode(200);
            response.setMessage("Comment updated successfully");
            response.setComment(comment);
            return response;
        } catch (OurException e) {
            logger.error("Error in updateComment: {}", e.getMessage(), e);
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in updateComment: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to update comment");
            return response;
        }
    }

    public Response deleteComment(UUID commentId, UUID userId) {
        Response response = new Response();

        try {
            boolean deleted = handleDeleteComment(commentId, userId);

            if (deleted) {
                response.setStatusCode(200);
                response.setMessage("Comment deleted successfully");
            } else {
                response.setStatusCode(500);
                response.setMessage("Failed to delete comment");
            }
            return response;
        } catch (OurException e) {
            logger.error("Error in deleteComment: {}", e.getMessage(), e);
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in deleteComment: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to delete comment");
            return response;
        }
    }

    public Response getAllComments() {
        Response response = new Response();

        try {
            List<CommentDto> comments = handleGetAllComments();

            response.setStatusCode(200);
            response.setMessage("All comments retrieved successfully");
            response.setComments(comments);
            return response;
        } catch (Exception e) {
            logger.error("Error in getAllComments: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to get all comments");
            return response;
        }
    }

    public Response getTotalComments() {
        Response response = new Response();

        try {
            long total = handleGetTotalComments();

            response.setStatusCode(200);
            response.setMessage("Total comments retrieved successfully");
            response.setAdditionalData(java.util.Map.of("total", total));
            return response;
        } catch (Exception e) {
            logger.error("Error in getTotalComments: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to get total comments");
            return response;
        }
    }

    public Response getCommentsCreatedInRange(String startDate, String endDate) {
        Response response = new Response();

        try {
            Instant start = Instant.parse(startDate);
            Instant end = Instant.parse(endDate);

            List<CommentDto> comments = handleGetCommentsCreatedInRange(start, end);
            long count = handleGetCommentsCountCreatedInRange(start, end);

            response.setStatusCode(200);
            response.setMessage("Comments in range retrieved successfully");
            response.setComments(comments);
            response.setAdditionalData(java.util.Map.of("count", count));
            return response;
        } catch (Exception e) {
            logger.error("Error in getCommentsCreatedInRange: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to get comments in range");
            return response;
        }
    }

    public Response getRecentComments(int limit) {
        Response response = new Response();

        try {
            List<CommentDto> comments = handleGetRecentComments(limit);

            response.setStatusCode(200);
            response.setMessage("Recent comments retrieved successfully");
            response.setComments(comments);
            return response;
        } catch (Exception e) {
            logger.error("Error in getRecentComments: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to get recent comments");
            return response;
        }
    }
}
