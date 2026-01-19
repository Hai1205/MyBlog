package com.example.userservice.dtos;

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
    private String phone;
    private String birth;
    private String summary;
    private String status;
    private String role;
    private String facebook;
    private String linkedin;
    private String instagram;

    private String avatarUrl;
    private String avatarPublicId;

    // Constructor with all fields for MapStruct
    @Builder
    public UserDto(UUID id, String username, String password, String email,
            String phone, String birth, String summary,
            String status, String role, String facebook, String linkedin, String instagram,
            String avatarUrl, String avatarPublicId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.birth = birth;
        this.summary = summary;
        this.status = status;
        this.role = role;
        this.facebook = facebook;
        this.linkedin = linkedin;
        this.instagram = instagram;
        this.avatarUrl = avatarUrl;
        this.avatarPublicId = avatarPublicId;
    }

    // Basic constructor
    public UserDto(UUID id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
