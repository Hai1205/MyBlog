package com.example.blogservice.dtos.requests;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {
    private String content;
}
