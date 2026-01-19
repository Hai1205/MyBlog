package com.example.userservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor // Empty constructor for MapStruct
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    private String birth;

    private String facebook;
    private String linkedin;
    private String instagram;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.pending;

    private String avatarUrl; // Profile picture URL
    private String avatarPublicId; // Cloudinary public ID for avatar deletion

    // Audit fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Full constructor for MapStruct
    @Builder
    public User(UUID id, String username, String password, String email,
            String phone, String birth, String summary,
            UserRole role, UserStatus status,
            String avatarUrl, String avatarPublicId,
            String facebook, String linkedin, String instagram,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.birth = birth;
        this.summary = summary;
        this.role = role;
        this.status = status;
        this.avatarUrl = avatarUrl;
        this.avatarPublicId = avatarPublicId;
        this.facebook = facebook;
        this.linkedin = linkedin;
        this.instagram = instagram;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Basic constructor
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public enum UserRole {
        user,
        admin
    }

    public enum UserStatus {
        active,
        banned,
        pending
    }
}
