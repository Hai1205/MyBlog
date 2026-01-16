package com.example.mailservice.services.rabbitmqs.consumers;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.example.mailservice.services.apis.MailApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailConsumer {

    private final MailApi mailService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "auth.mail.send-mail-activation.queue", durable = "true", autoDelete = "false"), exchange = @Exchange(name = "auth.mail.exchange", type = "topic", durable = "true"), key = "auth.mail.send-mail-activation.request"))
    public void sendMailActivation(@Payload Map<String, Object> message) {
        String email = (String) message.get("email");
        String otp = (String) message.get("otp");

        log.info("Received activation mail request for email: {}", email);
        mailService.sendMailActivation(email, otp);
        log.info("Activation mail sent successfully to: {}", email);
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "auth.mail.send-mail-reset-password.queue", durable = "true", autoDelete = "false"), exchange = @Exchange(name = "auth.mail.exchange", type = "topic", durable = "true"), key = "auth.mail.send-mail-reset-password.request"))
    public void sendMailResetPassword(@Payload Map<String, Object> message) {
        String email = (String) message.get("email");
        String newPassword = (String) message.get("newPassword");

        log.info("Received reset password mail request for email: {}", email);
        mailService.sendMailResetPassword(email, newPassword);
        log.info("Reset password mail sent successfully to: {}", email);
    }
}
