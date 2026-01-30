package com.example.authservice.dtos.responses;

import java.util.Map;

import com.example.authservice.dtos.responses.views.UserView;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;
    private Map<String, Object> additionalData;

    private UserView userView;

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