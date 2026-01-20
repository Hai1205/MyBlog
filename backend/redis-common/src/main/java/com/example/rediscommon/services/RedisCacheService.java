package com.example.rediscommon.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Enhanced Redis Cache Service with generic caching patterns
 * Provides high-level caching operations with automatic
 * serialization/deserialization
 * Can be used across all microservices for consistent caching behavior
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    private static final long DEFAULT_TTL = 10;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

    /**
     * Execute with cache for single object
     * If cache exists, return from cache
     * Otherwise, execute fetcher, cache the result, and return
     *
     * @param cacheKey The Redis cache key
     * @param type     The class type to deserialize to
     * @param fetcher  The function to fetch data if cache misses
     * @param <T>      The return type
     * @return The cached or fetched data
     */
    public <T> T executeWithCache(String cacheKey, Class<T> type, Supplier<T> fetcher) {
        return executeWithCache(cacheKey, type, fetcher, DEFAULT_TTL, DEFAULT_TIME_UNIT);
    }

    /**
     * Execute with cache for single object with custom TTL
     *
     * @param cacheKey The Redis cache key
     * @param type     The class type to deserialize to
     * @param fetcher  The function to fetch data if cache misses
     * @param ttl      Time to live for cache
     * @param timeUnit Time unit for TTL
     * @param <T>      The return type
     * @return The cached or fetched data
     */
    public <T> T executeWithCache(String cacheKey, Class<T> type, Supplier<T> fetcher, long ttl, TimeUnit timeUnit) {
        try {
            // Try to get from cache
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for key: {}", cacheKey);
                return convertCached(cached, type);
            }

            // Cache miss - fetch from data source
            log.debug("Cache miss for key: {}", cacheKey);
            T result = fetcher.get();

            // Store in cache
            if (result != null) {
                redisService.set(cacheKey, result, ttl, timeUnit);
                log.debug("Cached result for key: {}", cacheKey);
            }

            return result;
        } catch (Exception e) {
            log.error("Error in executeWithCache for key {}: {}", cacheKey, e.getMessage(), e);
            // On cache error, still try to fetch from source
            return fetcher.get();
        }
    }

    /**
     * Execute with cache for list of objects
     * If cache exists, return from cache
     * Otherwise, execute fetcher, cache the result, and return
     *
     * @param cacheKey The Redis cache key
     * @param type     The class type of list elements
     * @param fetcher  The function to fetch data if cache misses
     * @param <T>      The type of list elements
     * @return The cached or fetched list
     */
    public <T> List<T> executeWithCacheList(String cacheKey, Class<T> type, Supplier<List<T>> fetcher) {
        return executeWithCacheList(cacheKey, type, fetcher, DEFAULT_TTL, DEFAULT_TIME_UNIT);
    }

    /**
     * Execute with cache for list of objects with custom TTL
     *
     * @param cacheKey The Redis cache key
     * @param type     The class type of list elements
     * @param fetcher  The function to fetch data if cache misses
     * @param ttl      Time to live for cache
     * @param timeUnit Time unit for TTL
     * @param <T>      The type of list elements
     * @return The cached or fetched list
     */
    public <T> List<T> executeWithCacheList(String cacheKey, Class<T> type, Supplier<List<T>> fetcher, long ttl,
            TimeUnit timeUnit) {
        try {
            // Try to get from cache
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for key: {}", cacheKey);
                return convertCachedList(cached, type);
            }

            // Cache miss - fetch from data source
            log.debug("Cache miss for key: {}", cacheKey);
            List<T> result = fetcher.get();

            // Store in cache
            if (result != null) {
                redisService.set(cacheKey, result, ttl, timeUnit);
                log.debug("Cached list result for key: {} with {} items", cacheKey, result.size());
            }

            return result;
        } catch (Exception e) {
            log.error("Error in executeWithCacheList for key {}: {}", cacheKey, e.getMessage(), e);
            // On cache error, still try to fetch from source
            return fetcher.get();
        }
    }

    /**
     * Execute with cache for primitive/wrapper types (Long, Integer, String, etc.)
     *
     * @param cacheKey The Redis cache key
     * @param fetcher  The function to fetch data if cache misses
     * @param <T>      The return type
     * @return The cached or fetched data
     */
    public <T> T executeWithCachePrimitive(String cacheKey, Supplier<T> fetcher) {
        return executeWithCachePrimitive(cacheKey, fetcher, DEFAULT_TTL, DEFAULT_TIME_UNIT);
    }

    /**
     * Execute with cache for primitive/wrapper types with custom TTL
     *
     * @param cacheKey The Redis cache key
     * @param fetcher  The function to fetch data if cache misses
     * @param ttl      Time to live for cache
     * @param timeUnit Time unit for TTL
     * @param <T>      The return type
     * @return The cached or fetched data
     */
    @SuppressWarnings("unchecked")
    public <T> T executeWithCachePrimitive(String cacheKey, Supplier<T> fetcher, long ttl, TimeUnit timeUnit) {
        try {
            // Try to get from cache
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for key: {}", cacheKey);
                return (T) cached;
            }

            // Cache miss - fetch from data source
            log.debug("Cache miss for key: {}", cacheKey);
            T result = fetcher.get();

            // Store in cache
            if (result != null) {
                redisService.set(cacheKey, result, ttl, timeUnit);
                log.debug("Cached primitive result for key: {}", cacheKey);
            }

            return result;
        } catch (Exception e) {
            log.error("Error in executeWithCachePrimitive for key {}: {}", cacheKey, e.getMessage(), e);
            // On cache error, still try to fetch from source
            return fetcher.get();
        }
    }

    /**
     * Invalidate cache by key
     *
     * @param cacheKey The cache key to invalidate
     */
    public void invalidate(String cacheKey) {
        try {
            redisService.delete(cacheKey);
            log.debug("Invalidated cache for key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Error invalidating cache for key {}: {}", cacheKey, e.getMessage(), e);
        }
    }

    /**
     * Invalidate multiple cache keys
     *
     * @param cacheKeys The cache keys to invalidate
     */
    public void invalidateMultiple(String... cacheKeys) {
        for (String key : cacheKeys) {
            invalidate(key);
        }
    }

    /**
     * Convert cached object to specified type
     * Handles LinkedHashMap from Redis deserialization
     */
    private <T> T convertCached(Object cached, Class<T> type) {
        if (type.isInstance(cached)) {
            return type.cast(cached);
        }
        return objectMapper.convertValue(cached, type);
    }

    /**
     * Convert cached list to specified type
     * Handles List of LinkedHashMap from Redis deserialization
     */
    private <T> List<T> convertCachedList(Object cached, Class<T> type) {
        if (cached instanceof List) {
            List<?> list = (List<?>) cached;
            return list.stream()
                    .map(item -> objectMapper.convertValue(item, type))
                    .collect(Collectors.toList());
        }
        throw new IllegalArgumentException("Cached object is not a List");
    }
}
