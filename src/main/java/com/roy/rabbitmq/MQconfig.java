package com.roy.rabbitmq;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Queue;

@Configuration
public class MQconfig {
    public static final String QUEUE = "queue";
    public static final String MIAOSHA_QUEUE = "miaosha.queue";

    @Bean
    public Queue queue(){
        return new Queue(QUEUE,true);
    }
}
