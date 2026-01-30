package com.example.blogservice.dtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.blogservice.dtos.responses.views.UserView;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogDto {
    private UUID id;
    private UUID authorId;
    private UserView author;
    private List<CommentDto> comments;
    private String title;
    private String category;
    private String description;
    private String content;
    private String thumbnailUrl;
    private String thumbnailPublicId;

    private Boolean isVisibility;
    private Boolean isSaved;

    private Instant createdAt;
    private Instant updatedAt;
}
