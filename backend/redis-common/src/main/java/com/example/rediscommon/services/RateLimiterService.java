package com.example.rediscommon.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Rate Limiter Service using Redis
 * Provides rate limiting functionality based on sliding window algorithm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisService redisService;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    /**
     * Check if request is allowed based on rate limit
     * 
     * @param key           Unique identifier for rate limiting (e.g., userId, IP,
     *                      endpoint)
     * @param maxRequests   Maximum number of requests allowed
     * @param windowSeconds Time window in seconds
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        try {
            String rateLimitKey = RATE_LIMIT_PREFIX + key;

            // Get current request count
            Object countObj = redisService.get(rateLimitKey);
            long currentCount = countObj != null ? Long.parseLong(countObj.toString()) : 0;

            if (currentCount >= maxRequests) {
                log.warn("Rate limit exceeded for key: {}, current: {}, max: {}", key, currentCount, maxRequests);
                return false;
            }

            // Increment counter
            long newCount = redisService.increment(rateLimitKey);

            // Set expiration only on first request
            if (newCount == 1) {
                redisService.expire(rateLimitKey, windowSeconds, TimeUnit.SECONDS);
            }

            log.debug("Rate limit check for key: {}, count: {}/{}", key, newCount, maxRequests);
            return true;
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // Allow request on error to prevent blocking users due to Redis issues
            return true;
        }
    }

    /**
     * Get remaining requests for a key
     * 
     * @param key         Unique identifier for rate limiting
     * @param maxRequests Maximum number of requests allowed
     * @return Number of remaining requests
     */
    public long getRemainingRequests(String key, int maxRequests) {
        try {
            String rateLimitKey = RATE_LIMIT_PREFIX + key;
            Object countObj = redisService.get(rateLimitKey);
            long currentCount = countObj != null ? Long.parseLong(countObj.toString()) : 0;
            return Math.max(0, maxRequests - currentCount);
        } catch (Exception e) {
            log.error("Error getting remaining requests for key: {}", key, e);
            return maxRequests;
        }
    }

    /**
     * Get time until rate limit resets in seconds
     * 
     * @param key Unique identifier for rate limiting
     * @return Seconds until reset, or -1 if no limit active
     */
    public long getResetTime(String key) {
        try {
            String rateLimitKey = RATE_LIMIT_PREFIX + key;
            Long expireTime = redisService.getExpire(rateLimitKey);
            return expireTime != null ? expireTime : -1;
        } catch (Exception e) {
            log.error("Error getting reset time for key: {}", key, e);
            return -1;
        }
    }

    /**
     * Reset rate limit for a key
     * 
     * @param key Unique identifier for rate limiting
     */
    public void resetLimit(String key) {
        try {
            String rateLimitKey = RATE_LIMIT_PREFIX + key;
            redisService.delete(rateLimitKey);
            log.info("Rate limit reset for key: {}", key);
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }
}
