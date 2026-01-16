package com.example.authservice.services.apis;

import com.example.authservice.dtos.*;
import com.example.authservice.dtos.requests.*;
import com.example.authservice.dtos.responses.*;
import com.example.authservice.exceptions.OurException;
import com.example.authservice.services.JwtService;
import com.example.authservice.services.OtpService;
import com.example.authservice.services.feigns.UserFeignClient;
import com.example.authservice.services.rabbitmqs.producers.AuthProducer;
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
    private static final int ACCESS_TOKEN_EXPIRATION_SECONDS = 5 * 60 * 60; // 5 hours
    private static final int REFRESH_TOKEN_EXPIRATION_SECONDS = 7 * 24 * 60 * 60; // 7 days

    @Value("${DEV_MODE}")
    private String devMode;

    public AuthApi(JwtService jwtService, UserFeignClient userFeignClient, AuthProducer authProducer,
            OtpService otpService) {
        this.jwtService = jwtService;
        this.userFeignClient = userFeignClient;
        this.authProducer = authProducer;
        this.otpService = otpService;
        this.objectMapper = new ObjectMapper();
    }

    public Response login(String dataJson, HttpServletResponse httpServletResponse) {
        logger.info("Login attempt");
        Response response = new Response();
        LoginRequest request = null;

        try {
            request = objectMapper.readValue(dataJson, LoginRequest.class);
            String identifier = request.getIdentifier();
            String password = request.getPassword();

            if (identifier == null || identifier.trim().isEmpty()) {
                logger.warn("Login failed: Identifier is required");
                throw new OurException("Identifier is required", 404);
            }

            if (password == null || password.trim().isEmpty()) {
                logger.warn("Login failed: Password is required");
                throw new OurException("Password is required", 400);
            }

            UserDto user = userFeignClient.authenticateUser(identifier, password).getUser();

            if (user == null) {
                logger.warn("Login failed: Invalid credentials for username or email: {}", identifier);
                throw new OurException("Invalid credentials", 404);
            }

            boolean isPending = user.getStatus().equals("pending");
            if (isPending) {
                logger.warn("Login blocked: Account not verified for username or email: {}", identifier);
                throw new OurException("Account not verified. Please verify your account before logging in.", 403);
            }

            boolean isBanned = user.getStatus().equals("banned");
            if (isBanned) {
                logger.warn("Login blocked: Account banned for username or email: {}", identifier);
                throw new OurException("Account is banned. Please contact support.", 405);
            }

            String userId = user.getId().toString();
            String email = user.getEmail();
            String username = user.getUsername();
            String role = user.getRole();

            String accessToken = jwtService.generateAccessToken(userId, email, role, username);
            String refreshToken = jwtService.generateRefreshToken(userId, email, username);

            Cookie accessTokenCookie = handleCreateCookie("access_token", accessToken, ACCESS_TOKEN_EXPIRATION_SECONDS);
            Cookie refreshTokenCookie = handleCreateCookie("refresh_token", refreshToken, REFRESH_TOKEN_EXPIRATION_SECONDS);
            httpServletResponse.addCookie(accessTokenCookie);
            httpServletResponse.addCookie(refreshTokenCookie);
            httpServletResponse.setHeader("X-Access-Token", accessToken);
            httpServletResponse.setHeader("X-Refresh-Token", refreshToken);

            response.setMessage("Login successful");
            response.setUser(user);
            logger.info("Login successful for user: {} (ID: {})", email, userId);
            return response;
        } catch (OurException e) {
            logger.error("Login failed with OurException for identifier {}: {}",
                    request != null ? request.getIdentifier() : "unknown", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Login failed with unexpected error for identifier {}",
                    request != null ? request.getIdentifier() : "unknown", e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
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
        }else{
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        }

        return cookie;
    }

    public Response validateToken(String token, String username) {
        logger.debug("Token validation attempt for username: {}", username);
        Response response = new Response();

        try {
            boolean isValid = jwtService.validateToken(token, username);

            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("valid", isValid);

            if (isValid) {
                logger.debug("Token validation successful for username: {}", username);
            } else {
                logger.warn("Token validation failed for username: {}", username);
            }

            response.setMessage("Token validation successful");
            response.setAdditionalData(additionalData);
            return response;
        } catch (OurException e) {
            logger.error("Token validation failed with OurException for username {}: {}", username, e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Token validation failed with unexpected error for username {}", username, e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response register(String dataJson) {
        logger.info("Registration attempt");
        Response response = new Response();

        try {
            // UserDto user = userFeignClient.createUser(dataJson).getUser();
            // UserCreateRequest userCreateRequest = new UserCreateRequest(dataJson);
            UserDto user = userFeignClient.registerUser(dataJson).getUser();

            response.setMessage("Registration successful");
            response.setUser(user);
            logger.info("Registration successful for email: {}", user.getEmail());
            return response;
        } catch (OurException e) {
            logger.error("Registration failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Registration failed with unexpected error: {}", e.getMessage());
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response verifyOTP(String identifier, String dataJson) {
        logger.info("OTP verification attempt for identifier: {}", identifier);
        Response response = new Response();

        try {
            // Validate input parameters
            if (identifier == null || identifier.trim().isEmpty()) {
                logger.warn("OTP verification failed: Identifier is required");
                throw new OurException("Identifier is required", 400);
            }

            if (dataJson == null || dataJson.trim().isEmpty()) {
                logger.warn("OTP verification failed: Request data is required");
                throw new OurException("Request data is required", 400);
            }

            UserDto user = userFeignClient.findUserByIdentifier(identifier).getUser();

            if (user == null) {
                logger.warn("OTP verification failed: User not found for identifier: {}", identifier);
                throw new OurException("User not found.", 404);
            }

            String email = user.getEmail();

            VerifyOtpRequest request = objectMapper.readValue(dataJson, VerifyOtpRequest.class);
            String otp = request.getOtp();

            if (otp == null || otp.trim().isEmpty()) {
                logger.warn("OTP verification failed: OTP is required");
                throw new OurException("OTP is required", 400);
            }

            boolean isValid = otpService.validateOtp(email, otp);

            if (!isValid) {
                logger.warn("OTP verification failed: Invalid OTP for email: {}", email);
                throw new OurException("Invalid OTP.");
            }

            if (request.getIsActivation() != null && request.getIsActivation()) {
                userFeignClient.activateUser(email);
                logger.info("User account activated for email: {}", email);
            }

            response.setMessage("Otp verified successfully!");
            logger.info("OTP verification successful for email: {}", email);
            return response;
        } catch (OurException e) {
            logger.error("OTP verification failed with OurException for identifier {}: {}", identifier, e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("OTP verification failed with unexpected error for identifier {}", identifier, e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response sendOTP(String identifier) {
        logger.info("Sending OTP to identifier: {}", identifier);
        Response response = new Response();

        try {
            UserDto user = userFeignClient.findUserByIdentifier(identifier).getUser();

            if (user == null) {
                logger.warn("OTP send failed: User not found for identifier: {}", identifier);
                throw new OurException("User not found", 404);
            }

            String email = user.getEmail();

            String otp = otpService.generateOtp(email);

            authProducer.sendMailActivation(email, otp);

            response.setMessage("OTP is sent!");
            logger.info("OTP sent successfully to email: {}", email);
            return response;
        } catch (OurException e) {
            logger.error("OTP send failed with OurException for identifier {}: {}", identifier, e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("OTP send failed with unexpected error for identifier {}", identifier, e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response changePassword(String identifier, String dataJson) {
        logger.info("Password change attempt for identifier: {}", identifier);
        Response response = new Response();

        try {
            ChangePasswordRequest request = objectMapper.readValue(dataJson, ChangePasswordRequest.class);
            String currentPassword = request.getCurrentPassword();
            String newPassword = request.getNewPassword();
            String confirmPassword = request.getConfirmPassword();

            if (currentPassword == null || currentPassword.trim().isEmpty() ||
                    newPassword == null || newPassword.trim().isEmpty() ||
                    confirmPassword == null || confirmPassword.trim().isEmpty()) {
                logger.warn("Password change failed: All password fields are required for identifier: {}", identifier);
                throw new OurException("All password fields are required", 400);
            }

            if (!newPassword.equals(confirmPassword)) {
                logger.warn("Password change failed: Password mismatch for identifier: {}", identifier);
                throw new OurException("Password does not match.");
            }

            // Check if user exists
            UserDto user = userFeignClient.findUserByIdentifier(identifier).getUser();
            if (user == null) {
                logger.warn("Password change failed: User not found for identifier: {}", identifier);
                throw new OurException("User not found", 404);
            }

            ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(currentPassword, newPassword, confirmPassword);
            String requestJson = objectMapper.writeValueAsString(changePasswordRequest);
            UserDto updatedUser = userFeignClient.changePassword(identifier, requestJson).getUser();
            if (updatedUser == null) {
                logger.warn("Password change failed: Invalid current password for identifier: {}", identifier);
                throw new OurException("Invalid current password.");
            }

            response.setMessage("Password changed successfully!");
            logger.info("Password changed successfully for identifier: {}", identifier);
            return response;
        } catch (OurException e) {
            logger.error("Password change failed with OurException for identifier {}: {}", identifier, e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Password change failed with unexpected error for identifier {}", identifier, e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response resetPassword(String email) {
        logger.info("Password reset attempt for email: {}", email);
        Response response = new Response();

        try {
            // Validate input parameter
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Password reset failed: Email is required");
                throw new OurException("Email is required", 400);
            }

            Response resetResponse = userFeignClient.resetPassword(email);
            String newPassword = (String) resetResponse.getAdditionalData().get("newPassword");
            
            authProducer.sendMailResetPassword(email, newPassword);

            response.setMessage("Password reset email sent successfully!");
            logger.info("Password reset email sent successfully to: {}", email);
            return response;
        } catch (OurException e) {
            logger.error("Password reset failed with OurException for email {}: {}", email, e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Password reset failed with unexpected error for email {}", email, e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response forgotPassword(String identifier, String dataJson) {
        logger.info("Forgot password attempt for identifier: {}", identifier);
        Response response = new Response();

        try {
            UserDto user = userFeignClient.findUserByIdentifier(identifier).getUser();

            if (user == null) {
                logger.warn("Forgot password failed: User not found for identifier: {}", identifier);
                throw new OurException("User not found", 404);
            }

            ForgotPasswordRequest request = objectMapper.readValue(dataJson, ForgotPasswordRequest.class);
            String email = user.getEmail();
            String newPassword = request.getPassword();
            String rePassword = request.getConfirmPassword();

            if (!newPassword.equals(rePassword)) {
                logger.warn("Forgot password failed: Password mismatch for email: {}", email);
                throw new OurException("Password does not match.");
            }

            ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest(newPassword, rePassword);
            String requestJson = objectMapper.writeValueAsString(forgotPasswordRequest);
            userFeignClient.forgotPassword(email, requestJson);

            response.setMessage("Password updated successfully!");
            logger.info("Password updated successfully via forgot password for email: {}", email);
            return response;
        } catch (OurException e) {
            logger.error("Forgot password failed with OurException for identifier {}: {}", identifier, e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Forgot password failed with unexpected error for identifier {}", identifier, e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }

    public Response refreshToken(
            RefreshTokenRequest request,
            String authHeader,
            HttpServletRequest httpRequest,
            HttpServletResponse httpServletResponse) {

        logger.info("Token refresh attempt");
        Response response = new Response();

        try {
            String refreshToken = null;
            String source = "unknown";

            // Priority 1: Thử lấy từ cookie (recommended for security)
            if (httpRequest.getCookies() != null) {
                for (Cookie cookie : httpRequest.getCookies()) {
                    if ("refresh_token".equals(cookie.getName()) && cookie.getValue() != null
                            && !cookie.getValue().isEmpty()) {
                        refreshToken = cookie.getValue();
                        source = "cookie";
                        break;
                    }
                }
            }

            // Priority 2: Thử lấy refresh token từ request body
            if (refreshToken == null && request != null && request.getRefreshToken() != null
                    && !request.getRefreshToken().isEmpty()) {
                refreshToken = request.getRefreshToken();
                source = "request body";
            }

            // Priority 3: Thử lấy từ Authorization header
            if (refreshToken == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7);
                source = "Authorization header";
            }

            logger.debug("Refresh token source: {}", source);

            if (refreshToken == null || refreshToken.isEmpty()) {
                logger.warn("Token refresh failed: No refresh token provided from any source");
                throw new OurException("Refresh token is required", 400);
            }

            // Validate refresh token
            if (!jwtService.validateRefreshToken(refreshToken)) {
                logger.warn("Token refresh failed: Invalid or expired refresh token from {}", source);
                throw new OurException("Invalid or expired refresh token", 401);
            }

            // Extract user info from refresh token
            String email = jwtService.extractEmail(refreshToken);
            String userId = jwtService.extractUserId(refreshToken);

            if (email == null || userId == null) {
                logger.warn("Token refresh failed: Cannot extract user info from refresh token");
                throw new OurException("Invalid refresh token format", 401);
            }

            // Find user
            UserDto user = userFeignClient.findUserByEmail(email).getUser();

            if (user == null) {
                logger.warn("Token refresh failed: User not found for email: {}", email);
                throw new OurException("User not found", 404);
            }

            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(userId, email, user.getRole(),
                    user.getUsername());

            // Set new access token in cookie and header
            Cookie accessTokenCookie = handleCreateCookie("access_token", newAccessToken, ACCESS_TOKEN_EXPIRATION_SECONDS); 
            httpServletResponse.addCookie(accessTokenCookie);
            httpServletResponse.setHeader("X-Access-Token", newAccessToken);

            response.setMessage("Token refreshed successfully!");
            response.setUser(user);

            logger.info("Token refreshed successfully for user: {} (source: {})", user.getUsername(), source);
            return response;
        } catch (OurException e) {
            logger.error("Token refresh failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Token refresh failed with unexpected error", e);
            e.printStackTrace();
            return buildErrorResponse(500, "Token refresh failed: " + e.getMessage());
        }
    }

    public Response logout(HttpServletResponse httpServletResponse) {
        logger.info("User logout attempt");
        Response response = new Response();

        try {
            Cookie accessTokenCookie = handleCreateCookie("access_token", "", 0);
            Cookie refreshTokenCookie = handleCreateCookie("refresh_token", "", 0);
            httpServletResponse.addCookie(accessTokenCookie);
            httpServletResponse.addCookie(refreshTokenCookie);
            httpServletResponse.setHeader("X-Access-Token", "");
            httpServletResponse.setHeader("X-Refresh-Token", "");

            response.setMessage("Logged out successfully");
            logger.info("User logged out successfully");
            return response;
        } catch (OurException e) {
            logger.error("Logout failed with OurException: {}", e.getMessage());
            return buildErrorResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Logout failed with unexpected error", e);
            e.printStackTrace();
            return buildErrorResponse(500, e.getMessage());
        }
    }
}