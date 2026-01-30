package com.example.statsservice.services.apis;

import com.example.rediscommon.services.RateLimiterService;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.example.statsservice.dtos.DashboardStatsDto;
import com.example.statsservice.dtos.responses.Response;
import com.example.statsservice.exceptions.OurException;
import com.example.statsservice.services.apis.handlers.StatsHandler;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StatsApi {

    private final RateLimiterService rateLimiterService;
    private final StatsHandler statsHandler;
    private final CacheKeyBuilder cacheKeys;

    public StatsApi(
            RateLimiterService rateLimiterService,
            StatsHandler statsHandler) {
        this.rateLimiterService = rateLimiterService;
        this.statsHandler = statsHandler;
        this.cacheKeys = CacheKeyBuilder.forService("stats");
    }

    private long requestStart(String message) {
        log.info(message);
        return System.currentTimeMillis();
    }

    private void requestEnd(long startTime) {
        long endTime = System.currentTimeMillis();
        log.info("Completed request in {} ms", endTime - startTime);
    }

    private void checkRateLimit(String rateLimitKey, int maxRequests, int timeWindowSeconds) {
        if (!rateLimiterService.isAllowed(rateLimitKey, maxRequests, timeWindowSeconds)) {
            throw new OurException("Rate limit exceeded. Please try again later.", 429);
        }
    }

    public Response getDashboardStats() {
        long startTime = requestStart("Get dashboard stats attempt");

        try {
            String rateLimitKey = cacheKeys.forMethod("getDashboardStats");
            checkRateLimit(rateLimitKey, 45, 60);

            DashboardStatsDto stats = statsHandler.handleGetDashboardStats();

            Response response = new Response("Dashboard statistics retrieved successfully");
            response.setDashboardStats(stats);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getStatsReport() {
        long startTime = requestStart("Get stats report attempt");

        try {
            String rateLimitKey = cacheKeys.forMethod("getStatsReport");
            checkRateLimit(rateLimitKey, 45, 60);

            byte[] report = statsHandler.handleGetStatsReport();

            Response response = new Response("Stats report retrieved successfully");
            response.setStatsReport(report);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }
}
