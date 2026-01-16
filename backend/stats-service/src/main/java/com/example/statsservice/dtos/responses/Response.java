package com.example.statsservice.dtos.responses;

import java.util.List;
import java.util.Map;

import com.example.statsservice.dtos.DashboardStatsDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;

    private DashboardStatsDto dashboardStats;
    private byte[] statsReport;
    private List<Map<String, Object>> blogs;
    private List<Map<String, Object>> users;

    // Pagination and stats
    private Object pagination;
    private Map<String, Object> stats;

    // Generic data container for any other service-specific data
    private Map<String, Object> additionalData;

    public Response(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Response() {
        this.statusCode = 200;
    }
}
