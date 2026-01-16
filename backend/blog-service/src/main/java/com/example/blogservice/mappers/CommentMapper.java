package com.example.blogservice.mappers;

import com.example.blogservice.dtos.CommentDto;
import com.example.blogservice.entities.Comment;

import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    /**
     * Convert Comment entity to DTO
     */
    public CommentDto toDto(Comment comment) {
        if (comment == null)
            return null;

        return CommentDto.builder()
                .id(comment.getId())
                .blogId(comment.getBlogId())
                .user(null) // TODO: Fetch user info from user service
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    /**
     * Convert Comment DTO to entity
     */
    public Comment toEntity(CommentDto dto) {
        if (dto == null)
            return null;

        Comment comment = new Comment();
        comment.setId(dto.getId());
        comment.setBlogId(dto.getBlogId());
        comment.setUserId(dto.getUser() != null ? dto.getUser().getId() : null);
        comment.setContent(dto.getContent());
        comment.setCreatedAt(dto.getCreatedAt());
        comment.setUpdatedAt(dto.getUpdatedAt());

        return comment;
    }
}
