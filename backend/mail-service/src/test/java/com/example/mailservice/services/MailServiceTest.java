package com.example.mailservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.mailservice.configs.MailConfig;
import com.example.mailservice.services.apis.MailApi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MailServiceTest {

    @Mock
    private MailConfig mailConfig;

    @InjectMocks
    private MailApi mailService;

    private String toEmail;
    private String otp;
    private String newPassword;

    @BeforeEach
    void setUp() {
        toEmail = "test@example.com";
        otp = "123456";
        newPassword = "newPassword123";
    }

    @Test
    void testSendMailActivation_Success() throws Exception {
        // Arrange
        doNothing().when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act
        mailService.sendMailActivation(toEmail, otp);

        // Assert
        verify(mailConfig).sendMail(
                eq(toEmail),
                eq("Account Activation"),
                eq("mail_active_account"),
                anyMap());
    }

    @Test
    void testSendMailResetPassword_Success() throws Exception {
        // Arrange
        doNothing().when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act
        mailService.sendMailResetPassword(toEmail, newPassword);

        // Assert
        verify(mailConfig).sendMail(
                eq(toEmail),
                eq("Reset Password"),
                eq("mail_reset_password"),
                anyMap());
    }

    @Test
    void testSendMailActivation_ThrowsException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Failed to send email"))
                .when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> mailService.sendMailActivation(toEmail, otp));
    }

    @Test
    void testSendMailResetPassword_ThrowsException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Failed to send email"))
                .when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> mailService.sendMailResetPassword(toEmail, newPassword));
    }

    @Test
    void testSendMailActivation_NullEmail() throws Exception {
        // Arrange
        doNothing().when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act
        mailService.sendMailActivation(null, otp);

        // Assert
        verify(mailConfig).sendMail(
                eq(null),
                eq("Account Activation"),
                eq("mail_active_account"),
                anyMap());
    }

    @Test
    void testSendMailActivation_EmptyEmail() throws Exception {
        // Arrange
        doNothing().when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act
        mailService.sendMailActivation("", otp);

        // Assert
        verify(mailConfig).sendMail(
                eq(""),
                eq("Account Activation"),
                eq("mail_active_account"),
                anyMap());
    }

    @Test
    void testSendMailActivation_NullOtp() throws Exception {
        // Arrange
        doNothing().when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act
        mailService.sendMailActivation(toEmail, null);

        // Assert
        verify(mailConfig).sendMail(
                eq(toEmail),
                eq("Account Activation"),
                eq("mail_active_account"),
                anyMap());
    }

    @Test
    void testSendMailActivation_EmptyOtp() throws Exception {
        // Arrange
        doNothing().when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act
        mailService.sendMailActivation(toEmail, "");

        // Assert
        verify(mailConfig).sendMail(
                eq(toEmail),
                eq("Account Activation"),
                eq("mail_active_account"),
                anyMap());
    }

    @Test
    void testSendMailResetPassword_NullEmail() throws Exception {
        // Arrange
        doNothing().when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act
        mailService.sendMailResetPassword(null, newPassword);

        // Assert
        verify(mailConfig).sendMail(
                eq(null),
                eq("Reset Password"),
                eq("mail_reset_password"),
                anyMap());
    }

    @Test
    void testSendMailResetPassword_EmptyEmail() throws Exception {
        // Arrange
        doNothing().when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act
        mailService.sendMailResetPassword("", newPassword);

        // Assert
        verify(mailConfig).sendMail(
                eq(""),
                eq("Reset Password"),
                eq("mail_reset_password"),
                anyMap());
    }

    @Test
    void testSendMailResetPassword_NullNewPassword() throws Exception {
        // Arrange
        doNothing().when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act
        mailService.sendMailResetPassword(toEmail, null);

        // Assert
        verify(mailConfig).sendMail(
                eq(toEmail),
                eq("Reset Password"),
                eq("mail_reset_password"),
                anyMap());
    }

    @Test
    void testSendMailResetPassword_EmptyNewPassword() throws Exception {
        // Arrange
        doNothing().when(mailConfig).sendMail(anyString(), anyString(), anyString(), anyMap());

        // Act
        mailService.sendMailResetPassword(toEmail, "");

        // Assert
        verify(mailConfig).sendMail(
                eq(toEmail),
                eq("Reset Password"),
                eq("mail_reset_password"),
                anyMap());
    }
}
