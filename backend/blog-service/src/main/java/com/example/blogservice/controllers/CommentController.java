package com.example.blogservice.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.blogservice.dtos.responses.Response;
import com.example.blogservice.services.apis.CommentApi;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    @Autowired
    private CommentApi commentApi;

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
        Response response = commentApi.addComment(blogId, userId, dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
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
        Response response = commentApi.updateComment(commentId, dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Response> deleteComment(
            @PathVariable("commentId") UUID commentId) {
        Response response = commentApi.deleteComment(commentId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response("Comment Service is running", 200);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}