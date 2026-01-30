package com.example.userservice.dtos.responses.views;


import java.time.Instant;
import java.util.UUID;

import lombok.*;

@Data
@NoArgsConstructor // Empty constructor for MapStruct
public class UserView {
    private UUID id;
    private String username;
    private String email;
    private String status;
    private String role;
    private String avatarUrl;
    private Instant createdAt;
}