package com.example.blogservice.mappers;

import com.example.blogservice.dtos.BlogDto;
import com.example.blogservice.dtos.responses.views.BlogView;
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

        BlogDto dto = new BlogDto();
        dto.setId(blog.getId());
        dto.setAuthorId(blog.getAuthorId());
        dto.setTitle(blog.getTitle());

        if (blog.getCategory() != null) {
            dto.setCategory(blog.getCategory().name());
        } else {
            dto.setCategory(Blog.Category.technology.name());
        }

        dto.setDescription(blog.getDescription());
        dto.setContent(blog.getContent());
        dto.setThumbnailUrl(blog.getThumbnailUrl());
        dto.setThumbnailPublicId(blog.getThumbnailPublicId());
        dto.setIsVisibility(blog.getIsVisibility());
        dto.setCreatedAt(blog.getCreatedAt());
        dto.setUpdatedAt(blog.getUpdatedAt());

        return dto;
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

        if (dto.getCategory() != null) {
            blog.setCategory(Blog.Category.valueOf(dto.getCategory()));
        } else {
            blog.setCategory(Blog.Category.technology);
        }

        blog.setDescription(dto.getDescription());
        blog.setContent(dto.getContent());
        blog.setThumbnailUrl(dto.getThumbnailUrl());
        blog.setThumbnailPublicId(dto.getThumbnailPublicId());
        blog.setIsVisibility(dto.getIsVisibility());
        blog.setCreatedAt(dto.getCreatedAt());
        blog.setUpdatedAt(dto.getUpdatedAt());

        return blog;
    }

    /**
     * Convert Blog entity to BlogView
     */
    public BlogView toView(BlogDto dto) {
        if (dto == null)
            return null;

        BlogView view = new BlogView();
        view.setId(dto.getId());
        view.setAuthorId(dto.getAuthorId());
        view.setTitle(dto.getTitle());

        if (dto.getCategory() != null) {
            view.setCategory(dto.getCategory());
        } else {
            view.setCategory(Blog.Category.technology.name());
        }

        view.setThumbnailUrl(dto.getThumbnailUrl());
        view.setIsVisibility(dto.getIsVisibility());
        view.setCreatedAt(dto.getCreatedAt());

        return view;
    }
}