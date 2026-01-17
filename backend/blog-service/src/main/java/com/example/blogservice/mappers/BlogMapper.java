package com.example.blogservice.mappers;

import com.example.blogservice.dtos.BlogDto;
import com.example.blogservice.entities.Blog;

import org.springframework.stereotype.Component;

@Component
public class BlogMapper {

    /**
     * Convert Blog entity to DTO
     */
    public BlogDto toDto(Blog blog) {
        if (blog == null)
            return null;
        
        return BlogDto.builder()
                .id(blog.getId())
                .author(null) // TODO: Fetch author info from user service
                .title(blog.getTitle())
                .category(blog.getCategory() != null ? blog.getCategory().name() : null)
                .description(blog.getDescription())
                .content(blog.getContent())
                .thumbnailUrl(blog.getImageUrl())
                .imagePublicId(blog.getImagePublicId())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }

    /**
     * Convert Blog DTO to entity
     */
    public Blog toEntity(BlogDto dto) {
        if (dto == null)
            return null;
            
        Blog blog = new Blog();
        blog.setId(dto.getId());
        blog.setAuthorId(dto.getAuthor() != null ? dto.getAuthor().getId() : null);
        blog.setTitle(dto.getTitle());
        blog.setCategory(dto.getCategory() != null ? Blog.Category.valueOf(dto.getCategory()) : null);
        blog.setDescription(dto.getDescription());
        blog.setContent(dto.getContent());
        blog.setImageUrl(dto.getImageUrl());
        blog.setImagePublicId(dto.getImagePublicId());
        blog.setCreatedAt(dto.getCreatedAt());
        blog.setUpdatedAt(dto.getUpdatedAt());
        
        return blog;
    }
}