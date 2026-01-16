package com.example.authservice.services.rabbitmqs.producers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthProducer {
    private final RabbitTemplate rabbitTemplate;

    public AuthProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMailActivation(String email, String otp) {
        String exchange = "auth.mail.exchange";
        String routingKey = "auth.mail.send-mail-activation.request";

        Map<String, Object> message = new HashMap<>();
        message.put("email", email);
        message.put("otp", otp);

        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    public void sendMailResetPassword(String email, String newPassword) {
        String exchange = "auth.mail.exchange";
        String routingKey = "auth.mail.send-mail-reset-password.request";

        Map<String, Object> message = new HashMap<>();
        message.put("email", email);
        message.put("newPassword", newPassword);

        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
