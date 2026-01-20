package com.example.rediscommon.utils;

import java.util.UUID;

/**
 * Utility class for building consistent Redis cache keys across services
 * Provides standardized key generation patterns
 */
public class CacheKeyBuilder {

    private final String servicePrefix;

    private CacheKeyBuilder(String servicePrefix) {
        this.servicePrefix = servicePrefix;
    }

    /**
     * Create a cache key builder for a specific service
     * 
     * @param serviceName The service name (e.g., "blog", "user", "auth")
     * @return A new CacheKeyBuilder instance
     */
    public static CacheKeyBuilder forService(String serviceName) {
        return new CacheKeyBuilder(serviceName);
    }

    /**
     * Build a cache key for a method with no parameters
     * Pattern: service:methodName:all
     * 
     * @param methodName The method name
     * @return The cache key
     */
    public String forMethod(String methodName) {
        return String.format("%s:%s:all", servicePrefix, methodName);
    }

    /**
     * Build a cache key for a method with a single ID parameter
     * Pattern: service:methodName:id
     * 
     * @param methodName The method name
     * @param id         The ID parameter
     * @return The cache key
     */
    public String forMethodWithId(String methodName, UUID id) {
        return String.format("%s:%s:%s", servicePrefix, methodName, id.toString());
    }

    /**
     * Build a cache key for a method with a single string parameter
     * Pattern: service:methodName:param
     * 
     * @param methodName The method name
     * @param param      The string parameter
     * @return The cache key
     */
    public String forMethodWithParam(String methodName, String param) {
        return String.format("%s:%s:%s", servicePrefix, methodName, param);
    }

    /**
     * Build a cache key for a method with a single numeric parameter
     * Pattern: service:methodName:number
     * 
     * @param methodName The method name
     * @param number     The numeric parameter
     * @return The cache key
     */
    public String forMethodWithParam(String methodName, long number) {
        return String.format("%s:%s:%d", servicePrefix, methodName, number);
    }

    /**
     * Build a cache key for a method with two parameters
     * Pattern: service:methodName:param1:param2
     * 
     * @param methodName The method name
     * @param param1     First parameter
     * @param param2     Second parameter
     * @return The cache key
     */
    public String forMethodWithParams(String methodName, Object param1, Object param2) {
        return String.format("%s:%s:%s:%s", servicePrefix, methodName, param1.toString(), param2.toString());
    }

    /**
     * Build a cache key for a method with multiple parameters
     * Pattern: service:methodName:param1:param2:...:paramN
     * 
     * @param methodName The method name
     * @param params     Variable number of parameters
     * @return The cache key
     */
    public String forMethodWithParams(String methodName, Object... params) {
        StringBuilder keyBuilder = new StringBuilder()
                .append(servicePrefix)
                .append(":")
                .append(methodName);

        for (Object param : params) {
            keyBuilder.append(":").append(param.toString());
        }

        return keyBuilder.toString();
    }

    /**
     * Build a custom cache key with raw parts
     * Pattern: service:part1:part2:...:partN
     * 
     * @param parts Variable number of key parts
     * @return The cache key
     */
    public String custom(String... parts) {
        StringBuilder keyBuilder = new StringBuilder(servicePrefix);

        for (String part : parts) {
            keyBuilder.append(":").append(part);
        }

        return keyBuilder.toString();
    }

    /**
     * Get the service prefix
     * 
     * @return The service prefix
     */
    public String getServicePrefix() {
        return servicePrefix;
    }
}
