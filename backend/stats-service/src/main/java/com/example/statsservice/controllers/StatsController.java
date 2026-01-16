package com.example.statsservice.controllers;

import com.example.statsservice.dtos.responses.Response;
import com.example.statsservice.services.apis.StatsApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {

    @Autowired
    private StatsApi statsService;

    @GetMapping("/")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response> getDashboardStats() {
        Response response = statsService.getDashboardStats();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/report")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response> getStatsReport() {
        Response response = statsService.getStatsReport();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Response> health() {
        Response response = new Response(200, "Stats Service is running");

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
