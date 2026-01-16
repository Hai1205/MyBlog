package com.example.rediscommon.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Common Redis service for caching operations
 * Provides basic CRUD operations for Redis cache
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Set a key-value pair in Redis
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Set key: {}", key);
        } catch (Exception e) {
            log.error("Error setting key: {}", key, e);
            throw new RuntimeException("Failed to set key in Redis: " + e.getMessage());
        }
    }

    /**
     * Set a key-value pair with expiration time
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Set key: {} with expiration: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting key with expiration: {}", key, e);
            throw new RuntimeException("Failed to set key with expiration in Redis: " + e.getMessage());
        }
    }

    /**
     * Get value by key
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting key: {}", key, e);
            throw new RuntimeException("Failed to get key from Redis: " + e.getMessage());
        }
    }

    /**
     * Delete a key
     */
    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Delete key: {}, result: {}", key, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Error deleting key: {}", key, e);
            throw new RuntimeException("Failed to delete key from Redis: " + e.getMessage());
        }
    }

    /**
     * Check if key exists
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking key existence: {}", key, e);
            return false;
        }
    }

    /**
     * Set expiration time for a key
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            log.debug("Set expiration for key: {}, timeout: {} {}, result: {}", key, timeout, unit, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Error setting expiration for key: {}", key, e);
            throw new RuntimeException("Failed to set expiration for key in Redis: " + e.getMessage());
        }
    }

    /**
     * Get time to live for a key in seconds
     */
    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting expiration for key: {}", key, e);
            return null;
        }
    }

    /**
     * Increment a numeric value
     */
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Error incrementing key: {}", key, e);
            throw new RuntimeException("Failed to increment key in Redis: " + e.getMessage());
        }
    }

    /**
     * Increment a numeric value by delta
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Error incrementing key: {} by delta: {}", key, delta, e);
            throw new RuntimeException("Failed to increment key in Redis: " + e.getMessage());
        }
    }

    /**
     * Decrement a numeric value
     */
    public Long decrement(String key) {
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("Error decrementing key: {}", key, e);
            throw new RuntimeException("Failed to decrement key in Redis: " + e.getMessage());
        }
    }

    /**
     * Decrement a numeric value by delta
     */
    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("Error decrementing key: {} by delta: {}", key, delta, e);
            throw new RuntimeException("Failed to decrement key in Redis: " + e.getMessage());
        }
    }
}
