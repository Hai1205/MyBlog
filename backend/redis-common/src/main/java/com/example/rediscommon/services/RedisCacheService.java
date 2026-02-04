package com.example.rediscommon.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    private static final long DEFAULT_TTL = 10;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

    public <T> T getCacheData(String cacheKey, Class<T> type) {
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for key: {}", cacheKey);
                return convertCached(cached, type);
            }

            return null;
        } catch (Exception e) {
            log.error("Internal Server Error", e);
            return null;
        }
    }

    public void setCacheData(String cacheKey, Object data, long ttl, TimeUnit timeUnit) {
        try {
            if (data != null) {
                redisService.set(cacheKey, data, ttl, timeUnit);
                log.debug("Cached data for key: {}", cacheKey);
            }
        } catch (Exception e) {
            log.error("Internal Server Error", e);
        }
    }

    public void setCacheData(String cacheKey, Object data) {
        setCacheData(cacheKey, data, DEFAULT_TTL, DEFAULT_TIME_UNIT);
    }

    public void deleteCacheData(String cacheKey) {
        try {
            redisService.delete(cacheKey);
            log.debug("Deleted cache for key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Error deleting cache for key: {}", cacheKey, e);
        }
    }

    public <T> List<T> getCacheDataList(String cacheKey, Class<T> type) {
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for key: {}", cacheKey);
                return convertCachedList(cached, type);
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private <T> T convertCached(Object cached, Class<T> type) {
        if (type.isInstance(cached)) {
            return type.cast(cached);
        }
        return objectMapper.convertValue(cached, type);
    }

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
