package com.example.blogservice.services.rabbitmqs.producers;

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

    public void sendLikeNotification(NotificationMessage message) {
        try {
            String exchange = "blog.noti.exchange";
            String routingKey = "blog.noti.like.request";

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Sent like notification: authorId={}, receiverId={}, blogId={}",
                    message.getAuthorId(), message.getReceiverId(), message.getBlogId());
        } catch (Exception e) {
            log.error("Error sending like notification: {}", e.getMessage(), e);
        }
    }

    public void sendCommentNotification(NotificationMessage message) {
        try {
            String exchange = "blog.noti.exchange";
            String routingKey = "blog.noti.comment.request";

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Sent comment notification: authorId={}, receiverId={}, blogId={}",
                    message.getAuthorId(), message.getReceiverId(), message.getBlogId());
        } catch (Exception e) {
            log.error("Error sending comment notification: {}", e.getMessage(), e);
        }
    }

    public void sendNewBlogNotification(NotificationMessage message) {
        try {
            String exchange = "blog.noti.exchange";
            String routingKey = "blog.noti.new-blog.request";

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Sent new blog notification: authorId={}, receiverId={}, blogId={}",
                    message.getAuthorId(), message.getReceiverId(), message.getBlogId());
        } catch (Exception e) {
            log.error("Error sending new blog notification: {}", e.getMessage(), e);
        }
    }
}
