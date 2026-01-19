package com.example.blogservice.dtos;

import java.time.Instant;
import java.util.UUID;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private UUID id;
    private UUID blogId;
    private UUID userId;
    private String username;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
}
