package com.roy.rabbitmq;

import com.roy.util.JedisAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {
    private static Logger logger = LoggerFactory.getLogger(MQSender.class);

    @Autowired
    AmqpTemplate amqpTemplate;

//    public void send(Object message){
//        String msg = JedisAdapter.beanToString(message);
//        logger.info("send message:"+msg);
//        amqpTemplate.convertAndSend(MQconfig.QUEUE,msg);
//    }

    public void sendMiaoshaMessage(MiaoshaMessage miaoshaMessage) {
        String msg = JedisAdapter.beanToString(miaoshaMessage);
        logger.info("send message:"+msg);
        amqpTemplate.convertAndSend(MQconfig.MIAOSHA_QUEUE,msg);
    }
}
