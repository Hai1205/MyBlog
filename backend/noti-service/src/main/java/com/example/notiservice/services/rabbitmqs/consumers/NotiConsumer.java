package com.example.notiservice.services.rabbitmqs.consumers;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.example.notiservice.entities.Noti.NotificationType;
import com.example.notiservice.services.apis.handlers.NotiHandler;
import com.example.rabbitcommon.dtos.NotificationMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotiConsumer {

    private final NotiHandler notiHandler;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = "user.noti.follow.queue", durable = "true", autoDelete = "false"),
        exchange = @Exchange(name = "user.noti.exchange", type = "topic", durable = "true"),
        key = "user.noti.follow.request"
    ))
    public void handleFollowNotification(@Payload NotificationMessage message) {
        try {
            log.info("Received follow notification: authorId={}, receiverId={}", 
                    message.getAuthorId(), message.getReceiverId());
            
            notiHandler.handleCreateNotification(
                    null, // no blogId for follow
                    message.getAuthorId(),
                    message.getReceiverId(),
                    message.getContent(),
                    NotificationType.FOLLOW
            );
            
            log.info("Follow notification created successfully for receiverId={}", message.getReceiverId());
        } catch (Exception e) {
            log.error("Error handling follow notification: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = "blog.noti.like.queue", durable = "true", autoDelete = "false"),
        exchange = @Exchange(name = "blog.noti.exchange", type = "topic", durable = "true"),
        key = "blog.noti.like.request"
    ))
    public void handleLikeNotification(@Payload NotificationMessage message) {
        try {
            log.info("Received like notification: authorId={}, receiverId={}, blogId={}", 
                    message.getAuthorId(), message.getReceiverId(), message.getBlogId());
            
            notiHandler.handleCreateNotification(
                    message.getBlogId(),
                    message.getAuthorId(),
                    message.getReceiverId(),
                    message.getContent(),
                    NotificationType.LIKE
            );
            
            log.info("Like notification created successfully for receiverId={}", message.getReceiverId());
        } catch (Exception e) {
            log.error("Error handling like notification: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = "blog.noti.comment.queue", durable = "true", autoDelete = "false"),
        exchange = @Exchange(name = "blog.noti.exchange", type = "topic", durable = "true"),
        key = "blog.noti.comment.request"
    ))
    public void handleCommentNotification(@Payload NotificationMessage message) {
        try {
            log.info("Received comment notification: authorId={}, receiverId={}, blogId={}", 
                    message.getAuthorId(), message.getReceiverId(), message.getBlogId());
            
            notiHandler.handleCreateNotification(
                    message.getBlogId(),
                    message.getAuthorId(),
                    message.getReceiverId(),
                    message.getContent(),
                    NotificationType.COMMENT
            );
            
            log.info("Comment notification created successfully for receiverId={}", message.getReceiverId());
        } catch (Exception e) {
            log.error("Error handling comment notification: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = "blog.noti.new-blog.queue", durable = "true", autoDelete = "false"),
        exchange = @Exchange(name = "blog.noti.exchange", type = "topic", durable = "true"),
        key = "blog.noti.new-blog.request"
    ))
    public void handleNewBlogNotification(@Payload NotificationMessage message) {
        try {
            log.info("Received new blog notification: authorId={}, receiverId={}, blogId={}", 
                    message.getAuthorId(), message.getReceiverId(), message.getBlogId());
            
            notiHandler.handleCreateNotification(
                    message.getBlogId(),
                    message.getAuthorId(),
                    message.getReceiverId(),
                    message.getContent(),
                    NotificationType.NEW_BLOG
            );
            
            log.info("New blog notification created successfully for receiverId={}", message.getReceiverId());
        } catch (Exception e) {
            log.error("Error handling new blog notification: {}", e.getMessage(), e);
        }
    }
}
