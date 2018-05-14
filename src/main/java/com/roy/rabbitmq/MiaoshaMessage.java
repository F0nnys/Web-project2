package com.roy.rabbitmq;

import com.roy.domain.MiaoshaUser;

public class MiaoshaMessage {
    private MiaoshaUser miaoshaUser;
    private long goodId;

    public MiaoshaUser getMiaoshaUser() {
        return miaoshaUser;
    }

    public void setMiaoshaUser(MiaoshaUser miaoshaUser) {
        this.miaoshaUser = miaoshaUser;
    }

    public long getGoodId() {
        return goodId;
    }

    public void setGoodId(long goodId) {
        this.goodId = goodId;
    }
}
