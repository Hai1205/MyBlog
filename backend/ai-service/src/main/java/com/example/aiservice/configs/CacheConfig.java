package com.example.aiservice.configs;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache Manager with 2 caches:
     * 1. embeddingCache: For embedding vectors (5min TTL)
     * 2. searchCache: For vector search results (3min TTL)
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "embeddingCache", 
                "searchCache",
                "jdMatchCache");
        
        // Embedding cache: longer TTL, smaller size
        cacheManager.registerCustomCache("embeddingCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(100)
                        .recordStats()
                        .build());
        
        // Search cache: shorter TTL, larger size
        cacheManager.registerCustomCache("searchCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(3, TimeUnit.MINUTES)
                        .maximumSize(500)
                        .recordStats()
                        .build());

        // JD Match cache: longer TTL due to expensive operation
        cacheManager.registerCustomCache("jdMatchCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(200)
                        .recordStats()
                        .build());
        
        return cacheManager;
    }
}