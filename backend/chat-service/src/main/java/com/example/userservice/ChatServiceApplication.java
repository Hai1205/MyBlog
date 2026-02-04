package com.example.chatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.example.rediscommon.configs.RedisConfig;
import com.example.securitycommon.configs.SecurityConfig;

@SpringBootApplication
@ComponentScan(basePackages = { "com.example.chatservice", "com.example.cloudinarycommon", "com.example.rediscommon" })
@Import({ SecurityConfig.class, RedisConfig.class })
@EnableJpaAuditing
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}