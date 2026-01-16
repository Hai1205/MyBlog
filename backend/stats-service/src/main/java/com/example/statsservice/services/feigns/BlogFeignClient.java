package com.example.statsservice.services.feigns;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.statsservice.dtos.responses.Response;

@FeignClient(name = "${BLOG_SERVICE_NAME}", url = "${BLOG_SERVICE_URL}")
public interface BlogFeignClient {

    @GetMapping("/api/v1/cvs/stats/total")
    Response getTotalBlogs();

    @GetMapping("/api/v1/cvs/stats/visibility/{visibility}")
    Response getBlogsByVisibility(@PathVariable("visibility") boolean isVisibility);

    @GetMapping("/api/v1/cvs/stats/created-range")
    Response getBlogsCreatedInRange(@RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);

    @GetMapping("/api/v1/cvs/recent")
    Response getRecentBlogs(@RequestParam("limit") int limit);
}