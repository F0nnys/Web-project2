package com.roy.service;

import com.roy.dao.GoodsDao;
import com.roy.domain.Goods;
import com.roy.domain.MiaoshaGoods;
import com.roy.vo.GoodsVo;
import org.hibernate.validator.constraints.EAN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class GoodsService {
    @Autowired
    GoodsDao goodsDao;

    private GoodsVo goodsVoByGoodsId;

    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long id) {
        return goodsDao.goodsVoByGoodsId(id);
    }

    public int reduceStock(GoodsVo goodsVo){
        MiaoshaGoods goods = new MiaoshaGoods();
        goods.setGoodsId(goodsVo.getId());
        int res = goodsDao.reduceStock(goods);
        return res;
    }
}
