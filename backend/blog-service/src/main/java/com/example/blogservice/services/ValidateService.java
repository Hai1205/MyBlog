package com.example.blogservice.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogservice.dtos.BlogDto;
import com.example.blogservice.dtos.CommentDto;
import com.example.blogservice.dtos.responses.Response;
import com.example.blogservice.dtos.responses.views.UserView;
import com.example.blogservice.exceptions.OurException;
import com.example.blogservice.mappers.BlogMapper;
import com.example.blogservice.mappers.CommentMapper;
import com.example.blogservice.repositories.blogRepositories.BlogQueryRepository;
import com.example.blogservice.repositories.commentRepositories.CommentQueryRepository;
import com.example.blogservice.services.feigns.UserFeignClient;
import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.utils.CacheKeyBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidateService {

    private final UserFeignClient userFeignClient;
    private final RedisCacheService cacheService;
    private final CacheKeyBuilder cacheKeys;
    private final BlogQueryRepository blogQueryRepository;
    private final CommentQueryRepository commentQueryRepository;
    private final BlogMapper blogMapper;
    private final CommentMapper commentMapper;

    public ValidateService(RedisCacheService cacheService,
            UserFeignClient userFeignClient,
            BlogQueryRepository blogQueryRepository,
            CommentQueryRepository commentQueryRepository,
            BlogMapper blogMapper,
            CommentMapper commentMapper) {
        this.userFeignClient = userFeignClient;
        this.cacheService = cacheService;
        this.cacheKeys = CacheKeyBuilder.forService("blog_comment_validate");
        this.blogQueryRepository = blogQueryRepository;
        this.commentQueryRepository = commentQueryRepository;
        this.blogMapper = blogMapper;
        this.commentMapper = commentMapper;
    }

    public UserView validateUser(UUID userId) {
        try {
            String cacheKey = cacheKeys.forMethodWithId("validateUser", userId);
            UserView user = cacheService.getCacheData(cacheKey, UserView.class);

            if (user == null) {
                Response response = userFeignClient.getUserById(userId);
                if (response.getStatusCode() != 200) {
                    throw new OurException("User not found", 404);
                }

                user = response.getUserView();
                cacheService.setCacheData(cacheKey, user);
            }

            return user;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public BlogDto validateBlog(UUID blogId) {
        try {
            String cacheKey = cacheKeys.forMethodWithId("validateBlog", blogId);
            BlogDto blog = cacheService.getCacheData(cacheKey, BlogDto.class);

            if (blog == null) {
                blog = blogQueryRepository.findBlogById(blogId).map(blogMapper::toDto)
                        .orElseThrow(() -> new OurException("Blog not found", 404));

                cacheService.setCacheData(cacheKey, blog);
            }

            return blog;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public CommentDto validateComment(UUID commentId) {
        try {
            String cacheKey = cacheKeys.forMethodWithId("validateComment", commentId);
            CommentDto comment = cacheService.getCacheData(cacheKey, CommentDto.class);

            if (comment == null) {
                comment = commentQueryRepository.findCommentById(commentId).map(commentMapper::toDto)
                        .orElseThrow(() -> new OurException("Comment not found", 404));

                cacheService.setCacheData(cacheKey, comment);
            }

            return comment;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}
