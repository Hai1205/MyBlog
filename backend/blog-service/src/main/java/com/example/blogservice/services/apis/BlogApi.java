package com.example.blogservice.services.apis;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogservice.dtos.*;
import com.example.blogservice.dtos.requests.*;
import com.example.blogservice.dtos.responses.*;
import com.example.blogservice.entities.*;
import com.example.blogservice.exceptions.OurException;
import com.example.blogservice.mappers.*;
import com.example.blogservice.repositories.blogRepositories.*;
import com.example.blogservice.repositories.savedBlogRepositories.SavedBlogCommandRepository;
import com.example.blogservice.repositories.savedBlogRepositories.SavedBlogQueryRepository;
import com.example.cloudinarycommon.CloudinaryService;
import com.example.blogservice.services.feigns.UserFeignClient;
import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.services.ApiResponseHandler;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BlogApi extends BaseApi {
    private final BlogQueryRepository blogQueryRepository;
    private final BlogCommandRepository blogCommandRepository;
    private final SavedBlogCommandRepository savedBlogCommandRepository;
    private final SavedBlogQueryRepository savedBlogQueryRepository;
    private final BlogMapper blogMapper;
    private final CloudinaryService cloudinaryService;
    private final UserFeignClient userFeignClient;
    private final ObjectMapper objectMapper;
    private final RedisCacheService cacheService;
    private final ApiResponseHandler<Response> responseHandler;
    private final CommentApi commentApi;
    private final CacheKeyBuilder cacheKeys;

    public BlogApi(
            BlogQueryRepository blogQueryRepository,
            BlogCommandRepository blogCommandRepository,
            SavedBlogCommandRepository savedBlogCommandRepository,
            SavedBlogQueryRepository savedBlogQueryRepository,
            BlogMapper blogMapper,
            CloudinaryService cloudinaryService,
            UserFeignClient userFeignClient,
            ObjectMapper redisObjectMapper,
            RedisCacheService cacheService,
            ApiResponseHandler<Response> responseHandler,
            CommentApi commentApi) {
        this.blogQueryRepository = blogQueryRepository;
        this.blogCommandRepository = blogCommandRepository;
        this.savedBlogCommandRepository = savedBlogCommandRepository;
        this.savedBlogQueryRepository = savedBlogQueryRepository;
        this.blogMapper = blogMapper;
        this.cloudinaryService = cloudinaryService;
        this.userFeignClient = userFeignClient;
        this.objectMapper = redisObjectMapper;
        this.cacheService = cacheService;
        this.responseHandler = responseHandler;
        this.commentApi = commentApi;
        this.cacheKeys = CacheKeyBuilder.forService("blog");
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

    // ========== Core Business Logic Methods ==========

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
            logger.info("Creating blog for userId={} title='{}'", userId, title);

            validateUser(userId);

            UUID blogId = UUID.randomUUID();
            Instant now = Instant.now();

            String thumbnailUrl = null;
            String thumbnailPublicId = null;

            if (thumbnail != null && !thumbnail.isEmpty()) {
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(thumbnail);
                if (uploadResult.containsKey("error")) {
                    throw new OurException("Failed to upload thumbnail: " + uploadResult.get("error"), 500);
                }
                thumbnailUrl = (String) uploadResult.get("url");
                thumbnailPublicId = (String) uploadResult.get("publicId");
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

            Blog savedBlog = blogQueryRepository.findBlogById(blogId)
                    .orElseThrow(() -> new OurException("Failed to create blog", 500));

            logger.info("Blog created successfully: blogId={}", blogId);
            return blogMapper.toDto(savedBlog);
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating blog: {}", e.getMessage(), e);
            throw new OurException("Failed to create blog", 500);
        }
    }

    @Transactional(readOnly = true)
    public List<BlogDto> handleGetAllBlogs(Boolean isVisibility) {
        try {
            logger.debug("Fetching all blogs with visibility filter: {}", isVisibility);

            // If no filter, use simple cache key
            String cacheKey = isVisibility == null
                    ? cacheKeys.forMethod("handleGetAllBlogs")
                    : cacheKeys.forMethodWithParam("handleGetAllBlogs", String.valueOf(isVisibility));

            return cacheService.executeWithCacheList(
                    cacheKey,
                    BlogDto.class,
                    () -> {
                        List<Blog> blogs = blogQueryRepository.findAllBlogs();

                        // Filter by visibility if param provided
                        if (isVisibility != null) {
                            blogs = blogs.stream()
                                    .filter(blog -> blog.getIsVisibility().equals(isVisibility))
                                    .collect(Collectors.toList());
                        }

                        return blogs.stream()
                                .map(blogMapper::toDto)
                                .collect(Collectors.toList());
                    });
        } catch (Exception e) {
            logger.error("Error getting all blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get blogs", 500);
        }
    }

    @Transactional(readOnly = true)
    public BlogDto handleGetBlogById(UUID blogId, UUID userId) {
        try {
            logger.debug("Fetching blog by id={}", blogId);
            return cacheService.executeWithCache(
                    cacheKeys.forMethodWithId("handleGetBlogById", blogId),
                    BlogDto.class,
                    () -> {
                        Blog blog = blogQueryRepository.findBlogById(blogId)
                                .orElseThrow(() -> new OurException("Blog not found", 404));
                        BlogDto blogDto = blogMapper.toDto(blog);

                        try {
                            logger.debug("Fetching user by id={}", blog.getAuthorId());
                            Response res = userFeignClient.getUserById(blog.getAuthorId());
                            UserDto author = res.getUser();
                            logger.debug("Fetched user {}", author.getUsername());
                            blogDto.setAuthor(author);
                        } catch (Exception e) {
                            logger.error("Error fetching author for blog={}: {}", blogId, e.getMessage());
                        }

                        try {
                            List<CommentDto> comments = commentApi.handleGetBlogComments(blogId);
                            blogDto.setComments(comments);
                        } catch (Exception e) {
                            logger.error("Error fetching comments for blog={}: {}", blogId, e.getMessage());
                        }

                        if (userId != null) {
                            try {
                                boolean isSaved = savedBlogQueryRepository.findSavedBlogsByUserId(userId)
                                        .stream()
                                        .anyMatch(sb -> sb.getBlogId().equals(blogId));
                                blogDto.setIsSaved(isSaved);
                                logger.debug("Blog saved status for user={}: {}", userId, isSaved);
                            } catch (Exception e) {
                                logger.error("Error checking saved status for blog={}: {}", blogId, e.getMessage());
                                blogDto.setIsSaved(false);
                            }
                        } else {
                            blogDto.setIsSaved(false);
                        }

                        return blogDto;
                    });
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting blog: {}", e.getMessage(), e);
            throw new OurException("Failed to get blog", 500);
        }
    }

    @Transactional(readOnly = true)
    public List<BlogDto> handleGetUserBlogs(UUID userId) {
        try {
            logger.debug("Fetching blogs for userId={}", userId);
            validateUser(userId);

            return cacheService.executeWithCacheList(
                    cacheKeys.forMethodWithId("handleGetUserBlogs", userId),
                    BlogDto.class,
                    () -> blogQueryRepository.findBlogsByUserId(userId)
                            .stream()
                            .map(blogMapper::toDto)
                            .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                            .collect(Collectors.toList()));
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting user blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get user blogs", 500);
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
            logger.info("Updating blog id={}", blogId);

            Blog existingBlog = blogQueryRepository.findBlogById(blogId)
                    .orElseThrow(() -> new OurException("Blog not found", 404));

            Instant now = Instant.now();

            String thumbnailUrl = existingBlog.getThumbnailUrl();
            String thumbnailPublicId = existingBlog.getThumbnailPublicId();

            if (thumbnail != null && !thumbnail.isEmpty()) {
                // Delete old thumbnail if exists
                if (thumbnailPublicId != null && !thumbnailPublicId.isEmpty()) {
                    cloudinaryService.deleteImage(thumbnailPublicId);
                }

                // Upload new thumbnail
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(thumbnail);
                if (uploadResult.containsKey("error")) {
                    throw new OurException("Failed to upload thumbnail: " + uploadResult.get("error"), 500);
                }
                thumbnailUrl = (String) uploadResult.get("url");
                thumbnailPublicId = (String) uploadResult.get("publicId");
            }

            blogCommandRepository.updateBlog(
                    blogId,
                    title != null ? title : existingBlog.getTitle(),
                    description != null ? description : existingBlog.getDescription(),
                    category != null ? category : existingBlog.getCategory().name(),
                    content != null ? content : existingBlog.getContent(),
                    thumbnailUrl,
                    thumbnailPublicId,
                    isVisibility != null ? isVisibility : existingBlog.getIsVisibility(),
                    now);

            Blog updatedBlog = blogQueryRepository.findBlogById(blogId)
                    .orElseThrow(() -> new OurException("Failed to get updated blog", 500));

            logger.info("Blog updated successfully: blogId={}", blogId);
            return blogMapper.toDto(updatedBlog);
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating blog: {}", e.getMessage(), e);
            throw new OurException("Failed to update blog", 500);
        }
    }

    @Transactional
    public boolean handleDeleteBlog(UUID blogId) {
        try {
            logger.info("Deleting blog id={}", blogId);

            Blog blog = blogQueryRepository.findBlogById(blogId)
                    .orElseThrow(() -> new OurException("Blog not found", 404));

            // Delete thumbnail from Cloudinary if exists
            if (blog.getThumbnailPublicId() != null && !blog.getThumbnailPublicId().isEmpty()) {
                cloudinaryService.deleteImage(blog.getThumbnailPublicId());
            }

            blogCommandRepository.deleteBlogById(blogId);

            logger.info("Blog deleted successfully: blogId={}", blogId);
            return true;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting blog: {}", e.getMessage(), e);
            throw new OurException("Failed to delete blog", 500);
        }
    }

    @Transactional
    public BlogDto handleSaveBlog(UUID blogId, UUID userId) {
        try {
            logger.info("Saving blog for userId={} blogId={}", userId, blogId);

            validateUser(userId);

            Blog blog = blogQueryRepository.findBlogById(blogId)
                    .orElseThrow(() -> new OurException("Blog not found", 404));

            // Check if already saved
            boolean alreadySaved = savedBlogQueryRepository.findSavedBlogsByUserId(userId)
                    .stream()
                    .anyMatch(sb -> sb.getBlogId().equals(blogId));
            if (alreadySaved) {
                throw new OurException("Blog already saved", 400);
            }

            SavedBlog savedBlog = new SavedBlog();
            savedBlog.setId(UUID.randomUUID());
            savedBlog.setUserId(userId);
            savedBlog.setBlogId(blogId);

            savedBlogCommandRepository.save(savedBlog);

            logger.info("Blog saved successfully: savedBlogId={}", savedBlog.getId());
            return blogMapper.toDto(blog);
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error saving blog: {}", e.getMessage(), e);
            throw new OurException("Failed to save blog", 500);
        }
    }

    @Transactional(readOnly = true)
    public List<BlogDto> handleGetUserSavedBlogs(UUID userId) {
        try {
            logger.debug("Fetching saved blogs for userId={}", userId);
            validateUser(userId);

            return cacheService.executeWithCacheList(
                    cacheKeys.forMethodWithId("handleGetUserSavedBlogs", userId),
                    BlogDto.class,
                    () -> savedBlogQueryRepository.findBlogsByUserSaved(userId)
                            .stream()
                            .map(blogMapper::toDto)
                            .collect(Collectors.toList()));
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting saved blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get saved blogs", 500);
        }
    }

    @Transactional
    public boolean handleUnsaveBlog(UUID blogId, UUID userId) {
        try {
            logger.info("Unsaving blog for userId={} blogId={}", userId, blogId);

            validateUser(userId);

            blogQueryRepository.findBlogById(blogId)
                    .orElseThrow(() -> new OurException("Blog not found", 404));

            // Find the saved blog entry
            SavedBlog savedBlog = savedBlogQueryRepository.findSavedBlogsByUserId(userId)
                    .stream()
                    .filter(sb -> sb.getBlogId().equals(blogId))
                    .findFirst()
                    .orElseThrow(() -> new OurException("Saved blog not found", 404));

            savedBlogCommandRepository.deleteById(savedBlog.getId());

            // Invalidate cache
            cacheService.invalidate(cacheKeys.forMethodWithId("handleGetUserSavedBlogs", userId));

            logger.info("Blog unsaved successfully: savedBlogId={}", savedBlog.getId());
            return true;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error unsaving blog: {}", e.getMessage(), e);
            throw new OurException("Failed to unsave blog", 500);
        }
    }

    // ========== Statistics Methods ==========

    public List<BlogDto> handleGetBlogsCreatedInRange(Instant startDate, Instant endDate) {
        try {
            return cacheService.executeWithCacheList(
                    cacheKeys.forMethodWithParams("handleGetBlogsCreatedInRange", startDate, endDate),
                    BlogDto.class,
                    () -> blogQueryRepository.findBlogsCreatedBetween(startDate, endDate)
                            .stream()
                            .map(blogMapper::toDto)
                            .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Error getting blogs in range: {}", e.getMessage(), e);
            throw new OurException("Failed to get blogs in range", 500);
        }
    }

    public long handleGetBlogsCountCreatedInRange(Instant startDate, Instant endDate) {
        try {
            return cacheService.executeWithCachePrimitive(
                    cacheKeys.forMethodWithParams("handleGetBlogsCountCreatedInRange", startDate, endDate),
                    () -> blogQueryRepository.countBlogsCreatedBetween(startDate, endDate));
        } catch (Exception e) {
            logger.error("Error counting blogs in range: {}", e.getMessage(), e);
            throw new OurException("Failed to count blogs in range", 500);
        }
    }

    public List<BlogDto> handleGetRecentBlogs(int limit) {
        try {
            return cacheService.executeWithCacheList(
                    cacheKeys.forMethodWithParam("handleGetRecentBlogs", limit),
                    BlogDto.class,
                    () -> blogQueryRepository.findRecentBlogs(PageRequest.of(0, limit))
                            .stream()
                            .map(blogMapper::toDto)
                            .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Error getting recent blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get recent blogs", 500);
        }
    }

    // ========== Public Response Methods ==========

    public Response createBlog(UUID userId, String dataJson, MultipartFile thumbnail) {
        return responseHandler.executeWithResponse(
                cacheKeys.custom("createBlog", userId.toString()),
                45,
                () -> {
                    try {
                        CreateBlogRequest request = objectMapper.readValue(dataJson, CreateBlogRequest.class);
                        return handleCreateBlog(
                                userId,
                                request.getTitle(),
                                request.getDescription(),
                                request.getCategory(),
                                request.getContent(),
                                request.getIsVisibility(),
                                thumbnail);
                    } catch (Exception e) {
                        logger.error("Error parsing create blog request: {}", e.getMessage(), e);
                        throw new OurException("Invalid request data", 400);
                    }
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setBlog,
                "Blog created successfully",
                201);
    }

    public Response getAllBlogs(Boolean isVisibility) {
        String cacheKey = isVisibility == null
                ? cacheKeys.forMethod("getAllBlogs")
                : cacheKeys.forMethodWithParam("getAllBlogs", String.valueOf(isVisibility));

        return responseHandler.executeWithResponse(
                cacheKey,
                90,
                () -> handleGetAllBlogs(isVisibility),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setBlogs,
                "Blogs retrieved successfully",
                200);
    }

    public Response getBlog(UUID blogId, UUID userId) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("getBlog", blogId),
                90,
                () -> handleGetBlogById(blogId, userId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setBlog,
                "Blog retrieved successfully",
                200);
    }

    public Response getUserBlogs(UUID userId) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("getUserBlogs", userId),
                90,
                () -> handleGetUserBlogs(userId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setBlogs,
                "User blogs retrieved successfully",
                200);
    }

    public Response updateBlog(UUID blogId, String dataJson, MultipartFile thumbnail) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("updateBlog", blogId),
                45,
                () -> {
                    try {
                        UpdateBlogRequest request = objectMapper.readValue(dataJson, UpdateBlogRequest.class);
                        return handleUpdateBlog(
                                blogId,
                                request.getTitle(),
                                request.getDescription(),
                                request.getCategory(),
                                request.getContent(),
                                request.getIsVisibility(),
                                thumbnail);
                    } catch (Exception e) {
                        logger.error("Error parsing update blog request: {}", e.getMessage(), e);
                        throw new OurException("Invalid request data", 400);
                    }
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setBlog,
                "Blog updated successfully",
                200);
    }

    public Response deleteBlog(UUID blogId) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("deleteBlog", blogId),
                20,
                () -> handleDeleteBlog(blogId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                null,
                "Blog deleted successfully",
                200);
    }

    public Response saveBlog(UUID userId, UUID blogId) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("saveBlog", userId),
                45,
                () -> handleSaveBlog(blogId, userId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setBlog,
                "Blog saved successfully",
                200);
    }

    public Response unsaveBlog(UUID userId, UUID blogId) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("unsaveBlog", userId),
                45,
                () -> handleUnsaveBlog(blogId, userId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                null,
                "Blog unsaved successfully",
                200);
    }

    public Response getUserSavedBlogs(UUID userId) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("getUserSavedBlogs", userId),
                90,
                () -> handleGetUserSavedBlogs(userId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setBlogs,
                "Saved blogs retrieved successfully",
                200);
    }

    public Response getRecentBlogs(int limit) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("getRecentBlogs", limit),
                90,
                () -> handleGetRecentBlogs(limit),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setBlogs,
                "Recent blogs retrieved successfully",
                200);
    }
}