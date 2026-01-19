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
import com.example.rediscommon.services.RedisService;
import com.example.rediscommon.services.RateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
    private final RedisService redisService;
    private final RateLimiterService rateLimiterService;
    private final CommentApi commentApi;

    public BlogApi(
            BlogQueryRepository blogQueryRepository,
            BlogCommandRepository blogCommandRepository,
            SavedBlogCommandRepository savedBlogCommandRepository,
            SavedBlogQueryRepository savedBlogQueryRepository,
            BlogMapper blogMapper,
            CloudinaryService cloudinaryService,
            UserFeignClient userFeignClient,
            ObjectMapper redisObjectMapper,
            RedisService redisService,
            RateLimiterService rateLimiterService,
            CommentApi commentApi) {
        this.blogQueryRepository = blogQueryRepository;
        this.blogCommandRepository = blogCommandRepository;
        this.savedBlogCommandRepository = savedBlogCommandRepository;
        this.savedBlogQueryRepository = savedBlogQueryRepository;
        this.blogMapper = blogMapper;
        this.cloudinaryService = cloudinaryService;
        this.userFeignClient = userFeignClient;
        this.objectMapper = redisObjectMapper;
        this.redisService = redisService;
        this.rateLimiterService = rateLimiterService;
        this.commentApi = commentApi;
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
    public List<BlogDto> handleGetAllBlogs() {
        try {
            logger.debug("Fetching all blogs");
            String cacheKey = "blog:handleGetAllBlogs:all";

            // Check cache first
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for key: {}", cacheKey);
                // Convert to List<BlogDto> safely
                if (cached instanceof List) {
                    List<?> list = (List<?>) cached;
                    return list.stream()
                            .map(item -> objectMapper.convertValue(item, BlogDto.class))
                            .collect(Collectors.toList());
                }
            }

            // Cache miss - fetch from DB
            List<BlogDto> blogs = blogQueryRepository
                    .findAllBlogs()
                    .stream()
                    .map(blogMapper::toDto)
                    .collect(Collectors.toList());

            // Store in cache with 10 minutes TTL
            redisService.set(cacheKey, blogs, 10, TimeUnit.MINUTES);
            logger.debug("Cached result for key: {}", cacheKey);

            return blogs;
        } catch (Exception e) {
            logger.error("Error getting all blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get blogs", 500);
        }
    }

    @Transactional(readOnly = true)
    public BlogDto handleGetBlogById(UUID blogId) {
        try {
            logger.debug("Fetching blog by id={}", blogId);
            String cacheKey = "blog:handleGetBlogById:" + blogId.toString();

            // Check cache first
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for key: {}", cacheKey);
                // Convert LinkedHashMap to BlogDto if needed
                if (cached instanceof BlogDto) {
                    return (BlogDto) cached;
                } else {
                    // Handle case where Redis returns LinkedHashMap
                    return objectMapper.convertValue(cached, BlogDto.class);
                }
            }

            // Cache miss - fetch from DB
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

            redisService.set(cacheKey, blogDto, 10, TimeUnit.MINUTES);
            logger.debug("Cached result for key: {}", cacheKey);

            return blogDto;
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
            String cacheKey = "blog:handleGetUserBlogs:" + userId.toString();

            // Check cache first
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for key: {}", cacheKey);
                // Convert to List<BlogDto> safely
                if (cached instanceof List) {
                    List<?> list = (List<?>) cached;
                    return list.stream()
                            .map(item -> objectMapper.convertValue(item, BlogDto.class))
                            .collect(Collectors.toList());
                }
            }

            validateUser(userId);

            List<BlogDto> blogs = blogQueryRepository
                    .findBlogsByUserId(userId)
                    .stream()
                    .map(blogMapper::toDto)
                    .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                    .collect(Collectors.toList());

            // Store in cache with 10 minutes TTL
            redisService.set(cacheKey, blogs, 10, TimeUnit.MINUTES);
            logger.debug("Cached result for key: {}", cacheKey);

            return blogs;
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
            String cacheKey = "blog:handleGetUserSavedBlogs:" + userId.toString();

            // Check cache first
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for key: {}", cacheKey);
                // Convert to List<BlogDto> safely
                if (cached instanceof List) {
                    List<?> list = (List<?>) cached;
                    return list.stream()
                            .map(item -> objectMapper.convertValue(item, BlogDto.class))
                            .collect(Collectors.toList());
                }
            }

            validateUser(userId);

            List<Blog> savedBlogs = savedBlogQueryRepository.findBlogsByUserSaved(userId);
            List<BlogDto> blogs = savedBlogs.stream()
                    .map(blogMapper::toDto)
                    .collect(Collectors.toList());

            // Store in cache with 10 minutes TTL
            redisService.set(cacheKey, blogs, 10, TimeUnit.MINUTES);
            logger.debug("Cached result for key: {}", cacheKey);

            return blogs;
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
            String cacheKey = "blog:handleGetUserSavedBlogs:" + userId.toString();
            redisService.delete(cacheKey);

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

    public long handleGetTotalBlogs() {
        try {
            String cacheKey = "blog:handleGetTotalBlogs:count";

            // Check cache first
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for key: {}", cacheKey);
                return Long.parseLong(cached.toString());
            }

            long count = blogQueryRepository.countTotalBlogs();

            // Store in cache with 10 minutes TTL
            redisService.set(cacheKey, count, 10, TimeUnit.MINUTES);
            logger.debug("Cached result for key: {}", cacheKey);

            return count;
        } catch (Exception e) {
            logger.error("Error getting total blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get total blogs", 500);
        }
    }

    public List<BlogDto> handleGetBlogsCreatedInRange(Instant startDate, Instant endDate) {
        try {
            String cacheKey = "blog:handleGetBlogsCreatedInRange:" + startDate.toString() + ":" + endDate.toString();

            // Check cache first
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for key: {}", cacheKey);
                // Convert to List<BlogDto> safely
                if (cached instanceof List) {
                    List<?> list = (List<?>) cached;
                    return list.stream()
                            .map(item -> objectMapper.convertValue(item, BlogDto.class))
                            .collect(Collectors.toList());
                }
            }

            List<BlogDto> blogs = blogQueryRepository
                    .findBlogsCreatedBetween(startDate, endDate)
                    .stream()
                    .map(blogMapper::toDto)
                    .collect(Collectors.toList());

            // Store in cache with 10 minutes TTL
            redisService.set(cacheKey, blogs, 10, TimeUnit.MINUTES);
            logger.debug("Cached result for key: {}", cacheKey);

            return blogs;
        } catch (Exception e) {
            logger.error("Error getting blogs in range: {}", e.getMessage(), e);
            throw new OurException("Failed to get blogs in range", 500);
        }
    }

    public long handleGetBlogsCountCreatedInRange(Instant startDate, Instant endDate) {
        try {
            String cacheKey = "blog:handleGetBlogsCountCreatedInRange:" + startDate.toString() + ":"
                    + endDate.toString();

            // Check cache first
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for key: {}", cacheKey);
                return Long.parseLong(cached.toString());
            }

            long count = blogQueryRepository.countBlogsCreatedBetween(startDate, endDate);

            // Store in cache with 10 minutes TTL
            redisService.set(cacheKey, count, 10, TimeUnit.MINUTES);
            logger.debug("Cached result for key: {}", cacheKey);

            return count;
        } catch (Exception e) {
            logger.error("Error counting blogs in range: {}", e.getMessage(), e);
            throw new OurException("Failed to count blogs in range", 500);
        }
    }

    public List<BlogDto> handleGetRecentBlogs(int limit) {
        try {
            String cacheKey = "blog:handleGetRecentBlogs:" + limit;

            // Check cache first
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for key: {}", cacheKey);
                if (cached instanceof List) {
                    List<?> list = (List<?>) cached;
                    return list.stream()
                            .map(item -> objectMapper.convertValue(item, BlogDto.class))
                            .collect(Collectors.toList());
                }
            }

            List<BlogDto> blogs = blogQueryRepository
                    .findRecentBlogs(PageRequest.of(0, limit))
                    .stream()
                    .map(blogMapper::toDto)
                    .collect(Collectors.toList());

            // Store in cache with 10 minutes TTL
            redisService.set(cacheKey, blogs, 10, TimeUnit.MINUTES);
            logger.debug("Cached result for key: {}", cacheKey);

            return blogs;
        } catch (Exception e) {
            logger.error("Error getting recent blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get recent blogs", 500);
        }
    }

    // ========== Public Response Methods ==========

    public Response createBlog(UUID userId, String dataJson, MultipartFile thumbnail) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 45 req/min for Create APIs
            String rateLimitKey = "blog:createBlog:" + userId.toString();
            if (!rateLimiterService.isAllowed(rateLimitKey, 45, 60)) {
                logger.warn("Rate limit exceeded for createBlog by userId: {}", userId);
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            CreateBlogRequest request = objectMapper.readValue(dataJson, CreateBlogRequest.class);

            BlogDto blog = handleCreateBlog(
                    userId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getCategory(),
                    request.getContent(),
                    request.getIsVisibility(),
                    thumbnail);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(201);
            response.setMessage("Blog created successfully");
            response.setBlog(blog);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in createBlog: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error creating blog: " + e.getMessage());
            return response;
        }
    }

    public Response getAllBlogs() {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            String rateLimitKey = "blog:getAllBlogs:all";
            if (!rateLimiterService.isAllowed(rateLimitKey, 90, 60)) {
                logger.warn("Rate limit exceeded for getAllBlogs");
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            List<BlogDto> blogs = handleGetAllBlogs();

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Blogs retrieved successfully");
            response.setBlogs(blogs);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in getAllBlogs: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error getting blogs: " + e.getMessage());
            return response;
        }
    }

    public Response getBlog(UUID blogId) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            String rateLimitKey = "blog:getBlog:" + blogId.toString();
            if (!rateLimiterService.isAllowed(rateLimitKey, 90, 60)) {
                logger.warn("Rate limit exceeded for getBlog blogId: {}", blogId);
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            BlogDto blog = handleGetBlogById(blogId);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Blog retrieved successfully");
            response.setBlog(blog);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in getBlog: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error getting blog: " + e.getMessage());
            return response;
        }
    }

    public Response getUserBlogs(UUID userId) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            String rateLimitKey = "blog:getUserBlogs:" + userId.toString();
            if (!rateLimiterService.isAllowed(rateLimitKey, 90, 60)) {
                logger.warn("Rate limit exceeded for getUserBlogs userId: {}", userId);
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            List<BlogDto> blogs = handleGetUserBlogs(userId);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("User blogs retrieved successfully");
            response.setBlogs(blogs);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in getUserBlogs: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error getting user blogs: " + e.getMessage());
            return response;
        }
    }

    public Response updateBlog(UUID blogId, String dataJson, MultipartFile thumbnail) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 45 req/min for Update APIs
            String rateLimitKey = "blog:updateBlog:" + blogId.toString();
            if (!rateLimiterService.isAllowed(rateLimitKey, 45, 60)) {
                logger.warn("Rate limit exceeded for updateBlog blogId: {}", blogId);
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            UpdateBlogRequest request = objectMapper.readValue(dataJson, UpdateBlogRequest.class);

            BlogDto blog = handleUpdateBlog(
                    blogId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getCategory(),
                    request.getContent(),
                    request.getIsVisibility(),
                    thumbnail);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Blog updated successfully");
            response.setBlog(blog);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in updateBlog: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error updating blog: " + e.getMessage());
            return response;
        }
    }

    public Response deleteBlog(UUID blogId) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 20 req/min for Delete APIs
            String rateLimitKey = "blog:deleteBlog:" + blogId.toString();
            if (!rateLimiterService.isAllowed(rateLimitKey, 20, 60)) {
                logger.warn("Rate limit exceeded for deleteBlog blogId: {}", blogId);
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            handleDeleteBlog(blogId);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Blog deleted successfully");
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in deleteBlog: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error deleting blog: " + e.getMessage());
            return response;
        }
    }

    public Response saveBlog(UUID userId, UUID blogId) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 45 req/min for Create APIs
            String rateLimitKey = "blog:saveBlog:" + userId.toString();
            if (!rateLimiterService.isAllowed(rateLimitKey, 45, 60)) {
                logger.warn("Rate limit exceeded for saveBlog userId: {}", userId);
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            BlogDto blog = handleSaveBlog(blogId, userId);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Blog saved successfully");
            response.setBlog(blog);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in savedBlog: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error saving blog: " + e.getMessage());
            return response;
        }
    }

    public Response unsaveBlog(UUID userId, UUID blogId) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 45 req/min for Delete APIs
            String rateLimitKey = "blog:unsaveBlog:" + userId.toString();
            if (!rateLimiterService.isAllowed(rateLimitKey, 45, 60)) {
                logger.warn("Rate limit exceeded for unsaveBlog userId: {}", userId);
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            boolean success = handleUnsaveBlog(blogId, userId);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Blog unsaved successfully");
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in unsaveBlog: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error unsaving blog: " + e.getMessage());
            return response;
        }
    }

    public Response getUserSavedBlogs(UUID userId) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            String rateLimitKey = "blog:getUserSavedBlogs:" + userId.toString();
            if (!rateLimiterService.isAllowed(rateLimitKey, 90, 60)) {
                logger.warn("Rate limit exceeded for getUserSavedBlogs userId: {}", userId);
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            List<BlogDto> blogs = handleGetUserSavedBlogs(userId);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Saved blogs retrieved successfully");
            response.setBlogs(blogs);
            return response;
        } catch (OurException e) {
            response.setStatusCode(e.getStatusCode());
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            logger.error("Error in getSavedBlogs: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error getting saved blogs: " + e.getMessage());
            return response;
        }
    }

    public Response getTotalBlogs() {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            String rateLimitKey = "blog:getTotalBlogs:all";
            if (!rateLimiterService.isAllowed(rateLimitKey, 90, 60)) {
                logger.warn("Rate limit exceeded for getTotalBlogs");
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            long total = handleGetTotalBlogs();

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Total blogs retrieved successfully");
            response.setAdditionalData(Map.of("total", total));
            return response;
        } catch (Exception e) {
            logger.error("Error in getTotalBlogs: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to get total blogs");
            return response;
        }
    }

    public Response getBlogsByVisibility(boolean isVisibility) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            String rateLimitKey = "blog:getBlogsByVisibility:" + isVisibility;
            if (!rateLimiterService.isAllowed(rateLimitKey, 90, 60)) {
                logger.warn("Rate limit exceeded for getBlogsByVisibility");
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            long count = blogQueryRepository.countByVisibility(isVisibility);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("get Blogs count by visibility retrieved successfully");
            response.setAdditionalData(Map.of("count", count));
            return response;
        } catch (Exception e) {
            logger.error("Error in getBlogsCreatedInRange: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to get blogs in range");
            return response;
        }
    }

    public Response getBlogsCreatedInRange(String startDate, String endDate) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            String rateLimitKey = "blog:getBlogsCreatedInRange:" + startDate + ":" + endDate;
            if (!rateLimiterService.isAllowed(rateLimitKey, 90, 60)) {
                logger.warn("Rate limit exceeded for getBlogsCreatedInRange");
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            Instant start = Instant.parse(startDate);
            Instant end = Instant.parse(endDate);
            long count = handleGetBlogsCountCreatedInRange(start, end);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Blogs count in range retrieved successfully");
            response.setAdditionalData(Map.of("count", count));
            return response;
        } catch (Exception e) {
            logger.error("Error in getBlogsCreatedInRange: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to get blogs in range");
            return response;
        }
    }

    public Response getRecentBlogs(int limit) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            String rateLimitKey = "blog:getRecentBlogs:" + limit;
            if (!rateLimiterService.isAllowed(rateLimitKey, 90, 60)) {
                logger.warn("Rate limit exceeded for getRecentBlogs");
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            List<BlogDto> blogs = handleGetRecentBlogs(limit);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Recent blogs retrieved successfully");
            response.setBlogs(blogs);
            return response;
        } catch (Exception e) {
            logger.error("Error in getRecentBlogs: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Failed to get recent blogs");
            return response;
        }
    }
}