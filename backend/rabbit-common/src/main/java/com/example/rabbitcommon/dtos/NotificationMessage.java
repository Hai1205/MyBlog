package com.example.rabbitcommon.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID authorId; // User who triggered the notification
    private UUID receiverId; // User who receives the notification
    private UUID blogId; // Related blog ID (nullable)
    private String content; // Notification content/message
    private String type; // FOLLOW, LIKE, COMMENT, NEW_BLOG
}
