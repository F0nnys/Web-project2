package com.roy.rabbitmq;

import com.roy.domain.MiaoshaOrder;
import com.roy.domain.MiaoshaUser;
import com.roy.domain.OrderInfo;
import com.roy.service.GoodsService;
import com.roy.service.MiaoshaService;
import com.roy.service.OrderService;
import com.roy.util.JedisAdapter;
import com.roy.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;
    @Autowired
    MiaoshaService miaoshaService;

    private static Logger logger = LoggerFactory.getLogger(MQReceiver.class);

    @RabbitListener(queues = MQconfig.MIAOSHA_QUEUE)
    public void receive(String msg){
        logger.info("receive message:"+msg);
        MiaoshaMessage miaoshaMessage = JedisAdapter.stringToBean(msg,MiaoshaMessage.class);
        long id = miaoshaMessage.getGoodId();
        MiaoshaUser miaoshaUser = miaoshaMessage.getMiaoshaUser();

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(id);
        int stock = goodsVo.getStockCount();
        if(stock<=0){
            return;
        }
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(),id);
        if(miaoshaOrder!=null){
            return;
        }
        OrderInfo orderInfo = miaoshaService.miaosha(miaoshaUser,goodsVo);
    }

//    @RabbitListener(queues = MQconfig.QUEUE)
//    public void receive(String msg){
//        logger.info("receive message:"+msg);
//    }
}
