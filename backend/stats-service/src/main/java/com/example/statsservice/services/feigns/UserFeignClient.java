package com.example.statsservice.services.feigns;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.statsservice.dtos.responses.Response;

@FeignClient(name = "${USER_SERVICE_NAME}", url = "${USER_SERVICE_URL}")
public interface UserFeignClient {

    @GetMapping("/api/v1/users")
    Response getAllUsers();
}