package com.example.blogservice.dtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogDto {
    private UUID id;
    private UUID authorId;
    private UserDto author;
    private List<CommentDto> comments;
    private String title;
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String thumbnailUrl;
    private String thumbnailPublicId;

    private Boolean isVisibility;

    private Instant createdAt;
    private Instant updatedAt;
}
