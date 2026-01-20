package com.example.authservice.services.apis;

import com.example.authservice.dtos.*;
import com.example.authservice.dtos.requests.*;
import com.example.authservice.dtos.responses.*;
import com.example.authservice.exceptions.OurException;
import com.example.authservice.services.JwtService;
import com.example.authservice.services.OtpService;
import com.example.authservice.services.feigns.UserFeignClient;
import com.example.authservice.services.rabbitmqs.producers.AuthProducer;
import com.example.rediscommon.services.RateLimiterService;
import com.example.rediscommon.services.ApiResponseHandler;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthApi extends BaseApi {

    private JwtService jwtService;
    private UserFeignClient userFeignClient;
    private AuthProducer authProducer;
    private OtpService otpService;
    private final ObjectMapper objectMapper;
    private final RateLimiterService rateLimiterService;
    private final ApiResponseHandler<Response> responseHandler;
    private final CacheKeyBuilder cacheKeys;
    private static final int ACCESS_TOKEN_EXPIRATION_SECONDS = 5 * 60 * 60; // 5 hours
    private static final int REFRESH_TOKEN_EXPIRATION_SECONDS = 7 * 24 * 60 * 60; // 7 days

    @Value("${DEV_MODE}")
    private String devMode;

    public AuthApi(JwtService jwtService, UserFeignClient userFeignClient, AuthProducer authProducer,
            OtpService otpService, RateLimiterService rateLimiterService,
            ApiResponseHandler<Response> responseHandler) {
        this.jwtService = jwtService;
        this.userFeignClient = userFeignClient;
        this.authProducer = authProducer;
        this.otpService = otpService;
        this.objectMapper = new ObjectMapper();
        this.rateLimiterService = rateLimiterService;
        this.responseHandler = responseHandler;
        this.cacheKeys = CacheKeyBuilder.forService("auth");
    }

    public Response login(String dataJson, HttpServletResponse httpServletResponse) {
        return responseHandler.executeWithoutRateLimit(
                () -> {
                    LoginRequest request;
                    try {
                        request = objectMapper.readValue(dataJson, LoginRequest.class);
                    } catch (Exception e) {
                        throw new OurException("Invalid request format", 400);
                    }
                    String identifier = request.getIdentifier();
                    String password = request.getPassword();

                    String rateLimitKey = "auth:login:" + identifier;
                    if (!rateLimiterService.isAllowed(rateLimitKey, 7, 60)) {
                        throw new OurException("Rate limit exceeded. Please try again later.", 429);
                    }

                    if (identifier == null || identifier.trim().isEmpty()) {
                        throw new OurException("Identifier is required", 404);
                    }

                    if (password == null || password.trim().isEmpty()) {
                        throw new OurException("Password is required", 400);
                    }

                    UserDto user = userFeignClient.authenticateUser(identifier, password).getUser();

                    if (user == null) {
                        throw new OurException("Invalid credentials", 404);
                    }

                    boolean isPending = user.getStatus().equals("pending");
                    if (isPending) {
                        throw new OurException("Account not verified. Please verify your account before logging in.",
                                403);
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

                    Cookie accessTokenCookie = handleCreateCookie("access_token", accessToken,
                            ACCESS_TOKEN_EXPIRATION_SECONDS);
                    Cookie refreshTokenCookie = handleCreateCookie("refresh_token", refreshToken,
                            REFRESH_TOKEN_EXPIRATION_SECONDS);
                    httpServletResponse.addCookie(accessTokenCookie);
                    httpServletResponse.addCookie(refreshTokenCookie);
                    httpServletResponse.setHeader("X-Access-Token", accessToken);
                    httpServletResponse.setHeader("X-Refresh-Token", refreshToken);

                    return user;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "Login successful",
                200);
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

    public Response validateToken(String token, String username) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("validateToken", username),
                7,
                () -> {
                    boolean isValid = jwtService.validateToken(token, username);
                    Map<String, Object> additionalData = new HashMap<>();
                    additionalData.put("valid", isValid);
                    return additionalData;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setAdditionalData,
                "Token validation successful",
                200);
    }

    public Response register(String dataJson) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethod("register"),
                7,
                () -> userFeignClient.registerUser(dataJson).getUser(),
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "Registration successful",
                200);
    }

    public Response verifyOTP(String identifier, String dataJson) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("verifyOTP", identifier),
                7,
                () -> {
                    if (identifier == null || identifier.trim().isEmpty()) {
                        throw new OurException("Identifier is required", 400);
                    }
                    if (dataJson == null || dataJson.trim().isEmpty()) {
                        throw new OurException("Request data is required", 400);
                    }

                    UserDto user = userFeignClient.findUserByIdentifier(identifier).getUser();
                    if (user == null) {
                        throw new OurException("User not found.", 404);
                    }

                    String email = user.getEmail();
                    VerifyOtpRequest request;
                    try {
                        request = objectMapper.readValue(dataJson, VerifyOtpRequest.class);
                    } catch (Exception e) {
                        throw new OurException("Invalid request format", 400);
                    }
                    String otp = request.getOtp();

                    if (otp == null || otp.trim().isEmpty()) {
                        throw new OurException("OTP is required", 400);
                    }

                    boolean isValid = otpService.validateOtp(email, otp);
                    if (!isValid) {
                        throw new OurException("Invalid OTP.");
                    }

                    if (request.getIsActivation() != null && request.getIsActivation()) {
                        userFeignClient.activateUser(email);
                    }

                    return null;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                (r, data) -> {
                },
                "Otp verified successfully!",
                200);
    }

    public Response sendOTP(String identifier) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("sendOTP", identifier),
                7,
                () -> {
                    UserDto user = userFeignClient.findUserByIdentifier(identifier).getUser();
                    if (user == null) {
                        throw new OurException("User not found", 404);
                    }
                    String email = user.getEmail();
                    String otp = otpService.generateOtp(email);
                    authProducer.sendMailActivation(email, otp);
                    return null;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                (r, data) -> {
                },
                "OTP is sent!",
                200);
    }

    public Response changePassword(String identifier, String dataJson) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("changePassword", identifier),
                7,
                () -> {
                    ChangePasswordRequest request;
                    try {
                        request = objectMapper.readValue(dataJson, ChangePasswordRequest.class);
                    } catch (Exception e) {
                        throw new OurException("Invalid request format", 400);
                    }
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

                    UserDto user = userFeignClient.findUserByIdentifier(identifier).getUser();
                    if (user == null) {
                        throw new OurException("User not found", 404);
                    }

                    ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(currentPassword,
                            newPassword,
                            confirmPassword);
                    String requestJson;
                    try {
                        requestJson = objectMapper.writeValueAsString(changePasswordRequest);
                    } catch (Exception e) {
                        throw new OurException("Failed to process request", 500);
                    }
                    UserDto updatedUser = userFeignClient.changePassword(identifier, requestJson).getUser();
                    if (updatedUser == null) {
                        throw new OurException("Invalid current password.");
                    }

                    return null;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                (r, data) -> {
                },
                "Password changed successfully!",
                200);
    }

    public Response resetPassword(String email) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("resetPassword", email),
                7,
                () -> {
                    if (email == null || email.trim().isEmpty()) {
                        throw new OurException("Email is required", 400);
                    }
                    Response resetResponse = userFeignClient.resetPassword(email);
                    String newPassword = (String) resetResponse.getAdditionalData().get("newPassword");
                    authProducer.sendMailResetPassword(email, newPassword);
                    return null;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                (r, data) -> {
                },
                "Password reset email sent successfully!",
                200);
    }

    public Response forgotPassword(String identifier, String dataJson) {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethodWithParam("forgotPassword", identifier),
                7,
                () -> {
                    UserDto user = userFeignClient.findUserByIdentifier(identifier).getUser();
                    if (user == null) {
                        throw new OurException("User not found", 404);
                    }

                    ForgotPasswordRequest request;
                    try {
                        request = objectMapper.readValue(dataJson, ForgotPasswordRequest.class);
                    } catch (Exception e) {
                        throw new OurException("Invalid request format", 400);
                    }
                    String email = user.getEmail();
                    String newPassword = request.getPassword();
                    String rePassword = request.getConfirmPassword();

                    if (!newPassword.equals(rePassword)) {
                        throw new OurException("Password does not match.");
                    }

                    ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest(newPassword, rePassword);
                    String requestJson;
                    try {
                        requestJson = objectMapper.writeValueAsString(forgotPasswordRequest);
                    } catch (Exception e) {
                        throw new OurException("Failed to process request", 500);
                    }
                    userFeignClient.forgotPassword(email, requestJson);

                    return null;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                (r, data) -> {
                },
                "Password updated successfully!",
                200);
    }

    public Response refreshToken(
            RefreshTokenRequest request,
            String authHeader,
            HttpServletRequest httpRequest,
            HttpServletResponse httpServletResponse) {

        return responseHandler.executeWithoutRateLimit(
                () -> {
                    String rateLimitKey = "auth:refreshToken";
                    if (!rateLimiterService.isAllowed(rateLimitKey, 7, 60)) {
                        throw new OurException("Rate limit exceeded. Please try again later.", 429);
                    }

                    String refreshToken = null;

                    // Priority 1: Try from cookie (recommended for security)
                    if (httpRequest.getCookies() != null) {
                        for (Cookie cookie : httpRequest.getCookies()) {
                            if ("refresh_token".equals(cookie.getName()) && cookie.getValue() != null
                                    && !cookie.getValue().isEmpty()) {
                                refreshToken = cookie.getValue();
                                break;
                            }
                        }
                    }

                    // Priority 2: Try from request body
                    if (refreshToken == null && request != null && request.getRefreshToken() != null
                            && !request.getRefreshToken().isEmpty()) {
                        refreshToken = request.getRefreshToken();
                    }

                    // Priority 3: Try from Authorization header
                    if (refreshToken == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                        refreshToken = authHeader.substring(7);
                    }

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
                    UserDto user = userFeignClient.findUserByEmail(email).getUser();

                    if (user == null) {
                        throw new OurException("User not found", 404);
                    }

                    // Generate new access token
                    String newAccessToken = jwtService.generateAccessToken(userId, email, user.getRole(),
                            user.getUsername());

                    // Set new access token in cookie and header
                    Cookie accessTokenCookie = handleCreateCookie("access_token", newAccessToken,
                            ACCESS_TOKEN_EXPIRATION_SECONDS);
                    httpServletResponse.addCookie(accessTokenCookie);
                    httpServletResponse.setHeader("X-Access-Token", newAccessToken);

                    return user;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setUser,
                "Token refreshed successfully!",
                200);
    }

    public Response logout(HttpServletResponse httpServletResponse) {
        return responseHandler.executeWithoutRateLimit(
                () -> {
                    String rateLimitKey = "auth:logout";
                    if (!rateLimiterService.isAllowed(rateLimitKey, 7, 60)) {
                        throw new OurException("Rate limit exceeded. Please try again later.", 429);
                    }

                    Cookie accessTokenCookie = handleCreateCookie("access_token", "", 0);
                    Cookie refreshTokenCookie = handleCreateCookie("refresh_token", "", 0);
                    httpServletResponse.addCookie(accessTokenCookie);
                    httpServletResponse.addCookie(refreshTokenCookie);
                    httpServletResponse.setHeader("X-Access-Token", "");
                    httpServletResponse.setHeader("X-Refresh-Token", "");

                    return null;
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                (r, data) -> {
                },
                "Logged out successfully",
                200);
    }
}