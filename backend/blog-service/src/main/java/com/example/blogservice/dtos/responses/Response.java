package com.example.blogservice.dtos.responses;

import java.util.List;
import java.util.Map;

import com.example.blogservice.dtos.BlogDto;
import com.example.blogservice.dtos.CommentDto;
import com.example.blogservice.dtos.UserDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;

    // User field
    private UserDto user;

    // Blog fields
    private BlogDto blog;
    private List<BlogDto> blogs;

    // Comment fields
    private CommentDto comment;
    private List<CommentDto> comments;

    // Pagination and stats
    private Object pagination;
    private Map<String, Object> stats;

    // Generic data container for any other service-specific data
    private Map<String, Object> additionalData;

    public Response(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Response() {
        this.statusCode = 200;
    }
}