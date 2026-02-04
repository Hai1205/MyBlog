package com.example.blogservice.services.apis.handlers;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.blogservice.dtos.BlogDto;
import com.example.blogservice.dtos.CommentDto;
import com.example.blogservice.entities.Blog;
import com.example.blogservice.entities.LikedBlog;
import com.example.blogservice.entities.SavedBlog;
import com.example.blogservice.exceptions.OurException;
import com.example.blogservice.mappers.BlogMapper;
import com.example.blogservice.repositories.blogRepositories.BlogCommandRepository;
import com.example.blogservice.repositories.blogRepositories.BlogQueryRepository;
import com.example.blogservice.repositories.likedBlogRepositories.LikedBlogCommandRepository;
import com.example.blogservice.repositories.likedBlogRepositories.LikedBlogQueryRepository;
import com.example.blogservice.repositories.savedBlogRepositories.SavedBlogCommandRepository;
import com.example.blogservice.repositories.savedBlogRepositories.SavedBlogQueryRepository;
import com.example.blogservice.services.ValidateService;
import com.example.blogservice.services.rabbitmqs.producers.NotiProducer;
import com.example.cloudinarycommon.CloudinaryService;
import com.example.rabbitcommon.dtos.NotificationMessage;
import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.utils.CacheKeyBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BlogHandler {

    private final BlogQueryRepository blogQueryRepository;
    private final BlogCommandRepository blogCommandRepository;
    private final SavedBlogCommandRepository savedBlogCommandRepository;
    private final SavedBlogQueryRepository savedBlogQueryRepository;
    private final LikedBlogCommandRepository likedBlogCommandRepository;
    private final LikedBlogQueryRepository likedBlogQueryRepository;
    private final CloudinaryService cloudinaryService;
    private final RedisCacheService cacheService;
    private final CacheKeyBuilder cacheKeys;
    private final ValidateService validateService;
    private final BlogMapper blogMapper;
    private final CommentHandler commentHandler;
    private final NotiProducer notiProducer;

    public BlogHandler(
            BlogQueryRepository blogQueryRepository,
            BlogCommandRepository blogCommandRepository,
            SavedBlogCommandRepository savedBlogCommandRepository,
            SavedBlogQueryRepository savedBlogQueryRepository,
            LikedBlogCommandRepository likedBlogCommandRepository,
            LikedBlogQueryRepository likedBlogQueryRepository,
            CloudinaryService cloudinaryService,
            RedisCacheService cacheService,
            ValidateService validateService,
            BlogMapper blogMapper,
            CommentHandler commentHandler,
            NotiProducer notiProducer) {
        this.blogQueryRepository = blogQueryRepository;
        this.blogCommandRepository = blogCommandRepository;
        this.savedBlogCommandRepository = savedBlogCommandRepository;
        this.savedBlogQueryRepository = savedBlogQueryRepository;
        this.likedBlogCommandRepository = likedBlogCommandRepository;
        this.likedBlogQueryRepository = likedBlogQueryRepository;
        this.cloudinaryService = cloudinaryService;
        this.cacheService = cacheService;
        this.cacheKeys = CacheKeyBuilder.forService("blog");
        this.validateService = validateService;
        this.blogMapper = blogMapper;
        this.commentHandler = commentHandler;
        this.notiProducer = notiProducer;
    }

    private BlogDto builderBlog(UUID blogId,
            UUID userID,
            String title,
            String description,
            String category,
            String content,
            Boolean isVisibility,
            Instant createdAt,
            Instant updatedAt) {
        return BlogDto.builder()
                .id(blogId)
                .authorId(userID)
                .title(title)
                .description(description)
                .category(category)
                .content(content)
                .isVisibility(isVisibility)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // ========== Core Business Logic Methods ==========

    @Transactional
    public BlogDto handleCreateBlog(
            UUID userId,
            String title,
            String description,
            String category,
            String content,
            Boolean isVisibility,
            MultipartFile thumbnail) {
        try {
            log.info("Starting handleCreateBlog for userId={}, title={}", userId, title);

            validateService.validateUser(userId);
            log.debug("User validation passed for userId={}", userId);

            UUID blogId = UUID.randomUUID();
            Instant now = Instant.now();

            String thumbnailUrl = null;
            String thumbnailPublicId = null;

            if (thumbnail != null && !thumbnail.isEmpty()) {
                log.debug("Uploading thumbnail for blogId={}", blogId);
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(thumbnail);
                if (uploadResult.containsKey("error")) {
                    log.error("Thumbnail upload failed for blogId={}: {}", blogId, uploadResult.get("error"));
                    throw new OurException("Failed to upload thumbnail: " + uploadResult.get("error"), 500);
                }
                thumbnailUrl = (String) uploadResult.get("url");
                thumbnailPublicId = (String) uploadResult.get("publicId");
                log.debug("Thumbnail uploaded successfully for blogId={}, url={}", blogId, thumbnailUrl);
            }

            blogCommandRepository.insertBlog(
                    blogId,
                    userId,
                    title,
                    description,
                    category,
                    thumbnailUrl,
                    thumbnailPublicId,
                    content,
                    isVisibility,
                    now,
                    now);
            log.info("Blog created successfully: blogId={}", blogId);

            return builderBlog(blogId, userId, title, description,
                    category, content,
                    isVisibility, now, now);
        } catch (OurException e) {
            log.error("OurException in handleCreateBlog for userId={}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleCreateBlog for userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public BlogDto handleDuplicateBlog(UUID blogId, UUID userId) {
        try {
            log.info("Starting handleDuplicateBlog for blogId={}, userId={}", blogId, userId);

            validateService.validateUser(userId);
            log.debug("User validation passed for userId={}", userId);
            BlogDto originalBlog = validateService.validateBlog(blogId);
            log.debug("Blog validation passed for blogId={}", blogId);

            Instant now = Instant.now();
            UUID newBlogId = UUID.randomUUID();

            blogCommandRepository.insertBlog(
                    newBlogId,
                    userId,
                    originalBlog.getTitle(),
                    originalBlog.getDescription(),
                    originalBlog.getCategory(),
                    null,
                    null,
                    originalBlog.getContent(),
                    originalBlog.getIsVisibility(),
                    now,
                    now);
            log.info("Blog duplicated successfully: originalBlogId={}, newBlogId={}", blogId, newBlogId);

            return builderBlog(newBlogId, userId, originalBlog.getTitle(), originalBlog.getDescription(),
                    originalBlog.getCategory(), originalBlog.getContent(),
                    originalBlog.getIsVisibility(), now, now);
        } catch (OurException e) {
            log.error("OurException in handleDuplicateBlog for blogId={}, userId={}: {}", blogId, userId,
                    e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleDuplicateBlog for blogId={}, userId={}: {}", blogId, userId,
                    e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<BlogDto> handleGetAllBlogs(Boolean isVisibility) {
        try {
            log.info("Starting handleGetAllBlogs with isVisibility={}", isVisibility);

            String cacheKey = isVisibility == null
                    ? cacheKeys.forMethod("handleGetAllBlogs")
                    : cacheKeys.forMethodWithParam("handleGetAllBlogs", String.valueOf(isVisibility));
            List<BlogDto> blogs = cacheService.getCacheDataList(cacheKey, BlogDto.class);

            if (blogs == null) {
                log.debug("Cache miss for handleGetAllBlogs, fetching from database");
                blogs = blogQueryRepository.findAllBlogs(Pageable.unpaged()).stream()
                        .map(blogMapper::toDto)
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .collect(Collectors.toList());

                if (isVisibility != null) {
                    blogs = blogs.stream()
                            .filter(blog -> blog.getIsVisibility().equals(isVisibility))
                            .collect(Collectors.toList());
                }

                cacheService.setCacheData(cacheKey, blogs);
                log.debug("Fetched {} blogs from database and cached", blogs.size());
            }
            log.info("Retrieved {} blogs", blogs.size());

            return blogs;
        } catch (OurException e) {
            log.error("OurException in handleGetAllBlogs: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleGetAllBlogs: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public BlogDto handleGetBlog(UUID blogId) {
        try {
            log.info("Starting handleGetBlog for blogId={}", blogId);

            String cacheKey = cacheKeys.forMethodWithId("handleGetBlog", blogId);
            BlogDto blog = cacheService.getCacheData(cacheKey, BlogDto.class);

            if (blog == null) {
                log.debug("Cache miss for handleGetBlog, validating blogId={}", blogId);
                blog = validateService.validateBlog(blogId);

                List<CommentDto> comments = commentHandler.handleGetBlogComments(blogId);
                blog.setComments(comments);
                log.debug("Retrieved {} comments for blogId={}", comments.size(), blogId);

                List<UUID> likes = handleGetBlogLikes(blogId);
                blog.setLikes(likes);

                List<UUID> saves = handleGetBlogSaves(blogId);
                blog.setSaves(saves);

                cacheService.setCacheData(cacheKey, blog);
                log.debug("Blog cached for blogId={}", blogId);
            }
            log.info("Retrieved blog: blogId={}", blogId);

            return blog;
        } catch (OurException e) {
            log.error("OurException in handleGetBlog for blogId={}: {}", blogId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleGetBlog for blogId={}: {}", blogId,
                    e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<BlogDto> handleGetUserBlogs(UUID userId) {
        try {
            log.info("Starting handleGetUserBlogs for userId={}", userId);

            validateService.validateUser(userId);
            log.debug("User validation passed for userId={}", userId);

            String cacheKey = cacheKeys.forMethodWithId("handleGetUserBlogs", userId);
            List<BlogDto> blogs = cacheService.getCacheDataList(cacheKey, BlogDto.class);

            if (blogs == null) {
                log.debug("Cache miss for handleGetUserBlogs, fetching from database for userId={}", userId);
                blogs = blogQueryRepository.findBlogsByUserId(userId)
                        .stream()
                        .map(blogMapper::toDto)
                        .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                        .collect(Collectors.toList());

                cacheService.setCacheData(cacheKey, blogs);
                log.debug("Fetched {} blogs from database and cached for userId={}", blogs.size(), userId);
            }
            log.info("Retrieved {} blogs for userId={}", blogs.size(), userId);

            return blogs;
        } catch (OurException e) {
            log.error("OurException in handleGetUserBlogs for userId={}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleGetUserBlogs for userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public BlogDto handleUpdateBlog(
            UUID blogId,
            String title,
            String description,
            String category,
            String content,
            Boolean isVisibility,
            MultipartFile thumbnail) {
        try {
            log.info("Starting handleUpdateBlog for blogId={}", blogId);

            BlogDto existingBlog = validateService.validateBlog(blogId);
            log.debug("Blog validation passed for blogId={}", blogId);

            Instant now = Instant.now();

            String thumbnailUrl = existingBlog.getThumbnailUrl();
            String thumbnailPublicId = existingBlog.getThumbnailPublicId();

            if (thumbnail != null && !thumbnail.isEmpty()) {
                log.debug("Updating thumbnail for blogId={}", blogId);
                // Delete old thumbnail if exists
                if (thumbnailPublicId != null && !thumbnailPublicId.isEmpty()) {
                    cloudinaryService.deleteImage(thumbnailPublicId);
                    log.debug("Deleted old thumbnail for blogId={}", blogId);
                }

                // Upload new thumbnail
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(thumbnail);
                if (uploadResult.containsKey("error")) {
                    log.error("Thumbnail upload failed for blogId={}: {}", blogId, uploadResult.get("error"));
                    throw new OurException("Failed to upload thumbnail: " + uploadResult.get("error"), 500);
                }
                thumbnailUrl = (String) uploadResult.get("url");
                thumbnailPublicId = (String) uploadResult.get("publicId");
                log.debug("New thumbnail uploaded for blogId={}, url={}", blogId, thumbnailUrl);
            }

            Blog.Category categoryEnum = category != null
                    ? Blog.Category.valueOf(category.toLowerCase())
                    : Blog.Category.valueOf(existingBlog.getCategory());

            blogCommandRepository.updateBlog(
                    blogId,
                    title != null ? title : existingBlog.getTitle(),
                    description != null ? description : existingBlog.getDescription(),
                    categoryEnum,
                    content != null ? content : existingBlog.getContent(),
                    thumbnailUrl,
                    thumbnailPublicId,
                    isVisibility != null ? isVisibility : existingBlog.getIsVisibility(),
                    now);
            log.info("Blog updated successfully: blogId={}", blogId);

            return builderBlog(blogId, existingBlog.getAuthorId(), title, description, categoryEnum.toString(), content,
                    isVisibility, existingBlog.getCreatedAt(), now);
        } catch (OurException e) {
            log.error("OurException in handleUpdateBlog for blogId={}: {}", blogId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleUpdateBlog for blogId={}: {}", blogId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean handleDeleteBlog(UUID blogId) {
        try {
            log.info("Starting handleDeleteBlog for blogId={}", blogId);

            Blog blog = blogQueryRepository.findBlogById(blogId)
                    .orElseThrow(() -> new OurException("Blog not found", 404));
            log.debug("Blog found for deletion: blogId={}", blogId);

            // Delete thumbnail from Cloudinary if exists
            if (blog.getThumbnailPublicId() != null && !blog.getThumbnailPublicId().isEmpty()) {
                cloudinaryService.deleteImage(blog.getThumbnailPublicId());
                log.debug("Deleted thumbnail for blogId={}", blogId);
            }

            blogCommandRepository.deleteBlogById(blogId);
            log.info("Blog deleted successfully: blogId={}", blogId);
            return true;
        } catch (OurException e) {
            log.error("OurException in handleDeleteBlog for blogId={}: {}", blogId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleDeleteBlog for blogId={}: {}", blogId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean handleSaveBlog(UUID blogId, UUID userId) {
        try {
            log.info("Starting handleSaveBlog for blogId={}, userId={}", blogId, userId);

            validateService.validateUser(userId);
            log.debug("User validation passed for userId={}", userId);

            validateService.validateBlog(blogId);
            log.debug("Blog validation passed for blogId={}", blogId);

            // Check if already saved
            boolean alreadySaved = savedBlogQueryRepository.findSavedBlogsByUserId(userId)
                    .stream()
                    .anyMatch(sb -> sb.getBlogId().equals(blogId));
            if (alreadySaved) {
                log.warn("Blog already saved for userId={}, blogId={}", userId, blogId);
                throw new OurException("Blog already saved", 400);
            }

            UUID savedBlogId = UUID.randomUUID();
            savedBlogCommandRepository.saveSavedBlog(savedBlogId, userId, blogId);
            log.info("Blog saved successfully: savedBlogId={}", savedBlogId);

            return true;
        } catch (OurException e) {
            log.error("OurException in handleSaveBlog for blogId={}, userId={}: {}", blogId, userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleSaveBlog for blogId={}, userId={}: {}", blogId, userId,
                    e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<BlogDto> handleGetUserSavedBlogs(UUID userId) {
        try {
            log.info("Starting handleGetUserSavedBlogs for userId={}", userId);

            validateService.validateUser(userId);
            log.debug("User validation passed for userId={}", userId);

            String cacheKey = cacheKeys.forMethodWithId("handleGetUserSavedBlogs", userId);
            List<BlogDto> blogs = cacheService.getCacheDataList(cacheKey, BlogDto.class);

            if (blogs == null) {
                log.debug("Cache miss for handleGetUserSavedBlogs, fetching from database for userId={}", userId);
                blogs = savedBlogQueryRepository.findBlogsByUserSaved(userId)
                        .stream()
                        .map(blogMapper::toDto)
                        .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                        .collect(Collectors.toList());

                cacheService.setCacheData(cacheKey, blogs);
                log.debug("Fetched {} saved blogs from database and cached for userId={}", blogs.size(), userId);
            }
            log.info("Retrieved {} saved blogs for userId={}", blogs.size(), userId);

            return blogs;
        } catch (OurException e) {
            log.error("OurException in handleGetUserSavedBlogs for userId={}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleGetUserSavedBlogs for userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean handleUnsaveBlog(UUID blogId, UUID userId) {
        try {
            log.info("Starting handleUnsaveBlog for blogId={}, userId={}", blogId, userId);

            validateService.validateUser(userId);
            log.debug("User validation passed for userId={}", userId);

            validateService.validateBlog(blogId);
            log.debug("Blog validation passed for blogId={}", blogId);

            SavedBlog savedBlog = savedBlogQueryRepository.findSavedBlogsByUserId(userId)
                    .stream()
                    .filter(sb -> sb.getBlogId().equals(blogId))
                    .findFirst()
                    .orElseThrow(() -> new OurException("Saved blog not found", 404));
            log.debug("Found saved blog for deletion: savedBlogId={}", savedBlog.getId());

            savedBlogCommandRepository.deleteById(savedBlog.getId());
            log.info("Blog unsaved successfully for blogId={}, userId={}", blogId, userId);

            return true;
        } catch (OurException e) {
            log.error("OurException in handleUnsaveBlog for blogId={}, userId={}: {}", blogId, userId,
                    e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleUnsaveBlog for blogId={}, userId={}: {}", blogId, userId,
                    e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<UUID> handleGetBlogLikes(UUID blogId) {
        try {
            log.info("Starting handleGetBlogLikes for blogId={}", blogId);

            validateService.validateBlog(blogId);
            log.debug("Blog validation passed for blogId={}", blogId);

            String cacheKey = cacheKeys.forMethodWithId("handleGetBlogLikes", blogId);
            List<UUID> userIds = cacheService.getCacheDataList(cacheKey, UUID.class);

            if (userIds == null) {
                log.debug("Cache miss for handleGetBlogLikes, fetching from database for blogId={}", blogId);
                userIds = likedBlogQueryRepository.findLikedBlogsByBlogId(blogId)
                        .stream()
                        .map(LikedBlog::getUserId)
                        .collect(Collectors.toList());

                cacheService.setCacheData(cacheKey, userIds);
                log.debug("Fetched {} user IDs who liked blogId={} from database and cached", userIds.size(), blogId);
            }

            log.info("Retrieved {} users who liked blogId={}", userIds.size(), blogId);
            return userIds;
        } catch (OurException e) {
            log.error("OurException in handleGetBlogLikes for blogId={}: {}", blogId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleGetBlogLikes for blogId={}: {}", blogId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<UUID> handleGetBlogSaves(UUID blogId) {
        try {
            log.info("Starting handleGetBlogSaves for blogId={}", blogId);

            validateService.validateBlog(blogId);
            log.debug("Blog validation passed for blogId={}", blogId);

            String cacheKey = cacheKeys.forMethodWithId("handleGetBlogSaves", blogId);
            List<UUID> userIds = cacheService.getCacheDataList(cacheKey, UUID.class);

            if (userIds == null) {
                log.debug("Cache miss for handleGetBlogSaves, fetching from database for blogId={}", blogId);
                userIds = savedBlogQueryRepository.findSavedBlogsByBlogId(blogId)
                        .stream()
                        .map(SavedBlog::getUserId)
                        .collect(Collectors.toList());

                cacheService.setCacheData(cacheKey, userIds);
                log.debug("Fetched {} user IDs who saved blogId={} from database and cached", userIds.size(), blogId);
            }

            log.info("Retrieved {} users who saved blogId={}", userIds.size(), blogId);
            return userIds;
        } catch (OurException e) {
            log.error("OurException in handleGetBlogSaves for blogId={}: {}", blogId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleGetBlogSaves for blogId={}: {}", blogId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean handleLikeBlog(UUID blogId, UUID userId) {
        try {
            log.info("Starting handleLikeBlog for blogId={}, userId={}", blogId, userId);

            validateService.validateUser(userId);
            log.debug("User validation passed for userId={}", userId);

            BlogDto blog = validateService.validateBlog(blogId);
            log.debug("Blog validation passed for blogId={}", blogId);

            // Check if already liked
            boolean alreadyLiked = likedBlogQueryRepository.findLikedBlogsByUserId(userId)
                    .stream()
                    .anyMatch(lb -> lb.getBlogId().equals(blogId));
            if (alreadyLiked) {
                log.warn("Blog already liked for userId={}, blogId={}", userId, blogId);
                throw new OurException("Blog already liked", 400);
            }

            UUID likedBlogId = UUID.randomUUID();
            likedBlogCommandRepository.likeBlog(likedBlogId, userId, blogId);
            log.info("Blog liked successfully: likedBlogId={}", likedBlogId);

            // Send notification to blog owner if liker is not the owner
            if (!blog.getAuthorId().equals(userId)) {
                try {
                    NotificationMessage notiMessage = NotificationMessage.builder()
                            .authorId(userId)
                            .receiverId(blog.getAuthorId())
                            .blogId(blogId)
                            .content("liked your blog: " + blog.getTitle())
                            .type("LIKE")
                            .build();

                    notiProducer.sendLikeNotification(notiMessage);
                    log.debug("Like notification sent for blogId={}", blogId);
                } catch (Exception e) {
                    log.error("Failed to send like notification but like was saved: {}", e.getMessage());
                }
            }

            return true;
        } catch (OurException e) {
            log.error("OurException in handleLikeBlog for blogId={}, userId={}: {}", blogId, userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleLikeBlog for blogId={}, userId={}: {}", blogId, userId,
                    e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<BlogDto> handleGetUserLikedBlogs(UUID userId) {
        try {
            log.info("Starting handleGetUserLikedBlogs for userId={}", userId);

            validateService.validateUser(userId);
            log.debug("User validation passed for userId={}", userId);

            String cacheKey = cacheKeys.forMethodWithId("handleGetUserLikedBlogs", userId);
            List<BlogDto> blogs = cacheService.getCacheDataList(cacheKey, BlogDto.class);

            if (blogs == null) {
                log.debug("Cache miss for handleGetUserLikedBlogs:{}, fetching from database", userId);
                List<Blog> likedBlogs = likedBlogQueryRepository.findLikedBlogsDetailsByUserId(userId);
                blogs = likedBlogs.stream()
                        .map(blogMapper::toDto)
                        .collect(Collectors.toList());

                cacheService.setCacheData(cacheKey, blogs);
                log.debug("Fetched {} liked blogs for userId={} from database and cached", blogs.size(), userId);
            }

            log.info("Completed handleGetUserLikedBlogs for userId={} with {} blogs", userId, blogs.size());
            return blogs;
        } catch (OurException e) {
            log.error("OurException in handleGetUserLikedBlogs for userId={}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleGetUserLikedBlogs for userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean handleUnlikeBlog(UUID blogId, UUID userId) {
        try {
            log.info("Starting handleUnlikeBlog for blogId={}, userId={}", blogId, userId);

            validateService.validateUser(userId);
            log.debug("User validation passed for userId={}", userId);

            validateService.validateBlog(blogId);
            log.debug("Blog validation passed for blogId={}", blogId);

            LikedBlog likedBlog = likedBlogQueryRepository.findLikedBlogsByUserId(userId)
                    .stream()
                    .filter(lb -> lb.getBlogId().equals(blogId))
                    .findFirst()
                    .orElseThrow(() -> new OurException("Liked blog not found", 404));
            log.debug("Found liked blog for deletion: likedBlogId={}", likedBlog.getId());

            likedBlogCommandRepository.deleteById(likedBlog.getId());
            log.info("Blog unliked successfully for blogId={}, userId={}", blogId, userId);

            // Invalidate cache
            // String cacheKey = cacheKeys.forMethodWithId("handleGetUserLikedBlogs",
            // userId);
            // cacheService.deleteCacheData(cacheKey);
            log.debug("Cache invalidated for handleGetUserLikedBlogs:{}", userId);

            return true;
        } catch (OurException e) {
            log.error("OurException in handleUnlikeBlog for blogId={}, userId={}: {}", blogId, userId,
                    e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception in handleUnlikeBlog for blogId={}, userId={}: {}", blogId, userId,
                    e.getMessage(), e);
            throw e;
        }
    }
}
