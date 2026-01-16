package com.example.authservice.services;

import com.example.authservice.dtos.OAuth2UserDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OAuth2LoginService {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginService.class);

    private JwtService jwtUtil;

    public OAuth2LoginService(JwtService jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Xử lý đăng nhập OAuth2 thành công
     * 
     * @param oauth2User thông tin user từ OAuth2 provider
     * @param provider   tên provider (google, facebook, github)
     * @return JWT token
     */
    public String processOAuth2Login(OAuth2User oauth2User, String provider) {
        try {
            // Lấy thông tin user từ OAuth2User
            String email = extractEmail(oauth2User, provider);
            String name = extractName(oauth2User, provider);
            String providerId = extractProviderId(oauth2User, provider);
            String avatarUrl = extractAvatarUrl(oauth2User, provider);

            logger.info("Processing OAuth2 login for provider: {}, email: {}, name: {}, avatarUrl: {}",
                    provider, email, name, avatarUrl);

            // Tạo OAuth2UserDto để gửi đến user-service
            OAuth2UserDto oauth2UserDto = createOAuth2UserDto(oauth2User, provider);

            // Xử lý user trong user-service thông qua RabbitMQ (check exists,
            // create/update)
            // Map<String, Object> userResult =
            // userService.processOAuth2User(oauth2UserDto);
            Map<String, Object> userResult = new HashMap<>(); // Giả lập user-service response

            if (userResult == null) {
                throw new RuntimeException("Failed to process user in user-service");
            }

            // Lấy user ID để tạo JWT
            Object userIdObj = userResult.get("id");
            String userId = userIdObj != null ? userIdObj.toString() : providerId;

            // Lấy username và role từ kết quả
            String username = userResult.get("username") != null ? userResult.get("username").toString()
                    : oauth2UserDto.getUsername();

            String role = userResult.get("role") != null ? userResult.get("role").toString() : "ROLE_USER";

            // Tạo JWT token với thông tin user
            String token = jwtUtil.generateToken(userId, username, role);

            logger.info("Successfully generated JWT token for OAuth2 user: {}, userId: {}", email, userId);
            return token;

        } catch (Exception e) {
            logger.error("Failed to process OAuth2 login for provider: {}", provider, e);
            throw new RuntimeException("OAuth2 login processing failed", e);
        }
    }

    /**
     * Tạo OAuth2UserDto từ OAuth2User
     */
    private OAuth2UserDto createOAuth2UserDto(OAuth2User oauth2User, String provider) {
        String email = extractEmail(oauth2User, provider);
        String name = extractName(oauth2User, provider);
        String providerId = extractProviderId(oauth2User, provider);
        String avatarUrl = extractAvatarUrl(oauth2User, provider);

        // Tách first name và last name
        String firstName = extractFirstName(name);
        String lastName = extractLastName(name);

        // Tạo username từ email hoặc provider info
        String username = generateUsername(oauth2User, provider);

        OAuth2UserDto dto = new OAuth2UserDto(
                email, name, firstName, lastName,
                provider, providerId, avatarUrl, username);

        return dto;
    }

    /**
     * Tạo username từ OAuth2 info
     */
    private String generateUsername(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        switch (provider.toLowerCase()) {
            case "google":
                String googleEmail = (String) attributes.get("email");
                return googleEmail != null ? googleEmail.split("@")[0] : "google_user";
            case "facebook":
                return "fb_" + attributes.get("id");
            case "github":
                return (String) attributes.get("login");
            default:
                return provider + "_user";
        }
    }

    /**
     * Extract first name từ full name
     */
    private String extractFirstName(String fullname) {
        if (fullname == null || fullname.trim().isEmpty())
            return null;

        String[] parts = fullname.trim().split("\\s+");
        return parts[0];
    }

    /**
     * Extract last name từ full name
     */
    private String extractLastName(String fullname) {
        if (fullname == null || fullname.trim().isEmpty())
            return null;

        String[] parts = fullname.trim().split("\\s+");
        if (parts.length > 1) {
            return String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        }
        return null;
    }

    /**
     * Extract avatar URL từ OAuth2User
     */
    private String extractAvatarUrl(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("picture");
            case "facebook":
                // Facebook picture structure: {"data":{"url":"..."}}
                Object picture = attributes.get("picture");
                if (picture instanceof Map) {
                    Map<String, Object> pictureMap = (Map<String, Object>) picture;
                    Object data = pictureMap.get("data");
                    if (data instanceof Map) {
                        Map<String, Object> dataMap = (Map<String, Object>) data;
                        return (String) dataMap.get("url");
                    }
                }
                return null;
            case "github":
                return (String) attributes.get("avatar_url");
            default:
                return null;
        }
    }

    /**
     * Trích xuất email từ OAuth2User dựa trên provider
     */
    private String extractEmail(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("email");
            case "facebook":
                return (String) attributes.get("email");
            case "github":
                // GitHub có thể có email null nếu private
                String email = (String) attributes.get("email");
                if (email == null) {
                    // Fallback to login@github.local
                    String login = (String) attributes.get("login");
                    return login != null ? login + "@github.local" : "unknown@github.local";
                }
                return email;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    /**
     * Trích xuất tên từ OAuth2User dựa trên provider
     */
    private String extractName(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("name");
            case "facebook":
                return (String) attributes.get("name");
            case "github":
                String name = (String) attributes.get("name");
                // GitHub có thể có name null
                return name != null ? name : (String) attributes.get("login");
            default:
                return "Unknown User";
        }
    }

    /**
     * Trích xuất provider ID từ OAuth2User
     */
    private String extractProviderId(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("sub");
            case "facebook":
                return (String) attributes.get("id");
            case "github":
                Object id = attributes.get("id");
                return id != null ? String.valueOf(id) : "unknown";
            default:
                return "unknown";
        }
    }

    /**
     * Tạo redirect URL sau khi đăng nhập thành công
     */
    public String createSuccessRedirectUrl(String token) {
        // Trong thực tế, bạn sẽ redirect về frontend với token
        return "http://localhost:3000/auth/callback?token=" + token;
    }

    /**
     * Tạo redirect URL với thông tin provider
     */
    public String createSuccessRedirectUrl(String token, String provider) {
        return "http://localhost:3000/auth/callback?token=" + token + "&provider=" + provider;
    }

    /**
     * Log thông tin user từ OAuth2 để debug
     */
    public void logUserInfo(OAuth2User oauth2User, String provider) {
        logger.debug("OAuth2 User attributes for provider {}: {}", provider, oauth2User.getAttributes());
        logger.debug("OAuth2 User authorities: {}", oauth2User.getAuthorities());

        // Log key information for each provider
        Map<String, Object> attributes = oauth2User.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                logger.info("Google user - Email: {}, Name: {}, Sub: {}",
                        attributes.get("email"), attributes.get("name"), attributes.get("sub"));
                break;
            case "facebook":
                logger.info("Facebook user - Email: {}, Name: {}, ID: {}",
                        attributes.get("email"), attributes.get("name"), attributes.get("id"));
                break;
            case "github":
                logger.info("GitHub user - Email: {}, Name: {}, Login: {}, ID: {}",
                        attributes.get("email"), attributes.get("name"),
                        attributes.get("login"), attributes.get("id"));
                break;
        }
    }

    /**
     * Validate provider được hỗ trợ
     */
    public boolean isProviderSupported(String provider) {
        return provider != null &&
                (provider.equalsIgnoreCase("google") ||
                        provider.equalsIgnoreCase("facebook") ||
                        provider.equalsIgnoreCase("github"));
    }
}