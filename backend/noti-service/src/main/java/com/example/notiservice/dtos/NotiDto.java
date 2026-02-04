package com.example.notiservice.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.example.notiservice.entities.Noti.NotificationType;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiDto {
    private UUID id;
    private UUID blogId;
    private UUID authorId;
    private UUID receiverId;
    private String content;
    private NotificationType type;
    private Boolean isRead;
    private Instant createdAt;
    private Instant updatedAt;
}
