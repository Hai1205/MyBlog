package com.example.authservice.dtos;

import java.util.UUID;

import lombok.*;

@Data
@NoArgsConstructor // Empty constructor for MapStruct
@Builder
public class UserDto {
    private UUID id;
    private String username;
    private String password;
    private String email;
    private String location;
    private String birth;
    private String summary;
    private String status;
    private String role;
    private String facebook;
    private String linkedin;
    private String instagram;

    // OAuth2 Provider Information
    private String oauthProvider;
    private String oauthProviderId;
    private String avatarUrl;
    private String avatarPublicId;
    private boolean isOAuthUser;

    // Constructor with all fields for MapStruct
    @Builder
    public UserDto(UUID id, String username, String password, String email,
            String location, String birth, String summary, String status, String role,
            String facebook, String linkedin, String instagram, String oauthProvider, String oauthProviderId,
            String avatarUrl, String avatarPublicId, boolean isOAuthUser) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.location = location;
        this.birth = birth;
        this.summary = summary;
        this.status = status;
        this.role = role;
        this.facebook = facebook;
        this.linkedin = linkedin;
        this.instagram = instagram;
        this.oauthProvider = oauthProvider;
        this.oauthProviderId = oauthProviderId;
        this.avatarUrl = avatarUrl;
        this.avatarPublicId = avatarPublicId;
        this.isOAuthUser = isOAuthUser;
    }

    // Basic constructor
    public UserDto(UUID id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isOAuthUser = false;
    }

    // Constructor for OAuth2 users
    public UserDto(UUID id, String username, String email,
            String oauthProvider, String oauthProviderId, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.oauthProvider = oauthProvider;
        this.oauthProviderId = oauthProviderId;
        this.avatarUrl = avatarUrl;
        this.isOAuthUser = true;
    }
}