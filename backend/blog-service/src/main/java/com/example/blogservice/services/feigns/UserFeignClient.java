package com.example.blogservice.services.feigns;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.blogservice.dtos.responses.Response;

@FeignClient(name = "${USER_SERVICE_NAME}", url = "${USER_SERVICE_URL}")
public interface UserFeignClient {

    @GetMapping("/api/v1/users/{userId}?isView=true")
    Response getUserById(@PathVariable("userId") UUID userId);
}