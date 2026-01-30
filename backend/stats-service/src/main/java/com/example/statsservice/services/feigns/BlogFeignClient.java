package com.example.statsservice.services.feigns;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.statsservice.dtos.responses.Response;

@FeignClient(name = "${BLOG_SERVICE_NAME}", url = "${BLOG_SERVICE_URL}")
public interface BlogFeignClient {

    @GetMapping("/api/v1/blogs?isView=true")
    Response getAllBlogs();
}