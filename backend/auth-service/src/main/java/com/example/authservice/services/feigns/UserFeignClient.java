package com.example.authservice.services.feigns;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.authservice.dtos.responses.Response;

@FeignClient(name = "${USER_SERVICE_NAME}", url = "${USER_SERVICE_URL}")
public interface UserFeignClient {

    @PostMapping("/api/v1/users/register")
    Response registerUser(@RequestBody String dataJson);

    @GetMapping("/api/v1/users/{userId}")
    Response getUserById(@PathVariable("userId") String userId);

    @PostMapping("/api/v1/users/authenticate/{identifier}")
    Response authenticateUser(@PathVariable("identifier") String identifier, @RequestParam("password") String password);

    @GetMapping("/api/v1/users/identifier/{identifier}")
    Response findUserByIdentifier(@PathVariable("identifier") String identifier);

    @PatchMapping("/api/v1/users/activate/{email}")
    Response activateUser(@PathVariable("email") String email);

    @PatchMapping("/api/v1/users/change-password/{identifier}")
    Response changePassword(@PathVariable("identifier") String identifier, @RequestBody String data);

    @PatchMapping("/api/v1/users/reset-password/{email}")
    Response resetPassword(@PathVariable("email") String email);

    @PatchMapping("/api/v1/users/forgot-password/{email}")
    Response forgotPassword(@PathVariable("email") String email, @RequestBody String data);
}