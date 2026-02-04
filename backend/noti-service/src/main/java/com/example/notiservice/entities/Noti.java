package com.example.notiservice.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Noti {
    @Id
    private UUID id;
    
    private UUID blogId;
    private UUID authorId;
    private UUID receiverId;
    private String content;
    private NotificationType type;
    private Boolean isRead;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum NotificationType {
        FOLLOW,
        LIKE,
        COMMENT,
        NEW_BLOG
    }
}
