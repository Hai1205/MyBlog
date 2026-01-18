package com.example.userservice.services.apis;

import com.example.userservice.dtos.UserDto;
import com.example.userservice.dtos.requests.auth.ChangePasswordRequest;
import com.example.userservice.dtos.requests.auth.ForgotPasswordRequest;
import com.example.userservice.dtos.requests.user.CreateUserRequest;
import com.example.userservice.dtos.requests.user.UpdateUserRequest;
import com.example.userservice.dtos.response.Response;
import com.example.userservice.entities.*;
import com.example.userservice.entities.User.UserRole;
import com.example.userservice.entities.User.UserStatus;
import com.example.userservice.exceptions.OurException;
import com.example.userservice.mappers.UserMapper;
import com.example.userservice.repositories.*;
import com.example.cloudinarycommon.CloudinaryService;
import com.example.rediscommon.services.RedisService;
import com.example.rediscommon.services.RateLimiterService;
import com.example.securitycommon.utils.SecurityUtils;
import com.example.securitycommon.models.AuthenticatedUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserApi extends BaseApi {

    // private final SimpleUserRepository simpleUserRepository;
    private final UserQueryRepository userQueryRepository;
    private final UserCommandRepository userCommandRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
    private final SecureRandom random = new SecureRandom();
    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final RateLimiterService rateLimiterService;

    @Value("${PRIVATE_CHARS}")
    private String privateChars;

    @Value("${PASSWORD_LENGTH}")
    private int passwordLength;

    public UserApi(
            SimpleUserRepository simpleUserRepository,
            UserQueryRepository userQueryRepository,
            UserCommandRepository userCommandRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            CloudinaryService cloudinaryService,
            RedisService redisService,
            RateLimiterService rateLimiterService) {
        // this.simpleUserRepository = simpleUserRepository;
        this.userQueryRepository = userQueryRepository;
        this.userCommandRepository = userCommandRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.cloudinaryService = cloudinaryService;
        this.objectMapper = new ObjectMapper();
        this.redisService = redisService;
        this.rateLimiterService = rateLimiterService;
    }

    public UserDto handleCreateUser(String username,
            String email,
            String password,
            String fullname,
            String location,
            String birth,
            String summary,
            String role,
            String status,
            String instagram,
            String linkedin,
            String facebook,
            MultipartFile avatar) {
        try {
            System.out.println("Stored password hash: " + password);
            logger.info("Creating user with email: {}", email);

            // Basic validation
            if (email == null || email.isEmpty()) {
                logger.warn("Attempted to create user with empty email");
                throw new OurException("Email is required", 400);
            }

            if (userQueryRepository.existsByEmail(email)) {
                logger.warn("Attempted to create user with existing email: {}", email);
                throw new OurException("Email already exists", 400);
            }

            // Ensure username fallback to email prefix when not provided
            if (username == null || username.isEmpty()) {
                try {
                    username = email.split("@")[0];
                } catch (Exception ex) {
                    username = "user" + UUID.randomUUID().toString().substring(0, 8);
                }
            }

            // Check if username already exists
            if (userQueryRepository.existsByUsername(username)) {
                logger.warn("Attempted to create user with existing username: {}", username);
                throw new OurException("Username already exists", 400);
            }

            // If no password provided, generate a secure random one
            if (password == null || password.isEmpty()) {
                password = handleGenerateRandomPassword();
                logger.info("No password provided for {} - generated a random password", email);
            }

            User user = new User(
                    username,
                    email,
                    fullname);

            if (birth != null && !birth.isEmpty()) {
                user.setBirth(birth);
            }
            if (summary != null && !summary.isEmpty()) {
                user.setSummary(summary);
            }

            if (avatar != null && !avatar.isEmpty()) {
                logger.debug("Uploading avatar for user: {}", email);
                var uploadResult = cloudinaryService.uploadImage(avatar);
                if (uploadResult.containsKey("error")) {
                    logger.error("Failed to upload avatar for user {}: {}", email, uploadResult.get("error"));
                    throw new RuntimeException("Failed to upload avatar: " + uploadResult.get("error"));
                }

                user.setAvatarUrl((String) uploadResult.get("url"));
                user.setAvatarPublicId((String) uploadResult.get("publicId"));
            }

            user.setPassword(passwordEncoder.encode(password));

            if (role != null && !role.isEmpty()) {
                try {
                    user.setRole(UserRole.valueOf(role));
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid role value: {}. Valid values are: user, admin", role);
                    throw new OurException("Invalid role value: " + role + ". Valid values are: user, admin", 400);
                }
            }
            if (status != null && !status.isEmpty()) {
                try {
                    user.setStatus(UserStatus.valueOf(status));
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid status value: {}. Valid values are: active, banned, pending", status);
                    throw new OurException(
                            "Invalid status value: " + status + ". Valid values are: active, banned, pending", 400);
                }
            }

            // Insert user using command repository
            UUID userId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            userCommandRepository.insertUser(
                    userId,
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getFullname(),
                    user.getBirth(),
                    user.getSummary(),
                    user.getAvatarUrl(),
                    user.getAvatarPublicId(),
                    user.getRole().name(),
                    user.getStatus().name(),
                    user.getFacebook(),
                    user.getLinkedin(),
                    user.getInstagram(),
                    now,
                    now);

            // Fetch created user to return
            User savedUser = userQueryRepository.findUserById(userId)
                    .orElseThrow(() -> new OurException("Failed to create user", 500));
            logger.info("User created successfully with ID: {}", savedUser.getId());
            return userMapper.toDto(savedUser);
        } catch (OurException e) {
            logger.error("Error in handleCreateUser: {}", e.getMessage(), e);
            throw e;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Database constraint violation in handleCreateUser: {}", e.getMessage());
            String message = "Failed to create user";
            if (e.getMessage().contains("UK_r43af9ap4edm43mmtq01oddj6") || e.getMessage().contains("username")) {
                message = "Username already exists";
            } else if (e.getMessage().contains("email")) {
                message = "Email already exists";
            }
            throw new OurException(message, 400);
        } catch (Exception e) {
            logger.error("Unexpected error in handleCreateUser: {}", e.getMessage(), e);
            throw new OurException("Failed to create user", 500);
        }
    }

    public UserDto handleActivateUser(String email) {
        try {
            logger.info("Activating user with email: {}", email);
            User user = userQueryRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update status using command repository
            userCommandRepository.updateUserStatusByEmail(email, UserStatus.active, LocalDateTime.now());

            // Fetch updated user
            user = userQueryRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            logger.info("User activated successfully: {}", email);
            return userMapper.toDto(user);
        } catch (OurException e) {
            logger.error("Error in handleActivateUser: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in handleActivateUser: {}", e.getMessage(), e);
            throw new OurException("Failed to activate user", 500);
        }
    }

    private boolean handleIsValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(regex);
    }

    public UserDto handleAuthenticateUser(String identifier, String currentPassword) {
        try {
            logger.debug("Authenticating user: {}", identifier);

            User user = null;
            boolean isEmailValid = handleIsValidEmail(identifier);
            if (isEmailValid) {
                user = userQueryRepository.findByEmail(identifier)
                        .orElseThrow(() -> new OurException("User not found", 404));
            } else {
                user = userQueryRepository.findByUsername(identifier)
                        .orElseThrow(() -> new OurException("User not found", 404));
            }

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                logger.warn("Invalid password attempt for user: {}", identifier);
                throw new OurException("Invalid credentials", 400);
            }

            logger.debug("User authenticated successfully: {}", identifier);
            return userMapper.toDto(user);
        } catch (OurException e) {
            logger.error("Error in handleAuthenticateUser: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in handleAuthenticateUser: {}", e.getMessage(), e);
            throw new OurException("Failed to authenticate user", 500);
        }
    }

    public String handleGenerateRandomPassword() {
        try {
            StringBuilder password = new StringBuilder(passwordLength);
            for (int i = 0; i < passwordLength; i++) {
                int index = random.nextInt(privateChars.length());
                password.append(privateChars.charAt(index));
            }
            return password.toString();
        } catch (Exception e) {
            logger.error("Error in handleGenerateRandomPassword: {}", e.getMessage(), e);
            throw new OurException("Failed to generate random password", 500);
        }
    }

    public String handleResetPasswordUser(String email) {
        try {
            userQueryRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found", 404));

            String newPassword = handleGenerateRandomPassword();
            String encodedPassword = passwordEncoder.encode(newPassword);
            userCommandRepository.updateUserPasswordByEmail(email, encodedPassword, LocalDateTime.now());

            return newPassword;
        } catch (OurException e) {
            logger.error("Error in handleResetPasswordUser: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in handleResetPasswordUser: {}", e.getMessage(), e);
            throw new OurException("Failed to reset password", 500);
        }
    }

    public UserDto handleForgotPasswordUser(String email, String newPassword) {
        try {
            User user = userQueryRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found", 404));

            String encodedPassword = passwordEncoder.encode(newPassword);
            userCommandRepository.updateUserPasswordByEmail(email, encodedPassword, LocalDateTime.now());

            // Fetch updated user
            user = userQueryRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found", 404));
            return userMapper.toDto(user);
        } catch (OurException e) {
            logger.error("Error in handleForgotPasswordUser: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in handleForgotPasswordUser: {}", e.getMessage(), e);
            throw new OurException("Failed to update password", 500);
        }
    }

    public UserDto handleChangePasswordUser(String email, String currentPassword, String newPassword) {
        try {
            UserDto userDto = handleAuthenticateUser(email, currentPassword);
            System.out.println("Authenticated user for password change: " + userDto.getEmail());

            String encodedPassword = passwordEncoder.encode(newPassword);
            userCommandRepository.updateUserPasswordByEmail(email, encodedPassword, LocalDateTime.now());

            return userDto;
        } catch (OurException e) {
            logger.error("Error in handleChangePasswordUser: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in handleChangePasswordUser: {}", e.getMessage(), e);
            throw new OurException("Failed to change password", 500);
        }
    }

    public Response createUser(String dataJson) {
        long startTime = System.currentTimeMillis();
        logger.info("Creating new user");
        Response response = new Response();

        try {
            // Rate limiting: 45 req/min for Create/Update APIs
            if (!rateLimiterService.isAllowed("user:createUser", 45, 60)) {
                logger.warn("Rate limit exceeded for createUser");
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            CreateUserRequest request = objectMapper.readValue(dataJson, CreateUserRequest.class);
            String username = request.getUsername();
            String email = request.getEmail();
            String password = request.getPassword();
            String fullname = request.getFullname();
            String location = request.getLocation();
            String birth = request.getBirth();
            String summary = request.getSummary();
            String role = request.getRole();
            String status = request.getStatus();
            MultipartFile avatar = request.getAvatar();
            String instagram = request.getInstagram();
            String linkedin = request.getLinkedin();
            String facebook = request.getFacebook();

            UserDto savedUserDto = handleCreateUser(username, email, password, fullname, location, birth,
                    summary, role, status, instagram, linkedin, facebook, avatar);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(201);
            response.setMessage("User created successfully");
            response.setUser(savedUserDto);
            logger.info("User creation completed successfully for email: {}", email);
            return response;
        } catch (OurException e) {
            logger.error("User creation failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("User creation failed with unexpected error", e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public List<UserDto> handleGetAllUsers() {
        try {
            String cacheKey = "user:handleGetAllUsers:all";

            // Check cache first
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for key: {}", cacheKey);
                return (List<UserDto>) cached;
            }

            List<UserDto> users = userQueryRepository.findAllUsers(Pageable.unpaged()).stream()
                    .map(userMapper::toDto)
                    .collect(Collectors.toList());

            // Store in cache with 10 minutes TTL
            redisService.set(cacheKey, users, 10, TimeUnit.MINUTES);
            logger.debug("Cached result for key: {}", cacheKey);

            return users;
        } catch (Exception e) {
            logger.error("Error in handleGetAllUsers: {}", e.getMessage(), e);
            throw new OurException("Failed to retrieve users", 500);
        }
    }

    public Response getAllUsers() {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            if (!rateLimiterService.isAllowed("user:getAllUsers", 90, 60)) {
                logger.warn("Rate limit exceeded for getAllUsers");
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            List<UserDto> userDtos = handleGetAllUsers();

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setMessage("Users retrieved successfully");
            response.setUsers(userDtos);
            return response;
        } catch (OurException e) {
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public UserDto handleGetUserById(UUID userId) {
        try {
            String cacheKey = "user:handleGetUserById:" + userId.toString();

            // Check cache first
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for key: {}", cacheKey);
                return (UserDto) cached;
            }

            User user = userQueryRepository.findUserById(userId)
                    .orElseThrow(() -> new OurException("User not found", 404));
            UserDto userDto = userMapper.toDto(user);

            // Store in cache with 10 minutes TTL
            redisService.set(cacheKey, userDto, 10, TimeUnit.MINUTES);
            logger.debug("Cached result for key: {}", cacheKey);

            return userDto;
        } catch (OurException e) {
            logger.error("Error in handleGetUserById: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in handleGetUserById: {}", e.getMessage(), e);
            throw new OurException("Failed to retrieve user", 500);
        }
    }

    public Response getUserById(UUID userId) {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            if (!rateLimiterService.isAllowed("user:getUserById:" + userId, 90, 60)) {
                logger.warn("Rate limit exceeded for getUserById: {}", userId);
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            UserDto userDto = handleGetUserById(userId);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setMessage("User retrieved successfully");
            response.setUser(userDto);
            return response;
        } catch (OurException e) {
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public UserDto handleUpdateUser(UUID userId, String fullname, String location, String birth,
            String summary, String role, String status, String instagram, String linkedin, String facebook,
            MultipartFile avatar) {
        try {
            logger.info("Updating user with ID: {}", userId);

            User existingUser = userQueryRepository.findUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AuthenticatedUser currentUser = SecurityUtils.getCurrentUser();
            boolean privilegedChangeAllowed = currentUser == null || currentUser.hasRole("ADMIN");

            // Handle avatar upload
            if (avatar != null && !avatar.isEmpty()) {
                logger.debug("Updating avatar for user: {}", userId);
                // Delete old avatar if exists
                String oldAvatarPublicId = existingUser.getAvatarPublicId();
                if (oldAvatarPublicId != null && !oldAvatarPublicId.isEmpty()) {
                    cloudinaryService.deleteImage(oldAvatarPublicId);
                }

                // Upload new avatar
                var uploadResult = cloudinaryService.uploadImage(avatar);
                if (uploadResult.containsKey("error")) {
                    logger.error("Failed to upload avatar for user {}: {}", userId, uploadResult.get("error"));
                    throw new RuntimeException("Failed to upload avatar: " + uploadResult.get("error"));
                }

                existingUser.setAvatarUrl((String) uploadResult.get("url"));
                existingUser.setAvatarPublicId((String) uploadResult.get("publicId"));
            }

            // Update other fields
            if (fullname != null && !fullname.isEmpty() && !fullname.equals(existingUser.getFullname())) {
                existingUser.setFullname(fullname);
            }

            if (instagram != null && !instagram.equals(existingUser.getInstagram())) {
                existingUser.setInstagram(instagram);
            }

            if (linkedin != null && !linkedin.equals(existingUser.getLinkedin())) {
                existingUser.setLinkedin(linkedin);
            }

            if (facebook != null && !facebook.equals(existingUser.getFacebook())) {
                existingUser.setFacebook(facebook);
            }

            if (birth != null && !birth.equals(existingUser.getBirth())) {
                existingUser.setBirth(birth);
            }

            if (summary != null && !summary.equals(existingUser.getSummary())) {
                existingUser.setSummary(summary);
            }

            if (role != null && !role.isEmpty()) {
                if (!privilegedChangeAllowed) {
                    logger.warn("Unauthorized attempt to change role for user {} by user: {}",
                            userId, currentUser != null ? currentUser.getEmail() : "unknown");
                    throw new OurException("Forbidden", 403);
                }
                existingUser.setRole(UserRole.valueOf(role));
            }

            if (status != null && !status.isEmpty()) {
                if (!privilegedChangeAllowed) {
                    logger.warn("Unauthorized attempt to change status for user {} by user: {}",
                            userId, currentUser != null ? currentUser.getEmail() : "unknown");
                    throw new OurException("Forbidden", 403);
                }
                existingUser.setStatus(UserStatus.valueOf(status));
            }

            // Update user using command repository
            userCommandRepository.updateUserAllFields(
                    userId,
                    existingUser.getFullname(),
                    existingUser.getBirth(),
                    existingUser.getSummary(),
                    existingUser.getAvatarUrl(),
                    existingUser.getAvatarPublicId(),
                    existingUser.getRole(),
                    existingUser.getStatus(),
                    existingUser.getFacebook(),
                    existingUser.getLinkedin(),
                    existingUser.getInstagram(),
                    LocalDateTime.now());

            // Fetch updated user
            User updatedUser = userQueryRepository.findUserById(userId)
                    .orElseThrow(() -> new OurException("User not found", 404));
            logger.info("User updated successfully: {}", userId);
            return userMapper.toDto(updatedUser);
        } catch (OurException e) {
            logger.error("Error in handleUpdateUser: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in handleUpdateUser: {}", e.getMessage(), e);
            throw new OurException("Failed to update user", 500);
        }
    }

    public Response updateUser(UUID userId, String dataJson, MultipartFile avatar) {
        long startTime = System.currentTimeMillis();
        logger.info("Updating user: {}", userId);
        Response response = new Response();

        try {
            // Rate limiting: 45 req/min for Create/Update APIs
            if (!rateLimiterService.isAllowed("user:updateUser", 45, 60)) {
                logger.warn("Rate limit exceeded for updateUser");
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            UpdateUserRequest request = objectMapper.readValue(dataJson, UpdateUserRequest.class);
            String fullname = request.getFullname();
            String location = request.getLocation();
            String birth = request.getBirth();
            String summary = request.getSummary();
            String role = request.getRole();
            String status = request.getStatus();
            String instagram = request.getInstagram();
            String linkedin = request.getLinkedin();
            String facebook = request.getFacebook();

            UserDto updatedUserDto = handleUpdateUser(userId, fullname, location, birth, summary, role, status,
                    instagram, linkedin, facebook, avatar);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setMessage("User updated successfully");
            response.setUser(updatedUserDto);
            logger.info("User update completed successfully: {}", userId);
            return response;
        } catch (OurException e) {
            logger.error("User update failed with OurException for user {}: {}", userId, e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("User update failed with unexpected error for user {}", userId, e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public boolean handleDeleteUser(UUID userId) {
        try {
            logger.info("Deleting user with ID: {}", userId);
            UserDto user = handleGetUserById(userId);

            String avatarPublicId = user.getAvatarPublicId();
            if (avatarPublicId != null && !avatarPublicId.isEmpty()) {
                logger.debug("Deleting avatar for user: {}", userId);
                cloudinaryService.deleteImage(avatarPublicId);
            }

            userCommandRepository.deleteUserById(userId);
            logger.info("User deleted successfully: {}", userId);
            return true;
        } catch (OurException e) {
            logger.error("Error in handleDeleteUser: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in handleDeleteUser: {}", e.getMessage(), e);
            throw new OurException("Failed to delete user", 500);
        }
    }

    public Response deleteUser(UUID userId) {
        long startTime = System.currentTimeMillis();
        logger.info("Deleting user: {}", userId);
        Response response = new Response();

        try {
            // Rate limiting: 20 req/min for Delete APIs
            if (!rateLimiterService.isAllowed("user:deleteUser", 20, 60)) {
                logger.warn("Rate limit exceeded for deleteUser");
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            handleDeleteUser(userId);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setMessage("User deleted successfully");
            logger.info("User deletion completed successfully: {}", userId);
            return response;
        } catch (OurException e) {
            logger.error("User deletion failed with OurException for user {}: {}", userId, e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("User deletion failed with unexpected error for user {}", userId, e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public UserDto handleFindByEmail(String email) {
        try {
            return userQueryRepository.findByEmail(email)
                    .map(userMapper::toDto)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error in handleFindByEmail: {}", e.getMessage(), e);
            throw new OurException("Failed to find user by email", 500);
        }
    }

    public UserDto handleFindByUsername(String username) {
        try {
            return userQueryRepository.findByUsername(username)
                    .map(userMapper::toDto)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error in handleFindByUsername: {}", e.getMessage(), e);
            throw new OurException("Failed to find user by username", 500);
        }
    }

    public UserDto handleFindById(UUID userId) {
        try {
            return userQueryRepository.findUserById(userId)
                    .map(userMapper::toDto)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error in handleFindById: {}", e.getMessage(), e);
            throw new OurException("Failed to find user by ID", 500);
        }
    }

    public UserDto handleFindByIdentifier(String identifier) {
        try {
            boolean isEmailValid = handleIsValidEmail(identifier);
            UserDto user = null;
            if (isEmailValid) {
                user = handleFindByEmail(identifier);
            } else {
                user = handleFindByUsername(identifier);
            }

            return user;
        } catch (Exception e) {
            logger.error("Error in handleFindByIdentifier: {}", e.getMessage(), e);
            throw new OurException("Failed to find user by identifier", 500);
        }
    }

    // ========== Statistics Methods ==========

    /**
     * Get total number of users
     */
    public long handleGetTotalUsers() {
        try {
            return userQueryRepository.countTotalUsers();
        } catch (Exception e) {
            logger.error("Error in handleGetTotalUsers: {}", e.getMessage(), e);
            throw new OurException("Failed to get total users", 500);
        }
    }

    /**
     * Get users count by status
     */
    public long handleGetUsersByStatus(String status) {
        try {
            return userQueryRepository.findAllUsers(Pageable.unpaged()).stream()
                    .filter(user -> user.getStatus().toString().equalsIgnoreCase(status))
                    .count();
        } catch (Exception e) {
            logger.error("Error in handleGetUsersByStatus: {}", e.getMessage(), e);
            throw new OurException("Failed to get users by status", 500);
        }
    }

    /**
     * Get users created in date range
     */
    public long handleGetUsersCreatedInRange(String startDate, String endDate) {
        try {
            // Parse ISO 8601 format with timezone (e.g., "2026-01-01T00:00:00Z")
            LocalDateTime start = Instant.parse(startDate).atZone(ZoneOffset.UTC).toLocalDateTime();
            LocalDateTime end = Instant.parse(endDate).atZone(ZoneOffset.UTC).toLocalDateTime();
            return userQueryRepository.countUsersCreatedBetween(start, end);
        } catch (Exception e) {
            logger.error("Error in handleGetUsersCreatedInRange: {}", e.getMessage(), e);
            throw new OurException("Failed to get users created in range", 500);
        }
    }

    /**
     * Get recent users
     */
    public List<UserDto> handleGetRecentUsers(int limit) {
        try {
            // Note: User entity doesn't have createdAt field in current implementation
            // This returns first N users instead
            return userQueryRepository.findAllUsers(Pageable.unpaged()).stream()
                    .limit(limit)
                    .map(userMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error in handleGetRecentUsers: {}", e.getMessage(), e);
            throw new OurException("Failed to get recent users", 500);
        }
    }

    public Response getUserStats() {
        long startTime = System.currentTimeMillis();
        try {
            // Rate limiting: 90 req/min for GET APIs
            if (!rateLimiterService.isAllowed("user:getUserStats", 90, 60)) {
                logger.warn("Rate limit exceeded for getUserStats");
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            long totalUsers = handleGetTotalUsers();
            long activeUsers = handleGetUsersByStatus("active");
            long pendingUsers = handleGetUsersByStatus("pending");
            long bannedUsers = handleGetUsersByStatus("banned");

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            Response response = new Response(200, "User statistics retrieved successfully");
            response.setAdditionalData(Map.of(
                    "totalUsers", totalUsers,
                    "activeUsers", activeUsers,
                    "pendingUsers", pendingUsers,
                    "bannedUsers", bannedUsers));
            return response;
        } catch (Exception e) {
            logger.error("Error in getUserStats: {}", e.getMessage(), e);
            return new Response(500, "Failed to get user statistics");
        }
    }

    public Response getUsersByStatus(String status) {
        long startTime = System.currentTimeMillis();
        try {
            // Rate limiting: 90 req/min for GET APIs
            if (!rateLimiterService.isAllowed("user:getUsersByStatus", 90, 60)) {
                logger.warn("Rate limit exceeded for getUsersByStatus");
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            long count = handleGetUsersByStatus(status);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            Response response = new Response(200, "Users by status retrieved successfully");
            response.setAdditionalData(Map.of("count", count));
            return response;
        } catch (Exception e) {
            logger.error("Error in getUsersByStatus: {}", e.getMessage(), e);
            return new Response(500, "Failed to get users by status");
        }
    }

    public Response getUsersCreatedInRange(String startDate, String endDate) {
        long startTime = System.currentTimeMillis();
        try {
            // Rate limiting: 90 req/min for GET APIs
            if (!rateLimiterService.isAllowed("user:getUsersCreatedInRange", 90, 60)) {
                logger.warn("Rate limit exceeded for getUsersCreatedInRange");
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            long count = handleGetUsersCreatedInRange(startDate, endDate);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            Response response = new Response(200, "Users created in range retrieved successfully");
            response.setAdditionalData(Map.of("count", count));
            return response;
        } catch (Exception e) {
            logger.error("Error in getUsersCreatedInRange: {}", e.getMessage(), e);
            return new Response(500, "Failed to get users created in range");
        }
    }

    public Response getRecentUsers(int limit) {
        long startTime = System.currentTimeMillis();
        try {
            // Rate limiting: 90 req/min for GET APIs
            if (!rateLimiterService.isAllowed("user:getRecentUsers", 90, 60)) {
                logger.warn("Rate limit exceeded for getRecentUsers");
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            List<UserDto> recentUsers = handleGetRecentUsers(limit);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            Response response = new Response(200, "Recent users retrieved successfully");
            response.setUsers(recentUsers);
            return response;
        } catch (Exception e) {
            logger.error("Error in getRecentUsers: {}", e.getMessage(), e);
            return new Response(500, "Failed to get recent users");
        }
    }

    public Response authenticateUser(String identifier, String password) {
        long startTime = System.currentTimeMillis();
        logger.info("Authenticating user");
        Response response = new Response();

        try {
            // Rate limiting: 7 req/min for authentication (similar to auth service)
            if (!rateLimiterService.isAllowed("user:authenticateUser:" + identifier, 7, 60)) {
                logger.warn("Rate limit exceeded for authenticateUser: {}", identifier);
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            UserDto userDto = handleAuthenticateUser(identifier, password);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("User authenticated successfully");
            response.setUser(userDto);
            logger.info("User authentication completed successfully for identifier: {}", identifier);
            return response;
        } catch (OurException e) {
            logger.error("User authentication failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("User authentication failed with unexpected error", e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response findUserByIdentifier(String identifier) {
        long startTime = System.currentTimeMillis();
        logger.info("Finding user by identifier: {}", identifier);
        Response response = new Response();

        try {
            // Rate limiting: 90 req/min for GET APIs
            if (!rateLimiterService.isAllowed("user:findUserByIdentifier", 90, 60)) {
                logger.warn("Rate limit exceeded for findUserByIdentifier");
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            UserDto userDto = handleFindByIdentifier(identifier);

            if (userDto == null) {
                return buildErrorResponse(404, "User not found");
            }

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("User found successfully");
            response.setUser(userDto);
            logger.info("User found successfully for identifier: {}", identifier);
            return response;
        } catch (OurException e) {
            logger.error("Find user by identifier failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Find user by identifier failed with unexpected error", e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response activateUser(String email) {
        long startTime = System.currentTimeMillis();
        logger.info("Activating user: {}", email);
        Response response = new Response();

        try {
            // Rate limiting: 45 req/min for Update APIs
            if (!rateLimiterService.isAllowed("user:activateUser:" + email, 45, 60)) {
                logger.warn("Rate limit exceeded for activateUser: {}", email);
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            UserDto userDto = handleActivateUser(email);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("User activated successfully");
            response.setUser(userDto);
            logger.info("User activation completed successfully for email: {}", email);
            return response;
        } catch (OurException e) {
            logger.error("User activation failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("User activation failed with unexpected error", e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response changePassword(String identifier, String dataJson) {
        long startTime = System.currentTimeMillis();
        logger.info("Changing password for identifier: {}", identifier);
        Response response = new Response();

        try {
            // Rate limiting: 7 req/min for password operations (similar to auth service)
            if (!rateLimiterService.isAllowed("user:changePassword:" + identifier, 7, 60)) {
                logger.warn("Rate limit exceeded for changePassword: {}", identifier);
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            ChangePasswordRequest request = objectMapper.readValue(dataJson, ChangePasswordRequest.class);
            String currentPassword = request.getCurrentPassword();
            String newPassword = request.getNewPassword();

            UserDto userDto = handleChangePasswordUser(identifier, currentPassword, newPassword);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Password changed successfully");
            response.setUser(userDto);
            logger.info("Password change completed successfully for identifier: {}", identifier);
            return response;
        } catch (OurException e) {
            logger.error("Password change failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Password change failed with unexpected error", e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response resetPassword(String email) {
        long startTime = System.currentTimeMillis();
        logger.info("Resetting password for email: {}", email);
        Response response = new Response();

        try {
            // Rate limiting: 7 req/min for password operations
            if (!rateLimiterService.isAllowed("user:resetPassword:" + email, 7, 60)) {
                logger.warn("Rate limit exceeded for resetPassword: {}", email);
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            String newPassword = handleResetPasswordUser(email);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Password reset successfully");
            response.setAdditionalData(Map.of("newPassword", newPassword));
            logger.info("Password reset completed successfully for email: {}", email);
            return response;
        } catch (OurException e) {
            logger.error("Password reset failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Password reset failed with unexpected error", e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response forgotPassword(String email, String dataJson) {
        long startTime = System.currentTimeMillis();
        logger.info("Forgot password for email: {}", email);
        Response response = new Response();

        try {
            // Rate limiting: 7 req/min for password operations
            if (!rateLimiterService.isAllowed("user:forgotPassword:" + email, 7, 60)) {
                logger.warn("Rate limit exceeded for forgotPassword: {}", email);
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            ForgotPasswordRequest request = objectMapper.readValue(dataJson, ForgotPasswordRequest.class);
            String newPassword = request.getPassword();

            UserDto userDto = handleForgotPasswordUser(email, newPassword);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Password updated successfully");
            response.setUser(userDto);
            logger.info("Forgot password completed successfully for email: {}", email);
            return response;
        } catch (OurException e) {
            logger.error("Forgot password failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Forgot password failed with unexpected error", e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response findUserByEmail(String email) {
        long startTime = System.currentTimeMillis();
        logger.info("Finding user by email: {}", email);
        Response response = new Response();

        try {
            // Rate limiting: 90 req/min for GET APIs
            if (!rateLimiterService.isAllowed("user:findUserByEmail", 90, 60)) {
                logger.warn("Rate limit exceeded for findUserByEmail");
                return buildErrorResponse(429, "Rate limit exceeded. Please try again later.");
            }

            UserDto userDto = handleFindByEmail(email);

            if (userDto == null) {
                return buildErrorResponse(404, "User not found");
            }

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("User found successfully");
            response.setUser(userDto);
            logger.info("User found successfully for email: {}", email);
            return response;
        } catch (OurException e) {
            logger.error("Find user by email failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Find user by email failed with unexpected error", e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }
}