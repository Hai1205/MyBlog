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
    public ResponseEntity<Response> getAllBlogs(
            @RequestParam(value = "isVisibility", required = false) Boolean isVisibility,
            @RequestParam(value = "isView", required = false) Boolean isView) {
        Response response = blogApi.getAllBlogs(isVisibility, isView);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<Response> createBlog(
            @PathVariable("userId") UUID userId,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        Response response = blogApi.createBlog(userId, dataJson, thumbnail);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/{blogId}/users/{userId}/duplicate")
    public ResponseEntity<Response> duplicateBlog(
            @PathVariable("blogId") UUID blogId,
            @PathVariable("userId") UUID userId) {
        Response response = blogApi.duplicateBlog(blogId, userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/{blogId}")
    public ResponseEntity<Response> updateBlog(
            @PathVariable("blogId") UUID blogId,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        Response response = blogApi.updateBlog(blogId, dataJson, thumbnail);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/{blogId}/users/{userId}/save")
    public ResponseEntity<Response> saveBlog(
            @PathVariable("userId") UUID userId,
            @PathVariable("blogId") UUID blogId) {
        Response response = blogApi.saveBlog(userId, blogId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{blogId}/users/{userId}/unsave")
    public ResponseEntity<Response> unsaveBlog(
            @PathVariable("userId") UUID userId,
            @PathVariable("blogId") UUID blogId) {
        Response response = blogApi.unsaveBlog(userId, blogId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/users/{userId}/saved")
    public ResponseEntity<Response> getUserSavedBlogs(@PathVariable("userId") UUID userId) {
        Response response = blogApi.getUserSavedBlogs(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Response> getUserBlogs(@PathVariable("userId") UUID userId) {
        Response response = blogApi.getUserBlogs(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{blogId}/users/{userId}")
    public ResponseEntity<Response> getBlog(@PathVariable("blogId") UUID blogId, @PathVariable("userId") UUID userId) {
        Response response = blogApi.getBlog(blogId, userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
    @GetMapping("/{blogId}")
    public ResponseEntity<Response> getBlog(@PathVariable("blogId") UUID blogId) {
        Response response = blogApi.getBlog(blogId, null);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{blogId}")
    public ResponseEntity<Response> deleteBlog(@PathVariable("blogId") UUID blogId) {
        Response response = blogApi.deleteBlog(blogId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response("Blog Service is running", 200);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}