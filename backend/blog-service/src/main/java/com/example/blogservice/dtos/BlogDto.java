package com.example.blogservice.dtos;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogDto {
    private UUID id;
    private UserDto author;
    private String title;
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;
    private String imagePublicId;

    private Instant createdAt;
    private Instant updatedAt;
}
