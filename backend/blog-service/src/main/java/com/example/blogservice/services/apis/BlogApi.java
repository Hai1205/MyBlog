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

    public BlogApi(
            BlogQueryRepository blogQueryRepository,
            BlogCommandRepository blogCommandRepository,
            SavedBlogCommandRepository savedBlogCommandRepository,
            SavedBlogQueryRepository savedBlogQueryRepository,
            BlogMapper blogMapper,
            CloudinaryService cloudinaryService,
            UserFeignClient userFeignClient) {
        this.blogQueryRepository = blogQueryRepository;
        this.blogCommandRepository = blogCommandRepository;
        this.savedBlogCommandRepository = savedBlogCommandRepository;
        this.savedBlogQueryRepository = savedBlogQueryRepository;
        this.blogMapper = blogMapper;
        this.cloudinaryService = cloudinaryService;
        this.userFeignClient = userFeignClient;
        this.objectMapper = new ObjectMapper();
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
            MultipartFile thumbnail) {
        try {
            logger.info("Creating blog for userId={} title='{}'", userId, title);

            validateUser(userId);

            UUID blogId = UUID.randomUUID();
            Instant now = Instant.now();

            String imageUrl = null;
            String imagePublicId = null;

            if (thumbnail != null && !thumbnail.isEmpty()) {
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(thumbnail);
                if (uploadResult.containsKey("error")) {
                    throw new OurException("Failed to upload thumbnail: " + uploadResult.get("error"), 500);
                }
                imageUrl = (String) uploadResult.get("url");
                imagePublicId = (String) uploadResult.get("publicId");
            }

            blogCommandRepository.insertBlog(
                    blogId,
                    userId,
                    title,
                    description,
                    category,
                    imageUrl,
                    imagePublicId,
                    content,
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
            return blogQueryRepository
                    .findAllBlogs()
                    .stream()
                    .map(blogMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting all blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get blogs", 500);
        }
    }

    @Transactional(readOnly = true)
    public BlogDto handleGetBlogById(UUID blogId) {
        try {
            logger.debug("Fetching blog by id={}", blogId);
            Blog blog = blogQueryRepository.findBlogById(blogId)
                    .orElseThrow(() -> new OurException("Blog not found", 404));
            return blogMapper.toDto(blog);
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

            return blogQueryRepository
                    .findBlogsByUserId(userId)
                    .stream()
                    .map(blogMapper::toDto)
                    .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                    .collect(Collectors.toList());
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
            MultipartFile thumbnail) {
        try {
            logger.info("Updating blog id={}", blogId);

            Blog existingBlog = blogQueryRepository.findBlogById(blogId)
                    .orElseThrow(() -> new OurException("Blog not found", 404));

            Instant now = Instant.now();

            String imageUrl = existingBlog.getImageUrl();
            String imagePublicId = existingBlog.getImagePublicId();

            if (thumbnail != null && !thumbnail.isEmpty()) {
                // Delete old image if exists
                if (imagePublicId != null && !imagePublicId.isEmpty()) {
                    cloudinaryService.deleteImage(imagePublicId);
                }

                // Upload new image
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(thumbnail);
                if (uploadResult.containsKey("error")) {
                    throw new OurException("Failed to upload thumbnail: " + uploadResult.get("error"), 500);
                }
                imageUrl = (String) uploadResult.get("url");
                imagePublicId = (String) uploadResult.get("publicId");
            }

            blogCommandRepository.updateBlog(
                    blogId,
                    title != null ? title : existingBlog.getTitle(),
                    description != null ? description : existingBlog.getDescription(),
                    category != null ? category : existingBlog.getCategory().name(),
                    content != null ? content : existingBlog.getContent(),
                    imageUrl,
                    imagePublicId,
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

            // Delete image from Cloudinary if exists
            if (blog.getImagePublicId() != null && !blog.getImagePublicId().isEmpty()) {
                cloudinaryService.deleteImage(blog.getImagePublicId());
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

            List<Blog> savedBlogs = savedBlogQueryRepository.findBlogsByUserSaved(userId);
            return savedBlogs.stream()
                    .map(blogMapper::toDto)
                    .collect(Collectors.toList());
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting saved blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get saved blogs", 500);
        }
    }

    // ========== Statistics Methods ==========

    public long handleGetTotalBlogs() {
        try {
            return blogQueryRepository.countTotalBlogs();
        } catch (Exception e) {
            logger.error("Error getting total blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get total blogs", 500);
        }
    }

    public List<BlogDto> handleGetBlogsCreatedInRange(Instant startDate, Instant endDate) {
        try {
            return blogQueryRepository
                    .findBlogsCreatedBetween(startDate, endDate)
                    .stream()
                    .map(blogMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting blogs in range: {}", e.getMessage(), e);
            throw new OurException("Failed to get blogs in range", 500);
        }
    }

    public long handleGetBlogsCountCreatedInRange(Instant startDate, Instant endDate) {
        try {
            return blogQueryRepository.countBlogsCreatedBetween(startDate, endDate);
        } catch (Exception e) {
            logger.error("Error counting blogs in range: {}", e.getMessage(), e);
            throw new OurException("Failed to count blogs in range", 500);
        }
    }

    public List<BlogDto> handleGetRecentBlogs(int limit) {
        try {
            return blogQueryRepository
                    .findRecentBlogs(PageRequest.of(0, limit))
                    .stream()
                    .map(blogMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting recent blogs: {}", e.getMessage(), e);
            throw new OurException("Failed to get recent blogs", 500);
        }
    }

    // ========== Public Response Methods ==========

    public Response createBlog(UUID userId, String dataJson, MultipartFile thumbnail) {
        logger.info("Creating new blog");
        Response response = new Response();

        try {
            CreateBlogRequest request = objectMapper.readValue(dataJson, CreateBlogRequest.class);

            BlogDto blog = handleCreateBlog(
                    userId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getCategory(),
                    request.getContent(),
                    thumbnail);

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
            List<BlogDto> blogs = handleGetAllBlogs();

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
            BlogDto blog = handleGetBlogById(blogId);

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
            List<BlogDto> blogs = handleGetUserBlogs(userId);

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

    public Response updateBlog(String dataJson, MultipartFile thumbnail) {
        Response response = new Response();

        try {
            UpdateBlogRequest request = objectMapper.readValue(dataJson, UpdateBlogRequest.class);

            BlogDto blog = handleUpdateBlog(
                    UUID.fromString(request.getBlogId()),
                    request.getTitle(),
                    request.getDescription(),
                    request.getCategory(),
                    request.getContent(),
                    thumbnail);

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
            handleDeleteBlog(blogId);

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
            BlogDto blog = handleSaveBlog(blogId, userId);

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

    public Response getUserSavedBlogs(UUID userId) {
        Response response = new Response();

        try {
            List<BlogDto> blogs = handleGetUserSavedBlogs(userId);

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
            long total = handleGetTotalBlogs();

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

    public Response getBlogsCreatedInRange(String startDate, String endDate) {
        Response response = new Response();

        try {
            Instant start = Instant.parse(startDate);
            Instant end = Instant.parse(endDate);
            long count = handleGetBlogsCountCreatedInRange(start, end);

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
            List<BlogDto> blogs = handleGetRecentBlogs(limit);

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