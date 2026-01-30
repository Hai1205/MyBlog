package com.example.mailservice.services.apis.handlers;

import com.example.mailservice.configs.MailConfig;
import com.example.mailservice.exceptions.OurException;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailHandler {

    private MailConfig mailConfig;

    public MailHandler(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    public void sendMailActivation(String email, String otp) {
        try {
            log.info("Starting sendMailActivation for email: {}", email);

            long startTime = System.currentTimeMillis();
            log.info("Sending OTP to email: {}", email);

            String subject = "Account Activation";
            String templateName = "mail_active_account";
            Map<String, Object> variables = new HashMap<>();
            variables.put("recipientEmail", email);
            variables.put("recipientName", email);
            variables.put("senderName", "MyBlog");
            variables.put("otp", otp);
            log.debug("Prepared email variables for activation");

            mailConfig.sendMail(email, subject, templateName, variables);
            log.debug("Mail sent via mailConfig");

            long endTime = System.currentTimeMillis();
            log.info("Completed request in {} ms", endTime - startTime);
            log.info("OTP sent successfully to email: {}", email);
        } catch (OurException e) {
            log.error("Error in sendMailActivation: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in sendMailActivation: {}", e.getMessage(), e);
            throw new OurException("Failed to send activation mail", 500);
        }
    }

    public void sendMailResetPassword(String email, String newPassword) {
        try {
            log.info("Starting sendMailResetPassword for email: {}", email);

            long startTime = System.currentTimeMillis();
            log.info("Password reset attempt for email: {}", email);

            String subject = "Reset Password";
            String templateName = "mail_reset_password";
            Map<String, Object> variables = new HashMap<>();
            variables.put("recipientEmail", email);
            variables.put("recipientName", email);
            variables.put("senderName", "MyBlog");
            variables.put("password", newPassword);
            log.debug("Prepared email variables for password reset");

            mailConfig.sendMail(email, subject, templateName, variables);
            log.debug("Mail sent via mailConfig");

            long endTime = System.currentTimeMillis();
            log.info("Completed request in {} ms", endTime - startTime);
            log.info("Password reset email sent successfully to: {}", email);
        } catch (OurException e) {
            log.error("Error in sendMailResetPassword: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in sendMailResetPassword: {}", e.getMessage(), e);
            throw new OurException("Failed to send password reset mail", 500);
        }
    }
}