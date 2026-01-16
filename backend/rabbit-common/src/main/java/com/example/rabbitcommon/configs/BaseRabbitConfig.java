package com.example.rabbitcommon.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.rabbitcommon.dtos.RPCResponse;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class BaseRabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();

        // Map user-service RPCResponse to auth-service RPCResponse
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.example.rabbitcommon.dtos.RPCResponse", RPCResponse.class);
        classMapper.setIdClassMapping(idClassMapping);

        // Trust all packages (allows dynamic deserialization)
        classMapper.setTrustedPackages("*");

        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setReplyTimeout(120000); // 120 seconds - increased for AI processing
        template.setReceiveTimeout(120000); // 120 seconds
        template.setUseDirectReplyToContainer(true); // Enable direct reply-to optimization
        return template;
    }
}