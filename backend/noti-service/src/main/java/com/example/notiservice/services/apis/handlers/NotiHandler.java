package com.example.notiservice.services.apis.handlers;

import com.example.notiservice.dtos.NotiDto;
import com.example.notiservice.entities.Noti;
import com.example.notiservice.entities.Noti.NotificationType;
import com.example.notiservice.exceptions.OurException;
import com.example.notiservice.mappers.NotiMapper;
import com.example.notiservice.repositories.NotiCommandRepository;
import com.example.notiservice.repositories.NotiQueryRepository;
import com.example.notiservice.repositories.SimpleNotiRepository;
import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.utils.CacheKeyBuilder;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotiHandler {
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    private final NotiQueryRepository notiQueryRepository;
    private final NotiCommandRepository notiCommandRepository;
    private final SimpleNotiRepository simpleNotiRepository;
    private final NotiMapper notiMapper;
    private final RedisCacheService cacheService;
    private final CacheKeyBuilder cacheKeys;

    public NotiHandler(
            NotiQueryRepository notiQueryRepository,
            NotiCommandRepository notiCommandRepository,
            SimpleNotiRepository simpleNotiRepository,
            NotiMapper notiMapper,
            RedisCacheService cacheService) {
        this.notiQueryRepository = notiQueryRepository;
        this.notiCommandRepository = notiCommandRepository;
        this.simpleNotiRepository = simpleNotiRepository;
        this.notiMapper = notiMapper;
        this.cacheService = cacheService;
        this.cacheKeys = CacheKeyBuilder.forService("noti");
    }

    // Get all notifications
    public List<NotiDto> handleGetAllNotifications() {
        try {
            String cacheKey = cacheKeys.forMethod("handleGetAllNotifications");
            List<NotiDto> notifications = cacheService.getCacheDataList(cacheKey, NotiDto.class);

            if (notifications == null) {
                log.debug("Cache miss for handleGetAllNotifications, fetching from database");
                notifications = notiQueryRepository.findAllNotis(Pageable.unpaged()).getContent().stream()
                        .map(notiMapper::toDto)
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .collect(Collectors.toList());

                cacheService.setCacheData(cacheKey, notifications);
                log.debug("Fetched {} notifications from database and cached", notifications.size());
            }

            log.info("Completed handleGetAllNotifications with {} notifications", notifications.size());
            return notifications;
        } catch (Exception e) {
            log.error("Error fetching all notifications: {}", e.getMessage(), e);
            throw new OurException("Failed to fetch notifications", 500);
        }
    }

    // Get notifications for a specific user
    public List<NotiDto> handleGetUserNotifications(UUID userId) {
        try {
            String cacheKey = cacheKeys.forMethodWithId("handleGetUserNotifications", userId);
            List<NotiDto> notifications = cacheService.getCacheDataList(cacheKey, NotiDto.class);

            if (notifications == null) {
                log.debug("Cache miss for handleGetUserNotifications:{}, fetching from database", userId);
                notifications = notiQueryRepository.findByReceiverIdOrderByCreatedAtDesc(userId).stream()
                        .map(notiMapper::toDto)
                        .collect(Collectors.toList());

                cacheService.setCacheData(cacheKey, notifications);
                log.debug("Fetched {} notifications for user {} from database and cached", notifications.size(), userId);
            }

            log.info("Completed handleGetUserNotifications for user {} with {} notifications", userId, notifications.size());
            return notifications;
        } catch (Exception e) {
            log.error("Error fetching user notifications: {}", e.getMessage(), e);
            throw new OurException("Failed to fetch user notifications", 500);
        }
    }

    // Get a single notification
    public NotiDto handleGetNotification(UUID notiId) {
        try {
            String cacheKey = cacheKeys.forMethodWithId("handleGetNotification", notiId);
            NotiDto notification = cacheService.getCacheData(cacheKey, NotiDto.class);

            if (notification == null) {
                log.debug("Cache miss for handleGetNotification:{}, fetching from database", notiId);
                Noti noti = notiQueryRepository.findNotiById(notiId)
                        .orElseThrow(() -> new OurException("Notification not found", 404));
                notification = notiMapper.toDto(noti);

                cacheService.setCacheData(cacheKey, notification);
                log.debug("Fetched notification {} from database and cached", notiId);
            }

            log.info("Completed handleGetNotification for notification: {}", notiId);
            return notification;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching notification: {}", e.getMessage(), e);
            throw new OurException("Failed to fetch notification", 500);
        }
    }

    // Create a new notification
    public NotiDto handleCreateNotification(UUID blogId, UUID authorId, UUID receiverId, 
                                           String content, NotificationType type) {
        try {
            log.info("Creating notification for user: {}", receiverId);
            
            Noti noti = Noti.builder()
                    .id(UUID.randomUUID())
                    .blogId(blogId)
                    .authorId(authorId)
                    .receiverId(receiverId)
                    .content(content)
                    .type(type)
                    .isRead(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            Noti savedNoti = simpleNotiRepository.save(noti);
            
            // Invalidate caches
            cacheService.deleteCacheData(cacheKeys.forMethod("handleGetAllNotifications"));
            cacheService.deleteCacheData(cacheKeys.forMethodWithId("handleGetUserNotifications", receiverId));
            log.debug("Cache invalidated after creating notification");
            
            // Send real-time notification via SSE
            sendRealtimeNotification(receiverId, savedNoti);
            
            return notiMapper.toDto(savedNoti);
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage(), e);
            throw new OurException("Failed to create notification", 500);
        }
    }

    // Delete a notification
    public void handleDeleteNotification(UUID notiId) {
        try {
            log.info("Deleting notification: {}", notiId);
            
            Noti noti = notiQueryRepository.findNotiById(notiId)
                    .orElseThrow(() -> new OurException("Notification not found", 404));
            
            notiCommandRepository.deleteNotiById(notiId);
            
            // Invalidate caches
            cacheService.deleteCacheData(cacheKeys.forMethod("handleGetAllNotifications"));
            cacheService.deleteCacheData(cacheKeys.forMethodWithId("handleGetUserNotifications", noti.getReceiverId()));
            cacheService.deleteCacheData(cacheKeys.forMethodWithId("handleGetNotification", notiId));
            log.debug("Cache invalidated after deleting notification");
            
            log.info("Notification deleted successfully: {}", notiId);
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting notification: {}", e.getMessage(), e);
            throw new OurException("Failed to delete notification", 500);
        }
    }

    // Delete all notifications for a user
    public void handleDeleteUserNotifications(UUID userId) {
        try {
            log.info("Deleting all notifications for user: {}", userId);
            long deletedCount = notiCommandRepository.deleteByReceiverId(userId);
            
            // Invalidate caches
            cacheService.deleteCacheData(cacheKeys.forMethod("handleGetAllNotifications"));
            cacheService.deleteCacheData(cacheKeys.forMethodWithId("handleGetUserNotifications", userId));
            log.debug("Cache invalidated after deleting user notifications");
            
            log.info("Deleted {} notifications for user: {}", deletedCount, userId);
        } catch (Exception e) {
            log.error("Error deleting user notifications: {}", e.getMessage(), e);
            throw new OurException("Failed to delete user notifications", 500);
        }
    }

    // Mark a notification as read
    public NotiDto handleMarkNotification(UUID notiId) {
        try {
            log.info("Marking notification as read: {}", notiId);
            
            Noti noti = notiQueryRepository.findNotiById(notiId)
                    .orElseThrow(() -> new OurException("Notification not found", 404));
            
            noti.setIsRead(true);
            noti.setUpdatedAt(Instant.now());
            
            Noti updatedNoti = simpleNotiRepository.save(noti);
            
            // Invalidate caches
            cacheService.deleteCacheData(cacheKeys.forMethod("handleGetAllNotifications"));
            cacheService.deleteCacheData(cacheKeys.forMethodWithId("handleGetUserNotifications", noti.getReceiverId()));
            cacheService.deleteCacheData(cacheKeys.forMethodWithId("handleGetNotification", notiId));
            log.debug("Cache invalidated after marking notification");
            
            return notiMapper.toDto(updatedNoti);
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error marking notification: {}", e.getMessage(), e);
            throw new OurException("Failed to mark notification", 500);
        }
    }

    // Mark all notifications as read for a user
    public void handleMarkUserNotifications(UUID userId) {
        try {
            log.info("Marking all notifications as read for user: {}", userId);
            
            List<Noti> notifications = notiQueryRepository.findUnreadByReceiverId(userId);
            notifications.forEach(noti -> {
                noti.setIsRead(true);
                noti.setUpdatedAt(Instant.now());
            });
            
            simpleNotiRepository.saveAll(notifications);
            
            // Invalidate caches
            cacheService.deleteCacheData(cacheKeys.forMethod("handleGetAllNotifications"));
            cacheService.deleteCacheData(cacheKeys.forMethodWithId("handleGetUserNotifications", userId));
            // Invalidate individual notification caches
            notifications.forEach(noti -> 
                cacheService.deleteCacheData(cacheKeys.forMethodWithId("handleGetNotification", noti.getId()))
            );
            log.debug("Cache invalidated after marking user notifications");
            
            log.info("Marked {} notifications as read for user: {}", notifications.size(), userId);
        } catch (Exception e) {
            log.error("Error marking user notifications: {}", e.getMessage(), e);
            throw new OurException("Failed to mark user notifications", 500);
        }
    }

    // SSE Subscribe
    public SseEmitter handleSubscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitter.onCompletion(() -> {
            log.info("SSE completed for user: {}", userId);
            emitters.remove(userId);
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE timeout for user: {}", userId);
            emitters.remove(userId);
        });
        
        emitter.onError((ex) -> {
            log.error("SSE error for user: {}", userId, ex);
            emitters.remove(userId);
        });
        
        emitters.put(userId, emitter);
        log.info("User {} subscribed to notifications", userId);
        
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to notification service"));
        } catch (IOException e) {
            log.error("Error sending initial message", e);
        }
        
        return emitter;
    }

    // SSE Unsubscribe
    public void handleUnsubscribe(UUID userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
            log.info("User {} unsubscribed", userId);
        }
    }

    // Send real-time notification
    private void sendRealtimeNotification(UUID receiverId, Noti notification) {
        SseEmitter emitter = emitters.get(receiverId);
        
        if (emitter == null) {
            log.warn("User {} is not connected", receiverId);
            return;
        }

        try {
            NotiDto notiDto = notiMapper.toDto(notification);
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notiDto));
            log.info("Notification sent to user {}", receiverId);
        } catch (IOException e) {
            log.error("Error sending notification to {}", receiverId, e);
            emitters.remove(receiverId);
        }
    }

    // Check if user is online
    public boolean handleCheckUserOnline(UUID userId) {
        return emitters.containsKey(userId);
    }
}