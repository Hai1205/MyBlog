package com.example.authservice.services.apis;

import com.example.authservice.dtos.requests.*;
import com.example.authservice.dtos.responses.*;
import com.example.authservice.dtos.responses.views.UserView;
import com.example.authservice.exceptions.OurException;
import com.example.authservice.services.apis.handlers.AuthHandler;
import com.example.rediscommon.services.RateLimiterService;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthApi {

    private final ObjectMapper objectMapper;
    private final RateLimiterService rateLimiterService;
    private final AuthHandler authHandler;
    private final CacheKeyBuilder cacheKeys;

    @Value("${DEV_MODE}")
    private String devMode;

    public AuthApi(RateLimiterService rateLimiterService, AuthHandler authHandler) {
        this.objectMapper = new ObjectMapper();
        this.rateLimiterService = rateLimiterService;
        this.authHandler = authHandler;
        this.cacheKeys = CacheKeyBuilder.forService("auth");
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

    public Response login(String identifier, String dataJson, HttpServletResponse httpServletResponse) {
        long startTime = requestStart("Login attempt");

        try {
            log.debug("Parsing login request JSON for identifier={}", identifier);

            LoginRequest request = objectMapper.readValue(dataJson, LoginRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithParam("login", identifier);
            checkRateLimit(rateLimitKey, 7, 60);

            log.debug("Calling authHandler.handleLogin for identifier={}", identifier);
            UserView user = authHandler.handleLogin(identifier, request.getPassword(), httpServletResponse);

            log.info("Login successful for user: {} (ID: {})", user.getEmail(), user.getId());

            Response response = new Response("Login successful");
            response.setUserView(user);
            return response;
        } catch (OurException e) {
            log.error("OurException in login for identifier={}: {}", identifier, e.getMessage());
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response validateToken(String token, String username) {
        long startTime = requestStart(String.format("Token validation attempt for username: %s", username));

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("validateToken", username);
            checkRateLimit(rateLimitKey, 7, 60);

            log.debug("Calling authHandler.handleValidateToken for username={}", username);
            authHandler.handleValidateToken(token, username);

            log.info("Token validation successful for username: {}", username);

            return new Response("Token validation successful");
        } catch (OurException e) {
            log.error("OurException in validateToken for username={}: {}", username, e.getMessage());
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response register(String dataJson) {
        long startTime = requestStart("Registration attempt");

        try {
            String rateLimitKey = cacheKeys.forMethod("register");
            checkRateLimit(rateLimitKey, 7, 60);

            log.debug("Calling authHandler.handleRegister");
            authHandler.handleRegister(dataJson);

            log.info("Registration successful for email");

            return new Response("Registration successful");
        } catch (OurException e) {
            log.error("OurException in register: {}", e.getMessage());
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response verifyOTP(String identifier, String dataJson) {
        long startTime = requestStart(String.format("OTP verification attempt for identifier: {}", identifier));

        try {
            log.debug("Parsing verifyOTP request JSON for identifier={}", identifier);
            VerifyOtpRequest request = objectMapper.readValue(dataJson, VerifyOtpRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithParam("verifyOTP", identifier);
            log.debug("Checking rate limit for key={}", rateLimitKey);
            checkRateLimit(rateLimitKey, 7, 60);

            boolean isActivation = request.getIsActivation() != null && request.getIsActivation();
            log.debug("Calling authHandler.handleVerifyOTP for identifier={}, isActivation={}", identifier,
                    isActivation);
            authHandler.handleVerifyOTP(identifier, request.getOtp(), isActivation);

            log.info("OTP verification successful for identifier: {}", identifier);

            return new Response("OTP verified successfully!");
        } catch (OurException e) {
            log.error("OurException in verifyOTP for identifier={}: {}", identifier, e.getMessage());
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response sendOTP(String identifier) {
        long startTime = requestStart("Sending OTP to identifier: " + identifier);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("sendOTP", identifier);
            log.debug("Checking rate limit for key={}", rateLimitKey);
            checkRateLimit(rateLimitKey, 7, 60);

            log.debug("Calling authHandler.handleSendOTP for identifier={}", identifier);
            authHandler.handleSendOTP(identifier);

            log.info("OTP sent successfully to identifier: {}", identifier);

            return new Response("OTP is sent!");
        } catch (OurException e) {
            log.error("OurException in sendOTP for identifier={}: {}", identifier, e.getMessage());
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Unexpected exception in sendOTP for identifier={}: {}", identifier, e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response changePassword(String identifier, String dataJson) {
        long startTime = requestStart("Password change attempt for identifier: " + identifier);

        try {
            log.debug("Parsing changePassword request JSON for identifier={}", identifier);
            ChangePasswordRequest request = objectMapper.readValue(dataJson, ChangePasswordRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithParam("changePassword", identifier);
            log.debug("Checking rate limit for key={}", rateLimitKey);
            checkRateLimit(rateLimitKey, 7, 60);

            log.debug("Calling authHandler.handleChangePassword for identifier={}", identifier);
            authHandler.handleChangePassword(identifier, request);

            log.info("Password changed successfully for identifier: {}", identifier);

            return new Response("Password changed successfully!");
        } catch (OurException e) {
            log.error("OurException in changePassword for identifier={}: {}", identifier, e.getMessage());
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Unexpected exception in changePassword for identifier={}: {}", identifier, e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response resetPassword(String email) {
        long startTime = requestStart("Password reset attempt for email: " + email);

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("resetPassword", email);
            log.debug("Checking rate limit for key={}", rateLimitKey);
            checkRateLimit(rateLimitKey, 7, 60);

            log.debug("Calling authHandler.handleResetPassword for email={}", email);
            authHandler.handleResetPassword(email);

            log.info("Password reset email sent successfully to: {}", email);

            return new Response("Password reset email sent successfully!");
        } catch (OurException e) {
            log.error("OurException in resetPassword for email={}: {}", email, e.getMessage());
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Unexpected exception in resetPassword for email={}: {}", email, e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response forgotPassword(String identifier, String dataJson) {
        long startTime = requestStart("Forgot password attempt for identifier: " + identifier);

        try {
            log.debug("Parsing forgotPassword request JSON for identifier={}", identifier);
            ForgotPasswordRequest request = objectMapper.readValue(dataJson, ForgotPasswordRequest.class);

            String rateLimitKey = cacheKeys.forMethodWithParam("forgotPassword", identifier);
            log.debug("Checking rate limit for key={}", rateLimitKey);
            checkRateLimit(rateLimitKey, 7, 60);

            log.debug("Calling authHandler.handleForgotPassword for identifier={}", identifier);
            authHandler.handleForgotPassword(identifier, request);

            log.info("Password updated successfully via forgot password for identifier: {}", identifier);

            return new Response("Password updated successfully!");
        } catch (OurException e) {
            log.error("OurException in forgotPassword for identifier={}: {}", identifier, e.getMessage());
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Unexpected exception in forgotPassword for identifier={}: {}", identifier, e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response refreshToken(
            String authHeader,
            HttpServletRequest httpRequest,
            HttpServletResponse httpServletResponse) {

        long startTime = requestStart("Token refresh attempt");

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("refreshToken", authHeader.hashCode());
            log.debug("Checking rate limit for key={}", rateLimitKey);
            checkRateLimit(rateLimitKey, 7, 60);

            log.debug("Calling authHandler.handleRefreshToken");
            UserView user = authHandler.handleRefreshToken(authHeader, httpRequest, httpServletResponse);

            log.info("Token refreshed successfully for user: {}", user.getUsername());

            return new Response("Token refreshed successfully!");
        } catch (OurException e) {
            log.error("OurException in refreshToken: {}", e.getMessage());
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Unexpected exception in refreshToken: {}", e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response logout(String identifier, HttpServletResponse httpServletResponse) {
        long startTime = requestStart("User logout attempt");

        try {
            String rateLimitKey = cacheKeys.forMethodWithParam("logout", identifier);
            checkRateLimit(rateLimitKey, 7, 60);

            Response response = new Response();

            log.debug("Calling authHandler.handleLogout");
            authHandler.handleLogout(httpServletResponse);

            response.setMessage("Logged out successfully");
            log.info("User logged out successfully");

            return response;
        } catch (OurException e) {
            log.error("OurException in logout for identifier={}: {}", identifier, e.getMessage());
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Unexpected exception in logout for identifier={}: {}", identifier, e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }
}