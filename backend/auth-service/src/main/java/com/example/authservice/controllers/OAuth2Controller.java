package com.example.authservice.controllers;

import com.example.authservice.dtos.requests.*;
import com.example.authservice.services.OAuth2LoginService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);

    @Autowired
    private OAuth2LoginService oauth2LoginService;

    /**
     * Endpoint để bắt đầu quá trình đăng nhập Google OAuth2
     * GET /oauth2/authorize/google
     */
    @GetMapping("/authorize/google")
    public void authorizeGoogle(HttpServletResponse response) throws IOException {
        logger.info("Initiating Google OAuth2 authorization");
        response.sendRedirect("/oauth2/authorization/google");
    }

    /**
     * Endpoint để bắt đầu quá trình đăng nhập Facebook OAuth2
     * GET /oauth2/authorize/facebook
     */
    @GetMapping("/authorize/facebook")
    public void authorizeFacebook(HttpServletResponse response) throws IOException {
        logger.info("Initiating Facebook OAuth2 authorization");
        response.sendRedirect("/oauth2/authorization/facebook");
    }

    /**
     * Endpoint để bắt đầu quá trình đăng nhập GitHub OAuth2
     * GET /oauth2/authorize/github
     */
    @GetMapping("/authorize/github")
    public void authorizeGitHub(HttpServletResponse response) throws IOException {
        logger.info("Initiating GitHub OAuth2 authorization");
        response.sendRedirect("/oauth2/authorization/github");
    }

    /**
     * Endpoint callback sau khi đăng nhập Google thành công
     * Sẽ được gọi bởi Spring Security sau khi user authorize
     */
    @GetMapping("/callback/google")
    public ResponseEntity<Map<String, Object>> callbackGoogle(
            @AuthenticationPrincipal OAuth2User oauth2User) {
        return processOAuth2Callback(oauth2User, "google");
    }

    /**
     * Endpoint callback sau khi đăng nhập Facebook thành công
     */
    @GetMapping("/callback/facebook")
    public ResponseEntity<Map<String, Object>> callbackFacebook(
            @AuthenticationPrincipal OAuth2User oauth2User) {
        return processOAuth2Callback(oauth2User, "facebook");
    }

    /**
     * Endpoint callback sau khi đăng nhập GitHub thành công
     */
    @GetMapping("/callback/github")
    public ResponseEntity<Map<String, Object>> callbackGitHub(
            @AuthenticationPrincipal OAuth2User oauth2User) {
        return processOAuth2Callback(oauth2User, "github");
    }

    /**
     * Xử lý callback chung cho tất cả providers
     */
    private ResponseEntity<Map<String, Object>> processOAuth2Callback(OAuth2User oauth2User, String provider) {
        logger.info("{} OAuth2 callback received", provider);

        try {
            // Log thông tin user để debug
            oauth2LoginService.logUserInfo(oauth2User, provider);

            // Xử lý đăng nhập và tạo JWT token
            String token = oauth2LoginService.processOAuth2Login(oauth2User, provider);

            // Tạo response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", provider + " login successful");
            response.put("token", token);
            response.put("provider", provider);
            response.put("userInfo", extractBasicUserInfo(oauth2User, provider));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing {} OAuth2 callback", provider, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", provider + " login failed: " + e.getMessage());
            errorResponse.put("provider", provider);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Alternative callback endpoint với redirect cho Google
     */
    @GetMapping("/login/oauth2/code/google")
    public RedirectView handleGoogleCallback(@AuthenticationPrincipal OAuth2User oauth2User) {
        return handleOAuth2Redirect(oauth2User, "google");
    }

    /**
     * Alternative callback endpoint với redirect cho Facebook
     */
    @GetMapping("/login/oauth2/code/facebook")
    public RedirectView handleFacebookCallback(@AuthenticationPrincipal OAuth2User oauth2User) {
        return handleOAuth2Redirect(oauth2User, "facebook");
    }

    /**
     * Alternative callback endpoint với redirect cho GitHub
     */
    @GetMapping("/login/oauth2/code/github")
    public RedirectView handleGitHubCallback(@AuthenticationPrincipal OAuth2User oauth2User) {
        return handleOAuth2Redirect(oauth2User, "github");
    }

    /**
     * Xử lý redirect callback chung
     */
    private RedirectView handleOAuth2Redirect(OAuth2User oauth2User, String provider) {
        logger.info("Handling {} OAuth2 redirect callback", provider);

        try {
            // Xử lý đăng nhập
            String token = oauth2LoginService.processOAuth2Login(oauth2User, provider);

            // Redirect về frontend với token
            String redirectUrl = oauth2LoginService.createSuccessRedirectUrl(token);
            return new RedirectView(redirectUrl);

        } catch (Exception e) {
            logger.error("Error in {} OAuth2 redirect callback", provider, e);
            // Redirect về error page
            return new RedirectView(
                    "http://localhost:3000/auth/error?message=" + e.getMessage() + "&provider=" + provider);
        }
    }

    /**
     * Test endpoint để kiểm tra OAuth2 configuration
     */
    @GetMapping("/test/google")
    public ResponseEntity<Map<String, Object>> testGoogleConfig() {
        return createTestResponse("google");
    }

    /**
     * Test endpoint để kiểm tra Facebook OAuth2 configuration
     */
    @GetMapping("/test/facebook")
    public ResponseEntity<Map<String, Object>> testFacebookConfig() {
        return createTestResponse("facebook");
    }

    /**
     * Test endpoint để kiểm tra GitHub OAuth2 configuration
     */
    @GetMapping("/test/github")
    public ResponseEntity<Map<String, Object>> testGitHubConfig() {
        return createTestResponse("github");
    }

    /**
     * Test endpoint để kiểm tra tất cả OAuth2 providers
     */
    @GetMapping("/test/all")
    public ResponseEntity<Map<String, Object>> testAllProviders() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All OAuth2 providers configuration");
        response.put("providers", new String[] { "google", "facebook", "github" });
        response.put("status", "ready");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("google_auth", "/oauth2/authorize/google");
        endpoints.put("facebook_auth", "/oauth2/authorize/facebook");
        endpoints.put("github_auth", "/oauth2/authorize/github");
        response.put("endpoints", endpoints);

        return ResponseEntity.ok(response);
    }

    /**
     * Tạo test response chung cho providers
     */
    private ResponseEntity<Map<String, Object>> createTestResponse(String provider) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", provider + " OAuth2 configuration is ready");
        response.put("authUrl", "/oauth2/authorize/" + provider);
        response.put("callbackUrl", "/oauth2/callback/" + provider);
        response.put("provider", provider);
        response.put("status", "ready");

        return ResponseEntity.ok(response);
    }

    /**
     * Error endpoint cho OAuth2 failures
     */
    @GetMapping("/error")
    public ResponseEntity<Map<String, Object>> oauthError(
            @ModelAttribute OAuthErrorRequest oAuthErrorRequest) {

        logger.error("OAuth2 error occurred: {}, description: {}", oAuthErrorRequest.getError(), oAuthErrorRequest.getErrorDescription());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", oAuthErrorRequest.getError() != null ? oAuthErrorRequest.getError() : "oauth2_error");
        errorResponse.put("message", oAuthErrorRequest.getErrorDescription() != null ? oAuthErrorRequest.getErrorDescription() : "OAuth2 authentication failed");

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * General success endpoint để xử lý tất cả OAuth2 success callbacks
     */
    @GetMapping("/callback/success")
    public ResponseEntity<Map<String, Object>> generalSuccessCallback(
            @AuthenticationPrincipal OAuth2User oauth2User,
            HttpServletRequest request) {

        // Determine provider from referrer hoặc session
        String provider = determineProviderFromRequest(request, oauth2User);

        logger.info("General OAuth2 success callback for provider: {}", provider);

        return processOAuth2Callback(oauth2User, provider);
    }

    /**
     * Xác định provider từ request hoặc OAuth2User attributes
     */
    private String determineProviderFromRequest(HttpServletRequest request, OAuth2User oauth2User) {
        // Try to get from request parameter first
        String provider = request.getParameter("provider");
        if (provider != null) {
            return provider;
        }

        // Try to determine from OAuth2User attributes
        Map<String, Object> attributes = oauth2User.getAttributes();

        if (attributes.containsKey("sub")) {
            return "google";
        } else if (attributes.containsKey("login")) {
            return "github";
        } else if (attributes.containsKey("id") && !attributes.containsKey("sub")) {
            return "facebook";
        }

        // Default fallback
        return "unknown";
    }

    /**
     * Trích xuất thông tin cơ bản của user theo provider
     */
    private Map<String, Object> extractBasicUserInfo(OAuth2User oauth2User, String provider) {
        Map<String, Object> userInfo = new HashMap<>();
        Map<String, Object> attributes = oauth2User.getAttributes();

        switch (provider.toLowerCase()) {
            case "google":
                userInfo.put("email", attributes.get("email"));
                userInfo.put("name", attributes.get("name"));
                userInfo.put("picture", attributes.get("picture"));
                userInfo.put("id", attributes.get("sub"));
                break;

            case "facebook":
                userInfo.put("email", attributes.get("email"));
                userInfo.put("name", attributes.get("name"));
                // Facebook picture structure: {"data":{"url":"..."}}
                Object picture = attributes.get("picture");
                if (picture instanceof Map<?, ?>) {
                    // Type-safe approach for nested map access
                    @SuppressWarnings("unchecked")
                    Map<String, Object> pictureMap = (Map<String, Object>) picture;
                    Object data = pictureMap.get("data");
                    if (data instanceof Map<?, ?>) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> dataMap = (Map<String, Object>) data;
                        userInfo.put("picture", dataMap.get("url"));
                    }
                }
                userInfo.put("id", attributes.get("id"));
                break;

            case "github":
                userInfo.put("email", attributes.get("email"));
                userInfo.put("name", attributes.get("name"));
                userInfo.put("picture", attributes.get("avatar_url"));
                userInfo.put("id", attributes.get("id"));
                userInfo.put("username", attributes.get("login"));
                break;

            default:
                userInfo.put("raw_attributes", attributes);
        }

        return userInfo;
    }
}