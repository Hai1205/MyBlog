package com.example.authservice.services.apis.handlers;

import com.example.authservice.dtos.requests.*;
import com.example.authservice.dtos.responses.Response;
import com.example.authservice.dtos.responses.views.UserView;
import com.example.authservice.exceptions.OurException;
import com.example.authservice.services.JwtService;
import com.example.authservice.services.OtpService;
import com.example.authservice.services.feigns.UserFeignClient;
import com.example.authservice.services.rabbitmqs.producers.AuthProducer;
import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthHandler {

    private final JwtService jwtService;
    private final UserFeignClient userFeignClient;
    private final AuthProducer authProducer;
    private final OtpService otpService;
    private final ObjectMapper objectMapper;
    private final CacheKeyBuilder cacheKeys;
    private final RedisCacheService cacheService;

    @Value("${DEV_MODE}")
    private String devMode;

    private static final int ACCESS_TOKEN_EXPIRATION_SECONDS = 5 * 60 * 60; // 5 hours
    private static final int REFRESH_TOKEN_EXPIRATION_SECONDS = 7 * 24 * 60 * 60; // 7 days

    public AuthHandler(
            JwtService jwtService,
            UserFeignClient userFeignClient,
            AuthProducer authProducer,
            OtpService otpService,
            RedisCacheService cacheService) {
        this.jwtService = jwtService;
        this.userFeignClient = userFeignClient;
        this.authProducer = authProducer;
        this.otpService = otpService;
        this.objectMapper = new ObjectMapper();
        this.cacheKeys = CacheKeyBuilder.forService("auth");
        this.cacheService = cacheService;
    }

    // ========== Private Helper Methods ==========

    private UserView handleGetUserByIdentifier(String identifier) {
        try {
            String cacheKey = cacheKeys.forMethodWithParam("validateUser", identifier);
            UserView user = cacheService.getCacheData(cacheKey, UserView.class);

            if (user == null) {
                Response response = userFeignClient.findUserByIdentifier(identifier);
                if (response.getStatusCode() != 200) {
                    throw new OurException("User not found", 404);
                }

                cacheService.setCacheData(cacheKey, response.getUserView());
            }

            return user;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    private UserView handleGetUserByEmail(String email) {
        try {
            String cacheKey = cacheKeys.forMethodWithParam("validateUser", email);
            UserView user = cacheService.getCacheData(cacheKey, UserView.class);

            if (user == null) {
                user = handleGetUserByIdentifier(email);

                cacheService.setCacheData(cacheKey, user);
            }

            return user;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    private Cookie handleCreateCookie(String name, String value, int maxAgeInSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeInSeconds);

        if (devMode != null && devMode.equals("development")) {
            cookie.setHttpOnly(false);
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        } else {
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        }

        return cookie;
    }

    // ========== Business Logic Methods ==========

    public UserView handleLogin(String identifier, String password, HttpServletResponse httpServletResponse) {
        try {
            if (identifier == null || identifier.trim().isEmpty()) {
                throw new OurException("Identifier is required", 404);
            }

            if (password == null || password.trim().isEmpty()) {
                throw new OurException("Password is required", 400);
            }

            Response response = userFeignClient.authenticateUser(identifier, password);
            UserView user = response.getUserView();
            if (user == null) {
                throw new OurException(response.getMessage(), response.getStatusCode());
            }

            boolean isPending = user.getStatus().equals("pending");
            if (isPending) {
                throw new OurException("Account not verified. Please verify your account before logging in.", 403);
            }

            boolean isBanned = user.getStatus().equals("banned");
            if (isBanned) {
                throw new OurException("Account is banned. Please contact support.", 405);
            }

            String userId = user.getId().toString();
            String email = user.getEmail();
            String username = user.getUsername();
            String role = user.getRole();

            String accessToken = jwtService.generateAccessToken(userId, email, role, username);
            String refreshToken = jwtService.generateRefreshToken(userId, email, username);

            Cookie accessTokenCookie = handleCreateCookie("access_token", accessToken, ACCESS_TOKEN_EXPIRATION_SECONDS);
            Cookie refreshTokenCookie = handleCreateCookie("refresh_token", refreshToken,
                    REFRESH_TOKEN_EXPIRATION_SECONDS);
            httpServletResponse.addCookie(accessTokenCookie);
            httpServletResponse.addCookie(refreshTokenCookie);
            httpServletResponse.setHeader("X-Access-Token", accessToken);
            httpServletResponse.setHeader("X-Refresh-Token", refreshToken);

            return user;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public void handleValidateToken(String token, String username) {
        boolean isValid = jwtService.validateToken(token, username);

        if (isValid) {
            log.info("Token validation successful for username: {}", username);
        } else {
            log.warn("Token validation failed for username: {}", username);
        }
    }

    public void handleRegister(String dataJson) {
        userFeignClient.registerUser(dataJson);
    }

    public void handleVerifyOTP(String identifier, String OTP, boolean isActivation) {
        try {
            if (identifier == null || identifier.trim().isEmpty()) {
                throw new OurException("Identifier is required", 400);
            }

            if (OTP == null || OTP.trim().isEmpty()) {
                throw new OurException("Request data is required", 400);
            }

            UserView user = handleGetUserByIdentifier(identifier);

            String email = user.getEmail();

            if (OTP == null || OTP.trim().isEmpty()) {
                throw new OurException("OTP is required", 400);
            }

            boolean isValid = otpService.validateOtp(email, OTP);

            if (!isValid) {
                throw new OurException("Invalid OTP.");
            }

            if (isActivation) {
                userFeignClient.activateUser(email);
            }
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public void handleSendOTP(String identifier) {
        try {
            UserView user = handleGetUserByIdentifier(identifier);

            if (user == null) {
                return;
            }

            String email = user.getEmail();
            String OTP = otpService.generateOtp(email);
            authProducer.sendMailActivation(email, OTP);
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public void handleChangePassword(String identifier, ChangePasswordRequest request) {
        try {
            String currentPassword = request.getCurrentPassword();
            String newPassword = request.getNewPassword();
            String confirmPassword = request.getConfirmPassword();

            if (currentPassword == null || currentPassword.trim().isEmpty() ||
                    newPassword == null || newPassword.trim().isEmpty() ||
                    confirmPassword == null || confirmPassword.trim().isEmpty()) {
                throw new OurException("All password fields are required", 400);
            }

            if (!newPassword.equals(confirmPassword)) {
                throw new OurException("Password does not match.");
            }

            handleGetUserByIdentifier(identifier);

            ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(currentPassword, newPassword,
                    confirmPassword);
            String requestJson = objectMapper.writeValueAsString(changePasswordRequest);
            UserView updatedUser = userFeignClient.changePassword(identifier, requestJson).getUserView();
            if (updatedUser == null) {
                throw new OurException("Password change failed", 500);
            }
        } catch (OurException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new OurException("Error processing JSON", 500);
        } catch (Exception e) {
            throw e;
        }
    }

    public void handleResetPassword(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                throw new OurException("Email is required", 400);
            }

            Response response = userFeignClient.resetPassword(email);
            if (response.getStatusCode() != 200) {
                throw new OurException("Failed to reset password", response.getStatusCode());
            }

            String newPassword = (String) response.getAdditionalData().get("newPassword");

            authProducer.sendMailResetPassword(email, newPassword);
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public void handleForgotPassword(String identifier, ForgotPasswordRequest request) {
        try {
            UserView user = handleGetUserByIdentifier(identifier);

            String email = user.getEmail();
            String newPassword = request.getPassword();
            String rePassword = request.getConfirmPassword();

            if (!newPassword.equals(rePassword)) {
                log.warn("Forgot password failed: Password mismatch for email: {}", email);
                throw new OurException("Password does not match.");
            }

            ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest(newPassword, rePassword);
            String requestJson = objectMapper.writeValueAsString(forgotPasswordRequest);
            userFeignClient.forgotPassword(email, requestJson);
        } catch (OurException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new OurException("Error processing JSON", 500);
        } catch (Exception e) {
            throw e;
        }
    }

    public UserView handleRefreshToken(
            String authHeader,
            HttpServletRequest httpRequest,
            HttpServletResponse httpServletResponse) {
        try {
            String source = "unknown";
            String refreshToken = null;

            if (httpRequest.getCookies() != null) {
                for (Cookie cookie : httpRequest.getCookies()) {
                    if ("refresh_token".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        source = "cookie";
                        break;
                    }
                }
            }

            if (refreshToken == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7);
                source = "header";
            }

            log.info("Refresh token retrieved from: {}", source);

            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new OurException("Refresh token is required", 400);
            }

            // Validate refresh token
            if (!jwtService.validateRefreshToken(refreshToken)) {
                throw new OurException("Invalid or expired refresh token", 401);
            }

            // Extract user info from refresh token
            String email = jwtService.extractEmail(refreshToken);
            String userId = jwtService.extractUserId(refreshToken);

            if (email == null || userId == null) {
                throw new OurException("Invalid refresh token format", 401);
            }

            // Find user
            UserView user = handleGetUserByEmail(email);

            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(userId, email, user.getRole(),
                    user.getUsername());

            // Set new access token in cookie and header
            Cookie accessTokenCookie = handleCreateCookie("access_token", newAccessToken,
                    ACCESS_TOKEN_EXPIRATION_SECONDS);
            httpServletResponse.addCookie(accessTokenCookie);
            httpServletResponse.setHeader("X-Access-Token", newAccessToken);

            return user;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public void handleLogout(HttpServletResponse httpServletResponse) {
        Cookie accessTokenCookie = handleCreateCookie("access_token", "", 0);
        Cookie refreshTokenCookie = handleCreateCookie("refresh_token", "", 0);
        httpServletResponse.addCookie(accessTokenCookie);
        httpServletResponse.addCookie(refreshTokenCookie);
        httpServletResponse.setHeader("X-Access-Token", "");
        httpServletResponse.setHeader("X-Refresh-Token", "");
    }
}
