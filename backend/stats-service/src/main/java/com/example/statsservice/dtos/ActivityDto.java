package com.example.statsservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDto {
    private String id;
    private String type;        // user_registered, cv_created, etc.
    private String description;
    private String timestamp;   // ISO format
    private String userId;
}
