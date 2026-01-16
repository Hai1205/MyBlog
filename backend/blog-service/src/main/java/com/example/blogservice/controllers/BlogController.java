package com.example.blogservice.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.blogservice.dtos.responses.Response;
import com.example.blogservice.services.apis.BlogApi;

@RestController
@RequestMapping("/api/v1/blogs")
public class BlogController {

    @Autowired
    private BlogApi blogApi;

    @GetMapping()
    public ResponseEntity<Response> getAllBlogs() {
        Response response = blogApi.getAllBlogs();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping()
    public ResponseEntity<Response> createBlog(
            @PathVariable("userId") UUID userId,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        Response response = blogApi.createBlog(userId, dataJson, thumbnail);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping()
    public ResponseEntity<Response> updateBlog(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        Response response = blogApi.updateBlog(dataJson, thumbnail);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }   

    @GetMapping("/{blogId}/users/{userId}/save")
    public ResponseEntity<Response> saveBlog(
            @PathVariable("userId") UUID userId,
            @PathVariable("blogId") UUID blogId) {
        Response response = blogApi.saveBlog(userId, blogId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/users/{userId}/save")
    public ResponseEntity<Response> getUserSavedBlogs(@PathVariable("userId") UUID userId) {
        Response response = blogApi.getUserSavedBlogs(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Response> getUserBlogs(@PathVariable("userId") UUID userId) {
        Response response = blogApi.getUserBlogs(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{blogId}")
    public ResponseEntity<Response> getBlog(@PathVariable("blogId") UUID blogId) {
        Response response = blogApi.getBlog(blogId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{blogId}")
    public ResponseEntity<Response> deleteBlog(@PathVariable("blogId") UUID blogId) {
        Response response = blogApi.deleteBlog(blogId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response(200, "Blog Service is running");

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}