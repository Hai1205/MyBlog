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
    private UserDto user;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
}
