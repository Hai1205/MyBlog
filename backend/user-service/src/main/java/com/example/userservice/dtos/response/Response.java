package com.example.userservice.dtos.response;

import java.util.List;
import java.util.Map;

import com.example.userservice.dtos.UserDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;

    // User related data
    private UserDto user;
    private List<UserDto> users;
    private String token;
    private String role;
    private String status;

    // Pagination and stats
    private Object pagination;
    private Map<String, Object> stats;

    // Authentication related
    private Boolean authenticated;
    private String expirationTime;

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