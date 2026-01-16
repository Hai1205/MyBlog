package com.example.aiservice.dtos.requests;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    private String title;
    private String description;
    private String content;
}
