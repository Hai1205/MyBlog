package com.example.statsservice.dtos.responses.views;

import java.time.Instant;
import java.util.UUID;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogView {
    private UUID id;
    private UUID authorId;
    private String title;
    private String category;
    private String thumbnailUrl;
    private Boolean isVisibility;
    private Instant createdAt;
}
