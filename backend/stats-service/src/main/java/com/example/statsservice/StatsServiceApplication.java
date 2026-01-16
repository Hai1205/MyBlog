package com.example.statsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import com.example.securitycommon.configs.SecurityConfig;
@SpringBootApplication
@ComponentScan(basePackages = { "com.example.statsservice", "com.example.rediscommon" })
@Import({ SecurityConfig.class })
@EnableFeignClients
public class StatsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatsServiceApplication.class, args);
    }
}
