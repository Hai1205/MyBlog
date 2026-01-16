package com.example.authservice.controllers;

import com.example.authservice.dtos.requests.*;
import com.example.authservice.dtos.responses.*;
import com.example.authservice.services.apis.AuthApi;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthApi authApi;

    @PostMapping("/login")
    public ResponseEntity<Response> login(
            @RequestPart("data") String dataJson,
            HttpServletResponse httpServletResponse) {
        Response response = authApi.login(dataJson, httpServletResponse);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Response> register(
            @RequestPart("data") String dataJson) {
        Response response = authApi.register(dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/send-otp/{identifier}")
    public ResponseEntity<Response> sendOTP(@PathVariable("identifier") String identifier) {
        Response response = authApi.sendOTP(identifier);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/verify-otp/{identifier}")
    public ResponseEntity<Response> verifyOTP(
            @PathVariable("identifier") String identifier,
            @RequestPart("data") String dataJson) {
        Response response = authApi.verifyOTP(identifier, dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/change-password/{identifier}")
    public ResponseEntity<Response> changePassword(
            @PathVariable("identifier") String identifier,
            @RequestPart("data") String dataJson) {
        Response response = authApi.changePassword(identifier,
                dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/reset-password/{email}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response> resetPassword(
            @PathVariable("email") String email) {
        Response response = authApi.resetPassword(email);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/forgot-password/{identifier}")
    public ResponseEntity<Response> forgotPassword(
            @PathVariable("identifier") String identifier,
            @RequestPart("data") String dataJson) {
        Response response = authApi.forgotPassword(identifier,
                dataJson);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Response> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            jakarta.servlet.http.HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        Response response = authApi.refreshToken(refreshTokenRequest, authorizationHeader, httpServletRequest,
                httpServletResponse);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyAuthority('admin','user')")
    public ResponseEntity<Response> logout(HttpServletResponse httpServletResponse) {
        Response response = authApi.logout(httpServletResponse);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response(200, "Auth Service is running");

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}