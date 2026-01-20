package com.example.rediscommon.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Generic API Response Handler with built-in:
 * - Rate limiting
 * - Timing/performance logging
 * - Consistent error handling
 * - Response wrapping
 * 
 * Can be used across all microservices for consistent API behavior
 * 
 * @param <R> The Response type (must have setStatusCode, setMessage methods)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiResponseHandler<R> {

    private final RateLimiterService rateLimiterService;

    /**
     * Execute API logic with rate limiting, timing, and error handling
     * For methods that return a single result
     *
     * @param rateLimitKey          The rate limit key
     * @param rateLimit             The rate limit threshold
     * @param businessLogic         The business logic to execute
     * @param responseSupplier      Supplier for new Response instance
     * @param responseStatusSetter  Consumer to set status code on response
     * @param responseMessageSetter Consumer to set message on response
     * @param resultSetter          BiConsumer to set result on response
     * @param successMessage        Success message
     * @param successStatusCode     Success HTTP status code (e.g., 200, 201)
     * @param <T>                   The result type
     * @return The response object
     */
    public <T> R executeWithResponse(
            String rateLimitKey,
            int rateLimit,
            Supplier<T> businessLogic,
            Supplier<R> responseSupplier,
            BiConsumer<R, Integer> responseStatusSetter,
            BiConsumer<R, String> responseMessageSetter,
            BiConsumer<R, T> resultSetter,
            String successMessage,
            int successStatusCode) {

        R response = responseSupplier.get();
        long startTime = System.currentTimeMillis();

        try {
            // Check rate limit
            if (!rateLimiterService.isAllowed(rateLimitKey, rateLimit, 60)) {
                log.warn("Rate limit exceeded for key: {}", rateLimitKey);
                responseStatusSetter.accept(response, 429);
                responseMessageSetter.accept(response, "Rate limit exceeded. Please try again later.");
                return response;
            }

            // Execute business logic
            T result = businessLogic.get();

            // Set success response
            responseStatusSetter.accept(response, successStatusCode);
            responseMessageSetter.accept(response, successMessage);
            if (result != null && resultSetter != null) {
                resultSetter.accept(response, result);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed request in {} ms", duration);

            return response;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            // Check if it's a custom exception with status code
            Integer statusCode = extractStatusCode(e);
            String errorMessage = e.getMessage();

            responseStatusSetter.accept(response, statusCode != null ? statusCode : 500);
            responseMessageSetter.accept(response, errorMessage != null ? errorMessage : "Internal server error");

            if (statusCode == null || statusCode >= 500) {
                log.error("Error in request (duration: {} ms): {}", duration, errorMessage, e);
            } else {
                log.warn("Client error in request (duration: {} ms): {}", duration, errorMessage);
            }

            return response;
        }
    }

    /**
     * Execute API logic without rate limiting (for internal/admin APIs)
     * Simplified version without rate limit check
     *
     * @param businessLogic         The business logic to execute
     * @param responseSupplier      Supplier for new Response instance
     * @param responseStatusSetter  Consumer to set status code on response
     * @param responseMessageSetter Consumer to set message on response
     * @param resultSetter          BiConsumer to set result on response
     * @param successMessage        Success message
     * @param successStatusCode     Success HTTP status code
     * @param <T>                   The result type
     * @return The response object
     */
    public <T> R executeWithoutRateLimit(
            Supplier<T> businessLogic,
            Supplier<R> responseSupplier,
            BiConsumer<R, Integer> responseStatusSetter,
            BiConsumer<R, String> responseMessageSetter,
            BiConsumer<R, T> resultSetter,
            String successMessage,
            int successStatusCode) {

        R response = responseSupplier.get();
        long startTime = System.currentTimeMillis();

        try {
            // Execute business logic
            T result = businessLogic.get();

            // Set success response
            responseStatusSetter.accept(response, successStatusCode);
            responseMessageSetter.accept(response, successMessage);
            if (result != null && resultSetter != null) {
                resultSetter.accept(response, result);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed request in {} ms", duration);

            return response;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            Integer statusCode = extractStatusCode(e);
            String errorMessage = e.getMessage();

            responseStatusSetter.accept(response, statusCode != null ? statusCode : 500);
            responseMessageSetter.accept(response, errorMessage != null ? errorMessage : "Internal server error");

            if (statusCode == null || statusCode >= 500) {
                log.error("Error in request (duration: {} ms): {}", duration, errorMessage, e);
            } else {
                log.warn("Client error in request (duration: {} ms): {}", duration, errorMessage);
            }

            return response;
        }
    }

    /**
     * Extract status code from exception
     * Override this method in subclass to support custom exception types
     * 
     * @param e The exception
     * @return The status code, or null if not found
     */
    protected Integer extractStatusCode(Exception e) {
        // Try to extract status code using reflection for common patterns
        try {
            var method = e.getClass().getMethod("getStatusCode");
            Object result = method.invoke(e);
            if (result instanceof Integer) {
                return (Integer) result;
            }
        } catch (Exception ignored) {
            // Method doesn't exist or failed to invoke
        }

        return null;
    }

    /**
     * Check rate limit only (useful for custom flows)
     * 
     * @param rateLimitKey The rate limit key
     * @param rateLimit    The rate limit threshold
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean checkRateLimit(String rateLimitKey, int rateLimit) {
        return rateLimiterService.isAllowed(rateLimitKey, rateLimit, 60);
    }
}
