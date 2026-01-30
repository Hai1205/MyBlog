package com.example.userservice.services.apis.handlers;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.cloudinarycommon.CloudinaryService;
import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.example.securitycommon.models.AuthenticatedUser;
import com.example.securitycommon.utils.SecurityUtils;
import com.example.userservice.dtos.UserDto;
import com.example.userservice.entities.User;
import com.example.userservice.entities.User.UserRole;
import com.example.userservice.entities.User.UserStatus;
import com.example.userservice.exceptions.OurException;
import com.example.userservice.mappers.UserMapper;
import com.example.userservice.repositories.SimpleUserRepository;
import com.example.userservice.repositories.UserCommandRepository;
import com.example.userservice.repositories.UserQueryRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserHandler {

    private final UserQueryRepository userQueryRepository;
    private final UserCommandRepository userCommandRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
    private final SecureRandom random;
    private final RedisCacheService cacheService;
    private final CacheKeyBuilder cacheKeys;

    @Value("${PRIVATE_CHARS}")
    private String privateChars;

    @Value("${PASSWORD_LENGTH}")
    private int passwordLength;

    public UserHandler(
            SimpleUserRepository simpleUserRepository,
            UserQueryRepository userQueryRepository,
            UserCommandRepository userCommandRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            CloudinaryService cloudinaryService,
            RedisCacheService cacheService) {
        this.userQueryRepository = userQueryRepository;
        this.userCommandRepository = userCommandRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.random = new SecureRandom();
        this.cloudinaryService = cloudinaryService;
        this.cacheService = cacheService;
        this.cacheKeys = CacheKeyBuilder.forService("user");
    }

    @Transactional(readOnly = true)
    public List<UserDto> handleGetAllUsers() {
        try {
            String cacheKey = cacheKeys.forMethod("handleGetAllUsers");
            List<UserDto> users = cacheService.getCacheDataList(cacheKey, UserDto.class);

            if (users == null) {
                log.debug("Cache miss for handleGetAllUsers, fetching from database");
                users = userQueryRepository.findAllUsers(Pageable.unpaged()).stream()
                        .map(userMapper::toDto)
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .collect(Collectors.toList());

                cacheService.setCacheData(cacheKey, users);
                log.debug("Fetched {} users from database and cached", users.size());
            }

            log.info("Completed handleGetAllUsers with {} users", users.size());
            return users;
        } catch (Exception e) {
            log.error("Error in handleGetAllUsers: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
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
            log.info("Creating user with email: {}", email);

            // Basic validation
            if (email == null || email.isEmpty()) {
                log.warn("Attempted to create user with empty email");
                throw new OurException("Email is required", 400);
            }

            if (userQueryRepository.existsByEmail(email)) {
                log.warn("Attempted to create user with existing email: {}", email);
                throw new OurException("Email already exists", 400);
            }

            // Ensure username fallback to email prefix when not provided
            if (username == null || username.isEmpty()) {
                username = email.split("@")[0];
                log.debug("Generated username from email: {}", username);
            }

            // Check if username already exists
            if (userQueryRepository.existsByUsername(username)) {
                log.warn("Attempted to create user with existing username: {}", username);
                throw new OurException("Username already exists", 400);
            }

            // If no password provided, generate a secure random one
            if (password == null || password.isEmpty()) {
                password = handleGenerateRandomPassword();
                log.info("No password provided for {} - generated a random password", email);
            }

            User user = new User(
                    username,
                    email);
            log.debug("User entity created with username: {}, email: {}", username, email);

            if (birth != null && !birth.isEmpty()) {
                user.setBirth(birth);
            }
            if (summary != null && !summary.isEmpty()) {
                user.setSummary(summary);
            }

            if (avatar != null && !avatar.isEmpty()) {
                log.debug("Uploading avatar for user: {}", email);
                var uploadResult = cloudinaryService.uploadImage(avatar);
                if (uploadResult.containsKey("error")) {
                    log.error("Failed to upload avatar for user {}: {}", email, uploadResult.get("error"));
                    throw new RuntimeException("Failed to upload avatar: " + uploadResult.get("error"));
                }

                user.setAvatarUrl((String) uploadResult.get("url"));
                user.setAvatarPublicId((String) uploadResult.get("publicId"));
                log.debug("Avatar uploaded successfully for user: {}", email);
            }

            user.setPassword(passwordEncoder.encode(password));
            log.debug("Password encoded for user: {}", email);

            if (role != null && !role.isEmpty()) {
                user.setRole(UserRole.valueOf(role));
                log.debug("Role set to {} for user: {}", role, email);
            }

            if (status != null && !status.isEmpty()) {
                user.setStatus(UserStatus.valueOf(status));
                log.debug("Status set to {} for user: {}", status, email);
            }

            // Insert user using command repository
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            log.debug("Inserting user into database with ID: {}", userId);

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

            log.info("User created successfully with ID: {}", userId);

            UserDto result = builderUser(
                    userId,
                    user.getUsername(),
                    user.getEmail(),
                    user.getBirth(),
                    user.getSummary(),
                    user.getAvatarUrl(),
                    user.getRole().name(),
                    user.getStatus().name(),
                    user.getFacebook(),
                    user.getLinkedin(),
                    user.getInstagram(),
                    now,
                    now);

            log.info("Completed handleCreateUser for email: {}", email);

            return result;
        } catch (IllegalArgumentException e) {
            log.error("Invalid role/status value in handleCreateUser: {}", e.getMessage());
            throw new OurException("Invalid role/status value", 400);
        } catch (OurException e) {
            log.warn("OurException in handleCreateUser: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in handleCreateUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void handleActivateUser(String email) {
        try {
            log.info("Starting handleActivateUser for email: {}", email);

            User user = handleGetUserByEmail(email);
            log.debug("User found for activation: {}", user.getId());

            Instant now = Instant.now();

            // Update status using command repository
            log.debug("Updating user status to active for email: {}", email);
            userCommandRepository.updateUserStatusByEmail(email, UserStatus.active, now);

            log.info("Completed handleActivateUser for email: {}", email);
        } catch (OurException e) {
            log.warn("OurException in handleActivateUser: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleActivateUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean handleIsValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(regex);
    }

    private UserDto builderUser(UUID userId,
            String username,
            String email,
            String birth,
            String summary,
            String avatarUrl,
            String role,
            String status,
            String facebook,
            String linkedin,
            String instagram,
            Instant createdAt,
            Instant updatedAt) {
        return UserDto.builder()
                .id(userId)
                .username(username)
                .email(email)
                .birth(birth)
                .summary(summary)
                .avatarUrl(avatarUrl)
                .role(role)
                .status(status)
                .facebook(facebook)
                .linkedin(linkedin)
                .instagram(instagram)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public User handleAuthenticateUser(String identifier, String currentPassword) {
        try {
            log.info("Starting handleAuthenticateUser for identifier: {}", identifier);

            User user = handleGetUserByIdentifier(identifier);
            log.debug("User found for identifier: {}", user.getId());

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                log.warn("Password mismatch for identifier: {}", identifier);
                throw new OurException("Invalid credentials", 400);
            }

            log.info("Authentication successful for identifier: {}", identifier);
            return user;
        } catch (OurException e) {
            log.warn("OurException in handleAuthenticateUser: {}", e.getMessage());
            throw null;
        } catch (Exception e) {
            log.error("Error in handleAuthenticateUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String handleGenerateRandomPassword() {
        try {
            log.debug("Starting handleGenerateRandomPassword");

            StringBuilder password = new StringBuilder(passwordLength);

            for (int i = 0; i < passwordLength; i++) {
                int index = random.nextInt(privateChars.length());
                password.append(privateChars.charAt(index));
            }

            String generatedPassword = password.toString();
            log.debug("Generated random password of length: {}", generatedPassword.length());

            return generatedPassword;
        } catch (Exception e) {
            log.error("Error in handleGenerateRandomPassword: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public String handleResetPasswordUser(String email) {
        try {
            log.info("Starting handleResetPasswordUser for email: {}", email);

            handleGetUserByEmail(email);

            log.debug("User found for password reset: {}", email);

            Instant now = Instant.now();
            String newPassword = handleGenerateRandomPassword();
            String encodedPassword = passwordEncoder.encode(newPassword);

            log.debug("Updating password for email: {}", email);
            userCommandRepository.updateUserPasswordByEmail(email, encodedPassword, now);
            log.info("Password reset successful for email: {}", email);

            return newPassword;
        } catch (OurException e) {
            log.warn("OurException in handleResetPasswordUser: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleResetPasswordUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean handleForgotPasswordUser(String email, String newPassword) {
        try {
            log.info("Starting handleForgotPasswordUser for email: {}", email);

            handleGetUserByEmail(email);

            log.debug("User found for forgot password: {}", email);

            String encodedPassword = passwordEncoder.encode(newPassword);
            Instant now = Instant.now();

            log.debug("Updating password for email: {}", email);
            int updated = userCommandRepository.updateUserPasswordByEmail(email, encodedPassword, now);

            boolean result = updated > 0;
            log.info("Forgot password {} for email: {}", result ? "successful" : "failed", email);

            return result;
        } catch (OurException e) {
            log.warn("OurException in handleForgotPasswordUser: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleForgotPasswordUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void handleChangePasswordUser(String email, String currentPassword, String newPassword) {
        try {
            log.info("Starting handleChangePasswordUser for email: {}", email);

            handleAuthenticateUser(email, currentPassword);

            log.debug("Authentication successful for password change: {}", email);

            String encodedPassword = passwordEncoder.encode(newPassword);
            Instant now = Instant.now();

            log.debug("Updating password for email: {}", email);
            userCommandRepository.updateUserPasswordByEmail(email, encodedPassword, now);
            log.info("Password changed successfully for email: {}", email);
        } catch (OurException e) {
            log.warn("OurException in handleChangePasswordUser: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleChangePasswordUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public User handleGetUserById(UUID userId) {
        try {
            log.info("Starting handleGetUserById for userId: {}", userId);

            String cacheKey = cacheKeys.forMethodWithId("handleGetUserById", userId);
            User user = cacheService.getCacheData(cacheKey, User.class);

            if (user == null) {
                log.debug("Cache miss for handleGetUserById, fetching from database");
                user = userQueryRepository.findUserById(userId)
                        .orElseThrow(() -> new OurException("User not found", 404));

                cacheService.setCacheData(cacheKey, user);
                log.debug("Fetched user from database and cached: {}", userId);
            }

            log.info("Completed handleGetUserById for userId: {}", userId);
            return user;
        } catch (OurException e) {
            log.warn("OurException in handleGetUserById: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleGetUserById: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public UserDto handleUpdateUser(UUID userId, String location, String birth,
            String summary, String role, String status, String instagram, String linkedin, String facebook,
            MultipartFile avatar) {
        try {
            log.info("Starting handleUpdateUser for userId: {}", userId);

            User existingUser = handleGetUserById(userId);
            log.debug("Existing user loaded for update: {}", userId);

            Instant now = Instant.now();
            AuthenticatedUser currentUser = SecurityUtils.getCurrentUser();
            boolean privilegedChangeAllowed = currentUser == null || currentUser.hasRole("ADMIN");
            log.debug("Privileged change allowed: {}", privilegedChangeAllowed);

            if (avatar != null && !avatar.isEmpty()) {
                log.debug("Updating avatar for user: {}", userId);
                String oldAvatarPublicId = existingUser.getAvatarPublicId();
                if (oldAvatarPublicId != null && !oldAvatarPublicId.isEmpty()) {
                    cloudinaryService.deleteImage(oldAvatarPublicId);
                    log.debug("Deleted old avatar for user: {}", userId);
                }

                var uploadResult = cloudinaryService.uploadImage(avatar);
                if (uploadResult.containsKey("error")) {
                    log.error("Failed to upload avatar for user {}: {}", userId, uploadResult.get("error"));
                    throw new RuntimeException("Failed to upload avatar: " + uploadResult.get("error"));
                }

                existingUser.setAvatarUrl((String) uploadResult.get("url"));
                existingUser.setAvatarPublicId((String) uploadResult.get("publicId"));
                log.debug("Avatar updated successfully for user: {}", userId);
            }

            if (instagram != null && !instagram.equals(existingUser.getInstagram())) {
                existingUser.setInstagram(instagram);
                log.debug("Instagram updated for user: {}", userId);
            }

            if (linkedin != null && !linkedin.equals(existingUser.getLinkedin())) {
                existingUser.setLinkedin(linkedin);
                log.debug("LinkedIn updated for user: {}", userId);
            }

            if (facebook != null && !facebook.equals(existingUser.getFacebook())) {
                existingUser.setFacebook(facebook);
                log.debug("Facebook updated for user: {}", userId);
            }

            if (birth != null && !birth.equals(existingUser.getBirth())) {
                existingUser.setBirth(birth);
                log.debug("Birth updated for user: {}", userId);
            }

            if (summary != null && !summary.equals(existingUser.getSummary())) {
                existingUser.setSummary(summary);
                log.debug("Summary updated for user: {}", userId);
            }

            if (role != null && !role.isEmpty()) {
                if (!privilegedChangeAllowed) {
                    log.warn("Unauthorized attempt to change role for user {} by user: {}",
                            userId, currentUser != null ? currentUser.getEmail() : "unknown");
                    throw new OurException("Forbidden", 403);
                }
                existingUser.setRole(User.UserRole.valueOf(role));
                log.debug("Role updated to {} for user: {}", role, userId);
            }

            if (status != null && !status.isEmpty()) {
                if (!privilegedChangeAllowed) {
                    log.warn("Unauthorized attempt to change status for user {} by user: {}",
                            userId, currentUser != null ? currentUser.getEmail() : "unknown");
                    throw new OurException("Forbidden", 403);
                }
                existingUser.setStatus(User.UserStatus.valueOf(status));
                log.debug("Status updated to {} for user: {}", status, userId);
            }

            log.debug("Updating user in database for userId: {}", userId);
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
                    now);

            return userMapper.toDto(existingUser);
        } catch (OurException e) {
            log.warn("OurException in handleUpdateUser: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleUpdateUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean handleDeleteUser(UUID userId) {
        try {
            log.info("Starting handleDeleteUser for userId: {}", userId);

            User user = handleGetUserById(userId);
            log.debug("User found for deletion: {}", userId);

            String avatarPublicId = user.getAvatarPublicId();
            if (avatarPublicId != null && !avatarPublicId.isEmpty()) {
                log.debug("Deleting avatar for user: {}", userId);
                cloudinaryService.deleteImage(avatarPublicId);
            }

            log.debug("Deleting user from database: {}", userId);
            userCommandRepository.deleteUserById(userId);
            log.info("User deleted successfully: {}", userId);

            return true;
        } catch (OurException e) {
            log.warn("OurException in handleDeleteUser: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleDeleteUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public User handleGetUserByEmail(String email) {
        try {
            log.info("Starting handleGetUserByEmail for email: {}", email);

            String cacheKey = cacheKeys.forMethodWithParam("handleGetUserByEmail", email);
            User user = cacheService.getCacheData(cacheKey, User.class);

            if (user == null) {
                log.debug("Cache miss for handleGetUserByEmail, fetching from database");
                user = userQueryRepository.findByEmail(email)
                        .orElseThrow(() -> new OurException("User not found", 404));

                cacheService.setCacheData(cacheKey, user);
                log.debug("Fetched user from database and cached: {}", email);
            }

            log.info("Completed handleGetUserByEmail for email: {}", email);

            return user;
        } catch (OurException e) {
            log.warn("OurException in handleGetUserByEmail: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleGetUserByEmail: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public User handleGetUserByUsername(String username) {
        try {
            log.info("Starting handleGetUserByUsername for username: {}", username);

            String cacheKey = cacheKeys.forMethodWithParam("handleGetUserByUsername", username);
            User user = cacheService.getCacheData(cacheKey, User.class);

            if (user == null) {
                log.debug("Cache miss for handleGetUserByUsername, fetching from database");
                user = userQueryRepository.findByUsername(username)
                        .orElseThrow(() -> new OurException("User not found", 404));

                cacheService.setCacheData(cacheKey, user);
                log.debug("Fetched user from database and cached: {}", username);
            }

            log.info("Completed handleGetUserByUsername for username: {}", username);

            return user;
        } catch (OurException e) {
            log.warn("OurException in handleGetUserByUsername: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleGetUserByUsername: {}", e.getMessage(), e);
            throw e;
        }
    }

    public User handleGetUserByIdentifier(String identifier) {
        try {
            log.info("Starting handleGetUserByIdentifier for identifier: {}", identifier);

            String cacheKey = cacheKeys.forMethodWithParam("handleGetUserByIdentifier", identifier);
            User user = cacheService.getCacheData(cacheKey, User.class);

            if (user == null) {
                UUID userId = isValidUUID(identifier);
                
                if (userId != null) {
                    log.debug("Identifier '{}' is UUID", identifier);

                    user = handleGetUserById(userId);
                } else {
                    boolean isEmailValid = handleIsValidEmail(identifier);
                    
                    if (isEmailValid) {
                        log.debug("Identifier '{}' is email", identifier);
                        
                        user = handleGetUserByEmail(identifier);
                    } else {
                        log.debug("Identifier '{}' is username", identifier);
                        user = handleGetUserByUsername(identifier);
                    }
                }

                cacheService.setCacheData(cacheKey, user);
                log.debug("Fetched user from database and cached: {}", identifier);
            }

            log.info("Completed handleGetUserByIdentifier for identifier: {}", identifier);

            return user;
        } catch (OurException e) {
            log.warn("OurException in handleGetUserByIdentifier: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleGetUserByIdentifier: {}", e.getMessage(), e);
            throw e;
        }
    }

    private UUID isValidUUID(String value) {
        try {
            if (value == null)
                return null;

            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
