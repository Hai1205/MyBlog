package com.example.mailservice.controllers;

import com.example.mailservice.dtos.responses.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mail")
public class MailController {
    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response(200, "Mail Service is running");

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}