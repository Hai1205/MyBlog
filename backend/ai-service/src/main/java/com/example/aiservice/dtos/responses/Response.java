package com.example.aiservice.dtos.responses;

import java.util.Map;

// import com.example.aiservice.dtos.*;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;

    private String title;
    private String description;
    private String content;

    private Map<String, Object> additionalData;

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