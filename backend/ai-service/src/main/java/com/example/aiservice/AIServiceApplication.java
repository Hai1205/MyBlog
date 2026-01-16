package com.example.aiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;

import com.example.securitycommon.configs.SecurityConfig;

import java.util.TimeZone;

@SpringBootApplication
@Import({ SecurityConfig.class })
@EnableRetry
@EnableFeignClients
public class AIServiceApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SpringApplication.run(AIServiceApplication.class, args);
    }
}
