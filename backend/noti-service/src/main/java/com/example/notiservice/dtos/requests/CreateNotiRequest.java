package com.example.notiservice.dtos.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.example.notiservice.entities.Noti.NotificationType;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotiRequest {
    private UUID blogId;
    private UUID authorId;
    private UUID receiverId;
    private String content;
    private NotificationType type;
}
