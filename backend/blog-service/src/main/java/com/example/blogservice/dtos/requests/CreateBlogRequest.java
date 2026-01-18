package com.example.blogservice.dtos.requests;

import org.springframework.web.multipart.MultipartFile;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlogRequest {
    private String title;
    private String description;
    private String category;
    private String content;
    private MultipartFile thumbnail;
    private Boolean isVisibility;    
}
