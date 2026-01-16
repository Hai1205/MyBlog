package com.example.blogservice.dtos.requests;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentRequest {
    private String content;
}
