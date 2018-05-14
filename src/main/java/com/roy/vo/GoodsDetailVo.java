package com.roy.vo;

import com.roy.domain.MiaoshaUser;

public class GoodsDetailVo {
    private int miaoshaStatus = 0;
    private int remainSec = 0;
    private GoodsVo goodsVo;
    private MiaoshaUser miaoshaUser;

    public MiaoshaUser getMiaoshaUser() {
        return miaoshaUser;
    }

    public void setMiaoshaUser(MiaoshaUser miaoshaUser) {
        this.miaoshaUser = miaoshaUser;
    }

    public int getMiaoshaStatus() {
        return miaoshaStatus;
    }

    public void setMiaoshaStatus(int miaoshaStatus) {
        this.miaoshaStatus = miaoshaStatus;
    }

    public int getRemainSec() {
        return remainSec;
    }

    public void setRemainSec(int remainSec) {
        this.remainSec = remainSec;
    }

    public GoodsVo getGoodsVo() {
        return goodsVo;
    }

    public void setGoodsVo(GoodsVo goodsVo) {
        this.goodsVo = goodsVo;
    }
}
