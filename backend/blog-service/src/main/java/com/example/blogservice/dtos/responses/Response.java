package com.example.blogservice.dtos.responses;

import java.util.List;
import java.util.Map;

import com.example.blogservice.dtos.BlogDto;
import com.example.blogservice.dtos.CommentDto;
import com.example.blogservice.dtos.responses.views.BlogView;
import com.example.blogservice.dtos.responses.views.UserView;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    private int statusCode;
    private String message;
    private Map<String, Object> additionalData;

    private UserView userView;
    private List<BlogView> blogViews;

    // Blog fields
    private BlogDto blog;
    private List<BlogDto> blogs;

    // Comment fields
    private CommentDto comment;
    private List<CommentDto> comments;

    public Response(String message, int statusCode) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public Response(String message) {
        this.statusCode = 200;
        this.message = message;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Response() {
        this.statusCode = 200;
    }
}