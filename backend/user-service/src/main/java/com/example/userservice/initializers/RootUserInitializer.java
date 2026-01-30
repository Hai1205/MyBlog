package com.example.userservice.initializers;

import com.example.userservice.exceptions.OurException;
import com.example.userservice.services.apis.handlers.UserHandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RootUserInitializer implements CommandLineRunner {

    private final UserHandler userHandler;

    @Value("${ROOT_EMAIL}")
    private String rootEmail;

    @Value("${ROOT_PASSWORD}")
    private String rootPassword;

    @Value("${ROOT_USERNAME}")
    private String rootUsername;

    @Value("${ROOT_ROLE}")
    private String rootRole;

    @Value("${ROOT_STATUS}")
    private String rootStatus;

    public RootUserInitializer(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking if root user exists...");

        try {
            userHandler.handleGetUserByEmail(rootEmail);
            log.info("Root user already exists. Skipping creation.");
        } catch (OurException e) {
            if ("User not found".equals(e.getMessage())) {
                log.info("Root user not found. Creating root user: {}", rootEmail);

                userHandler.handleCreateUser(rootUsername, rootEmail, rootPassword, null, null, null,
                        rootRole, rootStatus, null, null, null, null);

                log.info("Root user created successfully");
            } else {
                throw e;
            }
        }
    }
}