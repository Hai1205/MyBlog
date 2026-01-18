package com.example.aiservice.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.aiservice.dtos.responses.Response;
import com.example.aiservice.services.apis.AIApi;

@RestController
@RequestMapping("/api/v1/ai")
public class AIController {

    @Autowired
    private AIApi aiService;

    @PostMapping("/content")
    public ResponseEntity<Response> analyzeContent(
            @RequestPart("data") String dataJson) {
        Response response = aiService.analyzeContent(dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/description")
    public ResponseEntity<Response> analyzeDescription(
            @RequestPart("data") String dataJson) {
        Response response = aiService.analyzeDescription(dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/title")
    public ResponseEntity<Response> analyzeTitle(
            @RequestPart("data") String dataJson) {
        Response response = aiService.analyzeTitle(dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response();
        response.setStatusCode(200);
        response.setMessage("AI Service is running");

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}