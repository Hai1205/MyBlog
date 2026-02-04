package com.example.notiservice.controllers;

import com.example.notiservice.dtos.responses.Response;
import com.example.notiservice.services.apis.NotiApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotiController {

    @Autowired
    private NotiApi notiApi;

    @GetMapping
    public ResponseEntity<Response> GetAllNotifications() {
        Response response = notiApi.GetAllNotifications();
    
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Response> getUserNotifications(@PathVariable("userId") UUID userId) {
        Response response = notiApi.getUserNotifications(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{notiId}")
    public ResponseEntity<Response> deleteNotification(@PathVariable("notiId") UUID notiId) {
        Response response = notiApi.deleteNotification(notiId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Response> deleteUserNotifications(@PathVariable("userId") UUID userId) {
        Response response = notiApi.deleteUserNotifications(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{notiId}")
    public ResponseEntity<Response> getNotification(@PathVariable("notiId") UUID notiId) {
        Response response = notiApi.getNotification(notiId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/mark/{notiId}")
    public ResponseEntity<Response> markNotification(
            @PathVariable("notiId") UUID notiId) {
        Response response = notiApi.markNotification(notiId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/mark/users/{userId}")
    public ResponseEntity<Response> markUserNotification(
            @PathVariable("userId") UUID userId) {
        Response response = notiApi.markUserNotification(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable UUID userId) {
        return notiApi.subscribe(userId);
    }

    @PostMapping("/unsubscribe/{userId}")
    public ResponseEntity<?> unsubscribe(@PathVariable UUID userId) {
        notiApi.unsubscribe(userId);
        return ResponseEntity.ok(Map.of("message", "Unsubscribed successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response(200, "Notification Service is running");
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}