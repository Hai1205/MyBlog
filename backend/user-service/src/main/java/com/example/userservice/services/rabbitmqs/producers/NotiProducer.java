package com.example.userservice.services.rabbitmqs.producers;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.rabbitcommon.dtos.NotificationMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotiProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendFollowNotification(NotificationMessage message) {
        try {
            String exchange = "user.noti.exchange";
            String routingKey = "user.noti.follow.request";

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Sent follow notification: authorId={}, receiverId={}",
                    message.getAuthorId(), message.getReceiverId());
        } catch (Exception e) {
            log.error("Error sending follow notification: {}", e.getMessage(), e);
        }
    }
}
