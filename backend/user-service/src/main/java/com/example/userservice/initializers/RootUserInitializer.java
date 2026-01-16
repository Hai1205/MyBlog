package com.example.userservice.initializers;

import com.example.userservice.dtos.UserDto;
import com.example.userservice.services.apis.UserApi;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RootUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RootUserInitializer.class);

    private final UserApi userService;

    @Value("${ROOT_EMAIL}")
    private String rootEmail;

    @Value("${ROOT_PASSWORD}")
    private String rootPassword;

    @Value("${ROOT_FULLNAME}")
    private String rootFullname;

    @Value("${ROOT_USERNAME}")
    private String rootUsername;

    @Value("${ROOT_ROLE}")
    private String rootRole;

    @Value("${ROOT_STATUS}")
    private String rootStatus;

    public RootUserInitializer(UserApi userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking if root user exists...");

        // Check if root user exists by email
        UserDto existingUser = userService.handleFindByEmail(rootEmail);

        if (existingUser == null) {
            log.info("Root user not found. Creating root user: {}", rootEmail);

            // Parameters: username, email, password, fullname, location, birth, summary, role, status, instagram, linkedin, facebook, avatar
            userService.handleCreateUser(rootUsername, rootEmail, rootPassword, rootFullname, null, null, null,
                    rootRole, rootStatus, null, null, null, null);

            log.info("Root user created successfully");
        } else {
            log.info("Root user already exists. Skipping creation.");
        }
    }
}