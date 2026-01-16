package com.example.authservice.dtos;

import java.util.UUID;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2UserDto {
    private UUID id;
    private String email;
    private String name;
    private String firstName;
    private String lastName;
    private String provider; // google, facebook, github
    private String providerId; // Provider-specific user ID
    private String avatarUrl; // Profile picture URL
    private String username; // Generated username

    public OAuth2UserDto(String email, String name, String firstName, String lastName,
            String provider, String providerId, String avatarUrl, String username) {
        this.email = email;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.provider = provider;
        this.providerId = providerId;
        this.avatarUrl = avatarUrl;
        this.username = username;
    }
}