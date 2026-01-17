package com.example.mailservice.services.apis;

import com.example.mailservice.configs.MailConfig;
import com.example.mailservice.exceptions.OurException;

import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MailApi {

    private static final Logger logger = LoggerFactory.getLogger(MailApi.class);
    private MailConfig mailConfig;

    public MailApi(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    public void sendMailActivation(String email, String otp) {
        try {
            long startTime = System.currentTimeMillis();
            logger.info("Starting request at {}", startTime);
            logger.info("Sending OTP to email: {}", email);

            String subject = "Account Activation";
            String templateName = "mail_active_account";
            Map<String, Object> variables = new HashMap<>();
            variables.put("recipientEmail", email);
            variables.put("recipientName", email);
            variables.put("senderName", "MyBlog");
            variables.put("otp", otp);
            mailConfig.sendMail(email, subject, templateName, variables);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);
            logger.info("OTP sent successfully to email: {}", email);
        } catch (OurException e) {
            logger.error("Error in sendMailActivation: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in sendMailActivation: {}", e.getMessage(), e);
            throw new OurException("Failed to send activation mail", 500);
        }
    }

    public void sendMailResetPassword(String email, String newPassword) {
        try {
            long startTime = System.currentTimeMillis();
            logger.info("Starting request at {}", startTime);
            logger.info("Password reset attempt for email: {}", email);

            String subject = "Reset Password";
            String templateName = "mail_reset_password";
            Map<String, Object> variables = new HashMap<>();
            variables.put("recipientEmail", email);
            variables.put("recipientName", email);
            variables.put("senderName", "MyBlog");
            variables.put("password", newPassword);

            mailConfig.sendMail(email, subject, templateName, variables);

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);
            logger.info("Password reset email sent successfully to: {}", email);
        } catch (OurException e) {
            logger.error("Error in sendMailResetPassword: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in sendMailResetPassword: {}", e.getMessage(), e);
            throw new OurException("Failed to send password reset mail", 500);
        }
    }
}