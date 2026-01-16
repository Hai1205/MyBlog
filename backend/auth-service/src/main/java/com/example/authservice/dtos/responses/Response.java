package com.example.authservice.dtos.responses;

import java.util.List;
import java.util.Map;

import com.example.authservice.dtos.UserDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;

    private String newPassword;
    private List<UserDto> users;
    private String token;
    private String role;
    private String status;
    private UserDto user;
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