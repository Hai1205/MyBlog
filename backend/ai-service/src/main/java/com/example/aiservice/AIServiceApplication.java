package com.example.aiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;

import com.example.rediscommon.configs.RedisConfig;
import com.example.securitycommon.configs.SecurityConfig;

import java.util.TimeZone;

@SpringBootApplication
@ComponentScan(basePackages = { "com.example.aiservice", "com.example.rediscommon" })
@Import({ SecurityConfig.class, RedisConfig.class })
@EnableRetry
@EnableFeignClients
public class AIServiceApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SpringApplication.run(AIServiceApplication.class, args);
    }
}
