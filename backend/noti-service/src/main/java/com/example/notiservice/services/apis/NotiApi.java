package com.example.notiservice.services.apis;

import com.example.notiservice.dtos.NotiDto;
import com.example.notiservice.dtos.requests.CreateNotiRequest;
import com.example.notiservice.dtos.responses.Response;
import com.example.notiservice.exceptions.OurException;
import com.example.notiservice.services.apis.handlers.NotiHandler;
import com.example.rediscommon.services.RateLimiterService;
import com.example.rediscommon.utils.CacheKeyBuilder;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotiApi {
    private final NotiHandler notiHandler;
    private final RateLimiterService rateLimiterService;
    private final CacheKeyBuilder cacheKeys;

    public NotiApi(
            NotiHandler notiHandler,
            RateLimiterService rateLimiterService) {
        this.notiHandler = notiHandler;
        this.rateLimiterService = rateLimiterService;
        this.cacheKeys = CacheKeyBuilder.forService("noti");
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

    public Response GetAllNotifications() {
        long startTime = requestStart("Get all notifications attempt");

        try {
            String rateLimitKey = cacheKeys.forMethod("getAllNotifications");
            checkRateLimit(rateLimitKey, 100, 60);

            List<NotiDto> notifications = notiHandler.handleGetAllNotifications();

            Response response = new Response("Notifications fetched successfully", 200);
            response.setNotifications(notifications);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Error in GetAllNotifications: {}", e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getUserNotifications(UUID userId) {
        long startTime = requestStart("Get user notifications attempt for user: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethod("getUserNotifications:" + userId);
            checkRateLimit(rateLimitKey, 100, 60);

            List<NotiDto> notifications = notiHandler.handleGetUserNotifications(userId);

            Response response = new Response("User notifications fetched successfully", 200);
            response.setNotifications(notifications);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Error in getUserNotifications: {}", e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response getNotification(UUID notiId) {
        long startTime = requestStart("Get notification attempt: " + notiId);

        try {
            String rateLimitKey = cacheKeys.forMethod("getNotification");
            checkRateLimit(rateLimitKey, 100, 60);

            NotiDto notification = notiHandler.handleGetNotification(notiId);

            Response response = new Response("Notification fetched successfully", 200);
            response.setNotification(notification);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Error in getNotification: {}", e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response createNotification(CreateNotiRequest request) {
        long startTime = requestStart("Create notification attempt");

        try {
            String rateLimitKey = cacheKeys.forMethod("createNotification");
            checkRateLimit(rateLimitKey, 50, 60);

            NotiDto notification = notiHandler.handleCreateNotification(
                    request.getBlogId(),
                    request.getAuthorId(),
                    request.getReceiverId(),
                    request.getContent(),
                    request.getType());

            Response response = new Response("Notification created successfully", 201);
            response.setNotification(notification);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Error in createNotification: {}", e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response deleteNotification(UUID notiId) {
        long startTime = requestStart("Delete notification attempt: " + notiId);

        try {
            String rateLimitKey = cacheKeys.forMethod("deleteNotification");
            checkRateLimit(rateLimitKey, 50, 60);

            notiHandler.handleDeleteNotification(notiId);

            return new Response("Notification deleted successfully", 200);
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Error in deleteNotification: {}", e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response deleteUserNotifications(UUID userId) {
        long startTime = requestStart("Delete user notifications attempt for user: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethod("deleteUserNotifications:" + userId);
            checkRateLimit(rateLimitKey, 20, 60);

            notiHandler.handleDeleteUserNotifications(userId);

            return new Response("User notifications deleted successfully", 200);
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Error in deleteUserNotifications: {}", e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response markNotification(UUID notiId) {
        long startTime = requestStart("Mark notification attempt: " + notiId);

        try {
            String rateLimitKey = cacheKeys.forMethod("markNotification");
            checkRateLimit(rateLimitKey, 100, 60);

            NotiDto notification = notiHandler.handleMarkNotification(notiId);

            Response response = new Response("Notification marked as read", 200);
            response.setNotification(notification);
            return response;
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Error in markNotification: {}", e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public Response markUserNotification(UUID userId) {
        long startTime = requestStart("Mark user notifications attempt for user: " + userId);

        try {
            String rateLimitKey = cacheKeys.forMethod("markUserNotification:" + userId);
            checkRateLimit(rateLimitKey, 50, 60);

            notiHandler.handleMarkUserNotifications(userId);

            return new Response("User notifications marked as read", 200);
        } catch (OurException e) {
            return new Response(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Error in markUserNotification: {}", e.getMessage(), e);
            return new Response("Internal Server Error", 500);
        } finally {
            requestEnd(startTime);
        }
    }

    public SseEmitter subscribe(UUID userId) {
        return notiHandler.handleSubscribe(userId);
    }

    public void unsubscribe(UUID userId) {
        notiHandler.handleUnsubscribe(userId);
    }
}
