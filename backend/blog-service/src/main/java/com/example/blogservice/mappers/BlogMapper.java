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
                .authorId(blog.getAuthorId())
                .title(blog.getTitle())
                .category(blog.getCategory() != null ? blog.getCategory().name() : null)
                .description(blog.getDescription())
                .content(blog.getContent())
                .thumbnailUrl(blog.getThumbnailUrl())
                .thumbnailPublicId(blog.getThumbnailPublicId())
                .isVisibility(blog.getIsVisibility())
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
        blog.setAuthorId(dto.getAuthorId());
        blog.setTitle(dto.getTitle());
        blog.setCategory(dto.getCategory() != null ? Blog.Category.valueOf(dto.getCategory()) : null);
        blog.setDescription(dto.getDescription());
        blog.setContent(dto.getContent());
        blog.setThumbnailUrl(dto.getThumbnailUrl());
        blog.setThumbnailPublicId(dto.getThumbnailPublicId());
        blog.setIsVisibility(dto.getIsVisibility());
        blog.setCreatedAt(dto.getCreatedAt());
        blog.setUpdatedAt(dto.getUpdatedAt());
        
        return blog;
    }
}