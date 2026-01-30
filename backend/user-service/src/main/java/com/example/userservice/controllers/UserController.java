package com.example.userservice.controllers;

import com.example.userservice.dtos.responses.Response;
import com.example.userservice.services.apis.UserApi;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserApi userApi;

    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response> createUser(@RequestPart("data") String dataJson) {
        Response response = userApi.createUser(dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestBody String dataJson) {
        Response response = userApi.createUser(dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response> getAllUsers(
            @RequestParam(value = "isView", required = false) Boolean isView) {
        Response response = userApi.getAllUsers(isView);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Response> getUser(@PathVariable("userId") UUID userId,
            @RequestParam(value = "isView", required = false) Boolean isView) {
        Response response = userApi.getUser(userId, isView);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('admin','user')")
    public ResponseEntity<Response> updateUser(
            @PathVariable("userId") UUID userId,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        Response response = userApi.updateUser(userId, dataJson, avatar);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response> deleteUser(@PathVariable("userId") UUID userId) {
        Response response = userApi.deleteUser(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/authenticate/{identifier}")
    public ResponseEntity<Response> authenticateUser(@PathVariable("identifier") String identifier,
            @RequestParam("password") String password) {
        Response response = userApi.authenticateUser(identifier, password);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/identifier/{identifier}")
    public ResponseEntity<Response> findUserByIdentifier(@PathVariable("identifier") String identifier,
            @RequestParam(value = "isView", required = false) Boolean isView) {
        Response response = userApi.findUserByIdentifier(identifier, isView);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/activate/{email}")
    public ResponseEntity<Response> activateUser(@PathVariable("email") String email) {
        Response response = userApi.activateUser(email);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/change-password/{identifier}")
    public ResponseEntity<Response> changePassword(
            @PathVariable("identifier") String identifier,
            @RequestBody() String dataJson) {
        Response response = userApi.changePassword(identifier, dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/reset-password/{email}")
    public ResponseEntity<Response> resetPassword(@PathVariable("email") String email) {
        Response response = userApi.resetPassword(email);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/forgot-password/{email}")
    public ResponseEntity<Response> forgotPassword(
            @PathVariable("email") String email,
            @RequestBody() String dataJson) {

        Response response = userApi.forgotPassword(email, dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Response> findUserByEmail(@PathVariable("email") String email,
            @RequestParam(value = "isView", required = false) Boolean isView) {
        Response response = userApi.findUserByEmail(email, isView);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response("User Service is running", 200);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}