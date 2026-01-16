package com.example.userservice.controllers;

import com.example.userservice.dtos.response.Response;
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
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response> getAllUsers() {
        Response response = userApi.getAllUsers();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{userId}")
    // @PreAuthorize("hasAnyAuthority('admin','user')")
    public ResponseEntity<Response> getUserById(@PathVariable("userId") UUID userId) {
        Response response = userApi.getUserById(userId);

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

    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response();
        response.setStatusCode(200);
        response.setMessage("User Service is running");

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/stats")
    // @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response> getUserStats() {
        Response response = userApi.getUserStats();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/stats/status/{status}")
    // @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response> getUsersByStatus(@PathVariable("status") String status) {
        Response response = userApi.getUsersByStatus(status);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // @GetMapping("/stats/created-range")
    // @PreAuthorize("hasAuthority('admin')")
    // public ResponseEntity<Response> getUsersCreatedInRange(
    //         @RequestParam("startDate") String startDate,
    //         @RequestParam("endDate") String endDate) {
    //     Response response = userApi.getUsersCreatedInRange(startDate, endDate);

    //     return ResponseEntity.status(response.getStatusCode()).body(response);
    // }

    @GetMapping("/recent")
    // @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response> getRecentUsers(@RequestParam("limit") int limit) {
        Response response = userApi.getRecentUsers(limit);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/authenticate/{identifier}")
    public ResponseEntity<Response> authenticateUser(@PathVariable("identifier") String identifier,
            @RequestParam("password") String password) {
        Response response = userApi.authenticateUser(identifier, password);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/find/{identifier}")
    public ResponseEntity<Response> findUserByIdentifier(@PathVariable("identifier") String identifier) {
        Response response = userApi.findUserByIdentifier(identifier);

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
    public ResponseEntity<Response> findUserByEmail(@PathVariable("email") String email) {
        Response response = userApi.findUserByEmail(email);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}