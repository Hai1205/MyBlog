package com.example.blogservice.services.apis.handlers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogservice.dtos.CommentDto;
import com.example.blogservice.dtos.requests.UpdateCommentRequest;
import com.example.blogservice.dtos.responses.views.UserView;
import com.example.blogservice.exceptions.OurException;
import com.example.blogservice.mappers.CommentMapper;
import com.example.blogservice.repositories.commentRepositories.CommentCommandRepository;
import com.example.blogservice.repositories.commentRepositories.CommentQueryRepository;
import com.example.blogservice.services.ValidateService;
import com.example.blogservice.services.rabbitmqs.producers.NotiProducer;
import com.example.blogservice.entities.Blog;
import com.example.blogservice.repositories.blogRepositories.BlogQueryRepository;
import com.example.rabbitcommon.dtos.NotificationMessage;
import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.utils.CacheKeyBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CommentHandler {

        private final CommentQueryRepository commentQueryRepository;
        private final CommentCommandRepository commentCommandRepository;
        private final CommentMapper commentMapper;
        private final RedisCacheService cacheService;
        private final CacheKeyBuilder cacheKeys;
        private final ValidateService validateService;
        private final NotiProducer notiProducer;
        private final BlogQueryRepository blogQueryRepository;

        public CommentHandler(
                        CommentQueryRepository commentQueryRepository,
                        CommentCommandRepository commentCommandRepository,
                        CommentMapper commentMapper,
                        ValidateService validateService,
                        RedisCacheService cacheService,
                        NotiProducer notiProducer,
                        BlogQueryRepository blogQueryRepository) {
                this.commentQueryRepository = commentQueryRepository;
                this.commentCommandRepository = commentCommandRepository;
                this.commentMapper = commentMapper;
                this.cacheService = cacheService;
                this.cacheKeys = CacheKeyBuilder.forService("blog");
                this.validateService = validateService;
                this.notiProducer = notiProducer;
                this.blogQueryRepository = blogQueryRepository;
        }

        // ========== Private Helper Methods ==========

        private CommentDto handleBuildComment(UUID commentId, UUID blogId, UUID userId, String username, String content,
                        Instant createdAt,
                        Instant updatedAt) {
                return CommentDto.builder()
                                .id(commentId)
                                .blogId(blogId)
                                .userId(userId)
                                .username(username)
                                .content(content)
                                .createdAt(createdAt)
                                .updatedAt(updatedAt)
                                .build();
        }

        // ========== Business Logic Methods ==========

        @Transactional
        public CommentDto handleAddComment(UUID blogId, UUID userId, String content) {
                try {
                        log.info("Starting handleAddComment for blogId: {}, userId: {}", blogId, userId);

                        // Validate user and blog
                        log.debug("Validating user and blog");
                        UserView user = validateService.validateUser(userId);
                        validateService.validateBlog(blogId);

                        // Create comment
                        UUID commentId = UUID.randomUUID();
                        Instant now = Instant.now();
                        log.debug("Creating comment with ID: {}", commentId);

                        commentCommandRepository.insertComment(
                                        commentId,
                                        blogId,
                                        userId,
                                        user.getUsername(),
                                        content,
                                        now,
                                        now);
                        log.debug("Comment inserted into database");

                        // Send comment notification to blog owner
                        try {
                                Blog blog = blogQueryRepository.findBlogById(blogId)
                                                .orElseThrow(() -> new OurException("Blog not found", 404));

                                // Only send notification if commenter is not the blog owner
                                if (!blog.getAuthorId().equals(userId)) {
                                        NotificationMessage notiMessage = NotificationMessage.builder()
                                                        .authorId(userId)
                                                        .receiverId(blog.getAuthorId())
                                                        .blogId(blogId)
                                                        .content("commented on your blog: " + blog.getTitle())
                                                        .type("COMMENT")
                                                        .build();

                                        notiProducer.sendCommentNotification(notiMessage);
                                        log.debug("Comment notification sent for blogId={}", blogId);
                                }
                        } catch (Exception e) {
                                log.error("Failed to send comment notification but comment was saved: {}",
                                                e.getMessage());
                        }

                        CommentDto result = handleBuildComment(
                                        commentId,
                                        blogId,
                                        userId,
                                        user.getUsername(),
                                        content,
                                        now,
                                        now);
                        log.info("Completed handleAddComment for commentId: {}", commentId);

                        return result;
                } catch (OurException e) {
                        log.warn("OurException in handleAddComment: {}", e.getMessage());
                        throw e;
                } catch (Exception e) {
                        log.error("Error in handleAddComment: {}", e.getMessage(), e);
                        throw e;
                }
        }

        @Transactional(readOnly = true)
        public List<CommentDto> handleGetBlogComments(UUID blogId) {
                try {
                        log.info("Starting handleGetBlogComments for blogId: {}", blogId);

                        String cacheKey = cacheKeys.forMethodWithId("handleGetBlogComments", blogId);
                        List<CommentDto> comments = cacheService.getCacheDataList(cacheKey, CommentDto.class);

                        if (comments == null) {
                                log.debug("Cache miss, validating blog and fetching from database");
                                validateService.validateBlog(blogId);

                                comments = commentQueryRepository.findCommentsByBlogId(blogId)
                                                .stream()
                                                .map(commentMapper::toDto)
                                                .collect(Collectors.toList());

                                cacheService.setCacheData(cacheKey, comments);
                                log.debug("Fetched {} comments from database and cached", comments.size());
                        }
                        log.info("Completed handleGetBlogComments with {} comments", comments.size());

                        return comments;
                } catch (OurException e) {
                        log.warn("OurException in handleGetBlogComments: {}", e.getMessage());
                        throw e;
                } catch (Exception e) {
                        log.error("Error in handleGetBlogComments: {}", e.getMessage(), e);
                        throw e;
                }
        }

        @Transactional
        public CommentDto handleUpdateComment(UUID commentId, UpdateCommentRequest request) {
                try {
                        log.info("Starting handleUpdateComment for commentId: {}", commentId);

                        log.debug("Validating comment");
                        CommentDto existingComment = validateService.validateComment(commentId);

                        Instant now = Instant.now();
                        log.debug("Updating comment content");
                        commentCommandRepository.updateComment(
                                        commentId,
                                        request.getContent(),
                                        now);

                        CommentDto result = handleBuildComment(
                                        commentId,
                                        existingComment.getBlogId(),
                                        existingComment.getUserId(),
                                        existingComment.getUsername(),
                                        request.getContent(),
                                        now,
                                        now);

                        log.info("Completed handleUpdateComment for commentId: {}", commentId);

                        return result;
                } catch (OurException e) {
                        log.warn("OurException in handleUpdateComment: {}", e.getMessage());
                        throw e;
                } catch (Exception e) {
                        log.error("Error in handleUpdateComment: {}", e.getMessage(), e);
                        throw e;
                }
        }

        @Transactional
        public boolean handleDeleteComment(UUID commentId) {
                try {
                        log.info("Starting handleDeleteComment for commentId: {}", commentId);

                        log.debug("Validating comment");
                        validateService.validateComment(commentId);

                        log.debug("Deleting comment from database");
                        int deleted = commentCommandRepository.deleteCommentById(commentId);
                        boolean result = deleted > 0;

                        log.info("Completed handleDeleteComment for commentId: {}, deleted: {}", commentId, result);

                        return result;
                } catch (OurException e) {
                        log.warn("OurException in handleDeleteComment: {}", e.getMessage());
                        throw e;
                } catch (Exception e) {
                        log.error("Error in handleDeleteComment: {}", e.getMessage(), e);
                        throw e;
                }
        }

        @Transactional(readOnly = true)
        public List<CommentDto> handleGetAllComments() {
                try {
                        log.info("Starting handleGetAllComments");

                        String cacheKey = cacheKeys.forMethod("handleGetAllComments");
                        List<CommentDto> comments = cacheService.getCacheDataList(cacheKey, CommentDto.class);

                        if (comments == null) {
                                log.debug("Cache miss, fetching all comments from database");
                                comments = commentQueryRepository.findAllComments()
                                                .stream()
                                                .map(commentMapper::toDto)
                                                .collect(Collectors.toList());

                                cacheService.setCacheData(cacheKey, comments);
                                log.debug("Fetched {} comments from database and cached", comments.size());
                        }

                        return comments;
                } catch (OurException e) {
                        log.warn("OurException in handleGetAllComments: {}", e.getMessage());
                        throw e;
                } catch (Exception e) {
                        log.error("Error in handleGetAllComments: {}", e.getMessage(), e);
                        throw e;
                }
        }
}
