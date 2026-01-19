package com.example.userservice.dtos.requests.user;

import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String location;
    private String birth;
    private String summary;
    private MultipartFile avatar;
    private String role;
    private String status;
    private String instagram;
    private String linkedin;
    private String facebook;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime planExpiration;
}
