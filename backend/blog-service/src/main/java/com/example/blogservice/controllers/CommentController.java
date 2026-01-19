package com.example.blogservice.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.blogservice.dtos.requests.AddCommentRequest;
import com.example.blogservice.dtos.requests.UpdateCommentRequest;
import com.example.blogservice.dtos.responses.Response;
import com.example.blogservice.services.apis.CommentApi;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    @Autowired
    private CommentApi commentApi;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping()
    public ResponseEntity<Response> getAllComments() {
        Response response = commentApi.getAllComments();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/blogs/{blogId}/users/{userId}")
    public ResponseEntity<Response> addComment(
            @PathVariable("userId") UUID userId,
            @PathVariable("blogId") UUID blogId,
            @RequestPart("data") String dataJson) {
        try {
            AddCommentRequest request = objectMapper.readValue(dataJson, AddCommentRequest.class);
            Response response = commentApi.addComment(blogId, userId, request);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception e) {
            log.error("Error parsing add comment request: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body(new Response(400, "Invalid request data"));
        }
    }

    @GetMapping("/blogs/{blogId}")
    public ResponseEntity<Response> getBlogComments(
            @PathVariable("blogId") UUID blogId) {
        Response response = commentApi.getBlogComments(blogId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<Response> updateComment(
            @PathVariable("commentId") UUID commentId,
            @RequestPart("data") String dataJson) {
        try {
            UpdateCommentRequest request = objectMapper.readValue(dataJson, UpdateCommentRequest.class);
            Response response = commentApi.updateComment(commentId, request);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception e) {
            log.error("Error parsing update comment request: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body(new Response(400, "Invalid request data"));
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Response> deleteComment(
            @PathVariable("commentId") UUID commentId) {
        Response response = commentApi.deleteComment(commentId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response(200, "Comment Service is running");

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}