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
import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.services.ApiResponseHandler;
import com.example.rediscommon.utils.CacheKeyBuilder;
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
    private final RedisCacheService cacheService;
    private final ApiResponseHandler<Response> responseHandler;
    private final CacheKeyBuilder cacheKeys;

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
            RateLimiterService rateLimiterService,
            RedisCacheService cacheService,
            ApiResponseHandler<Response> responseHandler) {
        // this.simpleUserRepository = simpleUserRepository;
        this.userQueryRepository = userQueryRepository;
        this.userCommandRepository = userCommandRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.cloudinaryService = cloudinaryService;
        this.objectMapper = new ObjectMapper();
        this.redisService = redisService;
        this.rateLimiterService = rateLimiterService;
        this.cacheService = cacheService;
        this.responseHandler = responseHandler;
        this.cacheKeys = CacheKeyBuilder.forService("user");
    }

    public UserDto handleCreateUser(String username,
            String email,
            String password,
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
                    email);

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
        return responseHandler.executeWithResponse(
                cacheKeys.forMethod("createUser"),
                45,
                () -> {
                    try {
                        CreateUserRequest request = objectMapper.readValue(dataJson, CreateUserRequest.class);
                        return handleCreateUser(request.getUsername(), request.getEmail(), request.getPassword(),
                                request.getLocation(), request.getBirth(), request.getSummary(), request.getRole(),
                                request.getStatus(), request.getInstagram(), request.getLinkedin(),
                                request.getFacebook(), request.getAvatar());
                    } catch (Exception e) {
                        throw new OurException("Invalid request format", 400);
                    }
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "User created successfully",
                201);
    }

    public List<UserDto> handleGetAllUsers() {
        return cacheService.executeWithCacheList(
                cacheKeys.forMethod("handleGetAllUsers"),
                UserDto.class,
                () -> userQueryRepository.findAllUsers(Pageable.unpaged()).stream()
                        .map(userMapper::toDto)
                        .collect(Collectors.toList()));
    }

    public Response getAllUsers() {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethod("getAllUsers"),
                90,
                this::handleGetAllUsers,
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUsers,
                "Users retrieved successfully",
                200);
    }

    public UserDto handleGetUserById(UUID userId) {
        return cacheService.executeWithCache(
                cacheKeys.forMethodWithId("handleGetUserById", userId),
                UserDto.class,
                () -> {
                    User user = userQueryRepository.findUserById(userId)
                            .orElseThrow(() -> new OurException("User not found", 404));
                    return userMapper.toDto(user);
                });
    }

    public Response getUserById(UUID userId) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("getUserById", userId),
                90,
                () -> handleGetUserById(userId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "User retrieved successfully",
                200);
    }

    public UserDto handleUpdateUser(UUID userId, String location, String birth,
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
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("updateUser", userId),
                45,
                () -> {
                    try {
                        UpdateUserRequest request = objectMapper.readValue(dataJson, UpdateUserRequest.class);
                        return handleUpdateUser(userId, request.getLocation(), request.getBirth(),
                                request.getSummary(), request.getRole(), request.getStatus(),
                                request.getInstagram(), request.getLinkedin(), request.getFacebook(), avatar);
                    } catch (Exception e) {
                        throw new OurException("Invalid request format", 400);
                    }
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "User updated successfully",
                200);
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
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithId("deleteUser", userId),
                20,
                () -> handleDeleteUser(userId),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                (r, deleted) -> {
                },
                "User deleted successfully",
                200);
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

    public Response getRecentUsers(int limit) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("getRecentUsers", limit),
                90,
                () -> handleGetRecentUsers(limit),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUsers,
                "Recent users retrieved successfully",
                200);
    }

    public Response authenticateUser(String identifier, String password) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("authenticateUser", identifier),
                7,
                () -> handleAuthenticateUser(identifier, password),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "User authenticated successfully",
                200);
    }

    public Response findUserByIdentifier(String identifier) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("findUserByIdentifier", identifier),
                90,
                () -> {
                    UserDto userDto = handleFindByIdentifier(identifier);
                    if (userDto == null) {
                        throw new OurException("User not found", 404);
                    }
                    return userDto;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "User found successfully",
                200);
    }

    public Response activateUser(String email) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("activateUser", email),
                45,
                () -> handleActivateUser(email),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "User activated successfully",
                200);
    }

    public Response changePassword(String identifier, String dataJson) {
        return responseHandler.executeWithoutRateLimit(
                () -> {
                    if (!rateLimiterService.isAllowed("user:changePassword:" + identifier, 7, 60)) {
                        throw new OurException("Rate limit exceeded. Please try again later.", 429);
                    }
                    try {
                        ChangePasswordRequest request = objectMapper.readValue(dataJson, ChangePasswordRequest.class);
                        return handleChangePasswordUser(identifier, request.getCurrentPassword(),
                                request.getNewPassword());
                    } catch (OurException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new OurException("Invalid request format", 400);
                    }
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "Password changed successfully",
                200);
    }

    public Response resetPassword(String email) {
        Response response = responseHandler.executeWithoutRateLimit(
                () -> {
                    if (!rateLimiterService.isAllowed("user:resetPassword:" + email, 7, 60)) {
                        throw new OurException("Rate limit exceeded. Please try again later.", 429);
                    }
                    return handleResetPasswordUser(email);
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                (r, newPassword) -> r.setAdditionalData(Map.of("newPassword", newPassword)),
                "Password reset successfully",
                200);
        return response;
    }

    public Response forgotPassword(String email, String dataJson) {
        return responseHandler.executeWithoutRateLimit(
                () -> {
                    if (!rateLimiterService.isAllowed("user:forgotPassword:" + email, 7, 60)) {
                        throw new OurException("Rate limit exceeded. Please try again later.", 429);
                    }
                    try {
                        ForgotPasswordRequest request = objectMapper.readValue(dataJson, ForgotPasswordRequest.class);
                        return handleForgotPasswordUser(email, request.getPassword());
                    } catch (OurException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new OurException("Invalid request format", 400);
                    }
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "Password updated successfully",
                200);
    }

    public Response findUserByEmail(String email) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("findUserByEmail", email),
                90,
                () -> {
                    UserDto userDto = handleFindByEmail(email);
                    if (userDto == null) {
                        throw new OurException("User not found", 404);
                    }
                    return userDto;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "User found successfully",
                200);
    }
}