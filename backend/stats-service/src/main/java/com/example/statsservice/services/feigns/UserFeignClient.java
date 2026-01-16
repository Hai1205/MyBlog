package com.example.statsservice.services.feigns;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.statsservice.dtos.responses.Response;

@FeignClient(name = "${USER_SERVICE_NAME}", url = "${USER_SERVICE_URL}")
public interface UserFeignClient {

    @GetMapping("/api/v1/users/stats")
    Response getUserStats();

    @GetMapping("/api/v1/users/stats/status/{status}")
    Response getUsersByStatus(@RequestParam("status") String status);

    @GetMapping("/api/v1/users/stats/created-range")
    Response getUsersCreatedInRange(@RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);

    @GetMapping("/api/v1/users/recent")
    Response getRecentUsers(@RequestParam("limit") int limit);
}