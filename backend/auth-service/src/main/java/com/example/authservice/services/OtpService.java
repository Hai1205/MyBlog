package com.example.authservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.rediscommon.services.RedisService;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * OTP Service using Redis for temporary storage
 * Provides OTP generation, validation and management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String OTP_PREFIX = "OTP_";
    private static final int OTP_EXPIRATION = 3; // 3 minutes
    private static final int OTP_LENGTH = 6;

    private final RedisService redisService;

    /**
     * Generate and save OTP for an email
     */
    public String generateOtp(String email) {
        try {
            String otp = generateRandomOtp(OTP_LENGTH);
            saveOtp(email, otp);
            return otp;
        } catch (Exception e) {
            log.error("Error generating OTP for email: {}", email, e);
            throw new RuntimeException("Failed to generate OTP: " + e.getMessage());
        }
    }

    /**
     * Generate random numeric OTP
     */
    private String generateRandomOtp(int length) {
        StringBuilder otp = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }

    /**
     * Save OTP to Redis with expiration
     */
    public void saveOtp(String email, String otp) {
        try {
            String key = OTP_PREFIX + email;
            redisService.set(key, otp, OTP_EXPIRATION, TimeUnit.MINUTES);
            log.info("OTP saved for email: {}, expires in {} minutes", email, OTP_EXPIRATION);
        } catch (Exception e) {
            log.error("Error saving OTP for email: {}", email, e);
            throw new RuntimeException("Failed to save OTP: " + e.getMessage());
        }
    }

    /**
     * Validate OTP for specific email
     */
    public boolean validateOtp(String email, String otp) {
        try {
            String key = OTP_PREFIX + email;
            Object savedOtp = redisService.get(key);

            if (savedOtp != null && savedOtp.toString().equals(otp)) {
                // Delete OTP after successful validation
                redisService.delete(key);
                log.info("OTP verified successfully for email: {}", email);
                return true;
            }

            log.warn("Invalid OTP for email: {}", email);
            return false;
        } catch (Exception e) {
            log.error("Error verifying OTP for email: {}", email, e);
            throw new RuntimeException("Failed to verify OTP: " + e.getMessage());
        }
    }

    /**
     * Check if OTP exists for an email
     */
    public boolean otpExists(String email) {
        try {
            String key = OTP_PREFIX + email;
            return redisService.hasKey(key);
        } catch (Exception e) {
            log.error("Error checking OTP existence for email: {}", email, e);
            return false;
        }
    }

    /**
     * Delete OTP for specific email
     */
    public void deleteOtp(String email) {
        try {
            String key = OTP_PREFIX + email;
            redisService.delete(key);
            log.info("OTP deleted for email: {}", email);
        } catch (Exception e) {
            log.error("Error deleting OTP for email: {}", email, e);
        }
    }

    /**
     * Get remaining time for OTP in seconds
     */
    public Long getOtpExpireTime(String email) {
        try {
            String key = OTP_PREFIX + email;
            return redisService.getExpire(key);
        } catch (Exception e) {
            log.error("Error getting OTP expire time for email: {}", email, e);
            return null;
        }
    }
}
