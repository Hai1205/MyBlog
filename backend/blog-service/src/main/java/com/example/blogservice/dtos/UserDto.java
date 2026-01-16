package com.example.blogservice.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
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
    private String fullname;
    private String phone;
    private String birth;
    private String summary;
    private String status;
    private String role;

    private String avatarUrl;
    private String avatarPublicId;

    // Constructor with all fields for MapStruct
    @Builder
    public UserDto(UUID id, String username, String password, String email,
            String fullname, String phone, String birth, String summary,
            String status, String role, 
            String avatarUrl, String avatarPublicId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullname = fullname;
        this.phone = phone;
        this.birth = birth;
        this.summary = summary;
        this.status = status;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.avatarPublicId = avatarPublicId;
    }

    // Basic constructor
    public UserDto(UUID id, String username, String email, String fullname) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullname = fullname;
    }
}
