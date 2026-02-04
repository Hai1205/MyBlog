package com.example.chatservice.services.apis;

import com.example.chatservice.dtos.UserDto;
import com.example.chatservice.dtos.requests.auth.ChangePasswordRequest;
import com.example.chatservice.dtos.requests.auth.ForgotPasswordRequest;
import com.example.chatservice.dtos.requests.user.CreateUserRequest;
import com.example.chatservice.dtos.requests.user.UpdateUserRequest;
import com.example.chatservice.dtos.responses.Response;
import com.example.chatservice.dtos.responses.views.UserView;
import com.example.chatservice.entities.User;
import com.example.chatservice.exceptions.OurException;
import com.example.chatservice.mappers.UserMapper;
import com.example.chatservice.services.apis.handlers.UserHandler;
import com.example.rediscommon.services.RateLimiterService;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserApi {

    private final UserHandler userHandler;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final RateLimiterService rateLimiterService;
    private final CacheKeyBuilder cacheKeys;

    public UserApi(
            UserHandler userHandler,
            UserMapper userMapper,
            RateLimiterService rateLimiterService) {
        this.userHandler = userHandler;
        this.userMapper = userMapper;
        this.objectMapper = new ObjectMapper();
        this.rateLimiterService = rateLimiterService;
        this.cacheKeys = CacheKeyBuilder.forService("user");
    }

    private long requestStart(String message) {
        log.info(message);
        return System.currentTimeMillis();
    }

    private void requestEnd(long startTime) {
        long endTime = System.currentTimeMillis();
        log.info("Completed request in {} ms", endTime - startTime);
    }

    private void checkRateLimit(String rateLimitKey, int maxRequests, int timeWindowSeconds) {
        if (!rateLimiterService.isAllowed(rateLimitKey, maxRequests, timeWindowSeconds)) {
            throw new OurException("Rate limit exceeded. Please try again later.", 429);
        }
    }

    public Response createUser(String dataJson) {
        long startTime = requestStart("Create user attempt");

        try {
            CreateUserRequest request = objectMapper.readValue(dataJson, CreateUserRequest.class);

            String rateLimitKey = cacheKeys.forMethod("createUser");
            checkRateLimit(rateLimitKey, 45, 60);

            UserDto user = userHandler.handleCreateUser(request.getUsername(), request.getEmail(),
                    request.getPassword(),
                    request.getLocation(), request.getBirth(), request.getSummary(), request.getRole(),
                    request.getStatus(), request.getInstagram(), request.getLinkedin(),
                    request.getFacebook(), request.getAvatar());

            Response response = new Response("User created successfully", 201);
            response.setUser(user);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response followUser(UUID followerId, UUID followingId) {
        long startTime = requestStart("Follow user attempt");

        try {
            String rateLimitKey = cacheKeys.forMethod("followUser");
            checkRateLimit(rateLimitKey, 45, 60);

            userHandler.handleFollowUser(followerId, followingId);

            Response response = new Response("User followed successfully", 201);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response unfollowUser(UUID followerId, UUID followingId) {
        long startTime = requestStart(
                "Unfollow user attempt for follower: " + followerId + " following: " + followingId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParams("unfollowUser", followerId, followingId);
            checkRateLimit(rateLimitKey, 45, 60);

            userHandler.handleUnfollowUser(followerId, followingId);

            log.info("User unfollowed successfully: followerId={} followingId={}", followerId, followingId);

            return new Response("User unfollowed successfully");
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getAllUsers() {
        long startTime = requestStart("Get all users attempt");

        try {
            String rateLimitKey = cacheKeys.forMethod("getAllUsers");
            checkRateLimit(rateLimitKey, 45, 60);

            List<UserDto> users = userHandler.handleGetAllUsers();

            Response response = new Response("Users retrieved successfully");
            List<UserView> userViews = users.stream()
                    .map(userMapper::dtoToView)
                    .collect(Collectors.toList());

            response.setUserViews(userViews);
            response.setUsers(users);
            return response;
        } catch (OurException e) {
            Response response = new Response(e.getMessage(), e.getStatusCode());
            response.setUserViews(new ArrayList<>());
            response.setUsers(new ArrayList<UserDto>());
            return response;
        } catch (Exception e) {
            Response response = new Response("Internal Server Error", 500);
            response.setUserViews(new ArrayList<>());
            response.setUsers(new ArrayList<UserDto>());
            return response;
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getUser(UUID userId) {
        long startTime = requestStart("Get user by id attempt: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithId("getUser", userId);
            checkRateLimit(rateLimitKey, 45, 60);

            User user = userHandler.handleGetUserById(userId);

            Response response = new Response("User retrieved successfully");
            response.setUserView(userMapper.entityToView(user));
            response.setUser(userMapper.toDto(user));
            return response;
        } catch (OurException e) {
            Response response = new Response(e.getMessage(), e.getStatusCode());
            response.setUserView(null);
            response.setUser(null);
            return response;
        } catch (Exception e) {
            Response response = new Response("Internal Server Error", 500);
            response.setUserView(null);
            response.setUser(null);
            return response;
        } finally {
            requestEnd(startTime);
        }
    }

    public Response updateUser(UUID userId, String dataJson, MultipartFile avatar) {
        long startTime = requestStart("Update user attempt: " + userId);

        try {
            UpdateUserRequest request = objectMapper.readValue(dataJson, UpdateUserRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithId("updateUser", userId);
            checkRateLimit(rateLimitKey, 45, 60);

            UserDto user = userHandler.handleUpdateUser(userId, request.getLocation(), request.getBirth(),
                    request.getSummary(), request.getRole(), request.getStatus(),
                    request.getInstagram(), request.getLinkedin(), request.getFacebook(), avatar);

            Response response = new Response("User updated successfully");
            response.setUser(user);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response deleteUser(UUID userId) {
        long startTime = requestStart("Delete user attempt: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethodWithId("deleteUser", userId);
            checkRateLimit(rateLimitKey, 45, 60);

            userHandler.handleDeleteUser(userId);

            return new Response("User deleted successfully");
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response authenticateUser(String identifier, String password) {
        long startTime = requestStart("Authenticate user attempt: " + identifier);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("authenticateUser", identifier);
            checkRateLimit(rateLimitKey, 7, 60);

            User user = userHandler.handleAuthenticateUser(identifier, password);

            Response response = new Response("User authenticated successfully");
            response.setUserView(userMapper.entityToView(user));
            return response;
        } catch (OurException e) {
            Response response = new Response(e.getMessage(), e.getStatusCode());
            response.setUserView(null);
            return response;
        } catch (Exception e) {
            Response response = new Response("Internal Server Error", 500);
            response.setUserView(null);
            return response;
        } finally {
            requestEnd(startTime);
        }
    }

    public Response findUserByIdentifier(String identifier) {
        long startTime = requestStart("Get user by identifier attempt: " + identifier);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("findUserByIdentifier", identifier);
            checkRateLimit(rateLimitKey, 45, 60);

            User user = userHandler.handleGetUserByIdentifier(identifier);

            Response response = new Response("User found successfully");
            response.setUserView(userMapper.entityToView(user));
            response.setUser(userMapper.toDto(user));
            return response;
        } catch (OurException e) {
            Response response = new Response(e.getMessage(), e.getStatusCode());
            response.setUser(null);
            response.setUserView(null);
            return response;
        } catch (Exception e) {
            Response response = new Response("Internal Server Error", 500);
            response.setUser(null);
            response.setUserView(null);
            return response;
        } finally {
            requestEnd(startTime);
        }
    }
   
    public Response findUserProfile(String identifier) {
        long startTime = requestStart("Get user profile attempt: " + identifier);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("findUserProfile", identifier);
            checkRateLimit(rateLimitKey, 45, 60);

            UserDto user = userHandler.handleGetUserProfile(identifier);

            Response response = new Response("User found successfully");
            response.setUser(user);
            return response;
        } catch (OurException e) {
            Response response = new Response(e.getMessage(), e.getStatusCode());
            response.setUser(null);
            return response;
        } catch (Exception e) {
            Response response = new Response("Internal Server Error", 500);
            response.setUser(null);
            return response;
        } finally {
            requestEnd(startTime);
        }
    }

    public Response activateUser(String email) {
        long startTime = requestStart("Activate user attempt: " + email);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("activateUser", email);
            checkRateLimit(rateLimitKey, 7, 60);

            userHandler.handleActivateUser(email);

            return new Response("User activated successfully");
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response changePassword(String identifier, String dataJson) {
        long startTime = requestStart("Change password attempt for identifier: " + identifier);

        try {
            ChangePasswordRequest request = objectMapper.readValue(dataJson, ChangePasswordRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithParam("changePassword", identifier);
            checkRateLimit(rateLimitKey, 7, 60);

            userHandler.handleChangePasswordUser(identifier, request.getCurrentPassword(),
                    request.getNewPassword());

            return new Response("Password changed successfully");
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response resetPassword(String email) {
        long startTime = requestStart("Reset password attempt for email: " + email);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("resetPassword", email);
            checkRateLimit(rateLimitKey, 7, 60);

            String newPassword = userHandler.handleResetPasswordUser(email);

            Response response = new Response("Password reset successfully");
            response.setAdditionalData(Map.of("newPassword", newPassword));
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response forgotPassword(String email, String dataJson) {
        long startTime = requestStart("Forgot password attempt for email: " + email);

        try {
            ForgotPasswordRequest request = objectMapper.readValue(dataJson, ForgotPasswordRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithParam("forgotPassword", email);
            checkRateLimit(rateLimitKey, 7, 60);

            userHandler.handleForgotPasswordUser(email, request.getPassword());

            return new Response("Password updated successfully");
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response findUserByEmail(String email) {
        long startTime = requestStart("Get user by email attempt: " + email);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("findUserByEmail", email);
            checkRateLimit(rateLimitKey, 45, 60);

            User user = userHandler.handleGetUserByEmail(email);

            Response response = new Response("User found successfully");
            response.setUserView(userMapper.entityToView(user));
            response.setUser(userMapper.toDto(user));
            return response;
        } catch (OurException e) {
            Response response = new Response(e.getMessage(), e.getStatusCode());
            response.setUser(null);
            response.setUserView(null);
            return response;
        } catch (Exception e) {
            Response response = new Response("Internal Server Error", 500);
            response.setUser(null);
            response.setUserView(null);
            return response;
        } finally {
            requestEnd(startTime);
        }
    }
}