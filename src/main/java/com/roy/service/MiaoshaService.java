package com.roy.service;

import com.roy.dao.GoodsDao;
import com.roy.domain.Goods;
import com.roy.domain.MiaoshaOrder;
import com.roy.domain.MiaoshaUser;
import com.roy.domain.OrderInfo;
import com.roy.redis.MiaoshaKey;
import com.roy.util.JedisAdapter;
import com.roy.util.MD5Util;
import com.roy.util.UUIDUtil;
import com.roy.vo.GoodsVo;
import groovy.transform.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Service
public class MiaoshaService {
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Transactional
    public  OrderInfo miaosha(MiaoshaUser miaoshaUser, GoodsVo goodsVo) {
            int res = goodsService.reduceStock(goodsVo);
            if(res == 1){
                return orderService.createOrder(miaoshaUser, goodsVo);
            }
            setGoodsOver(goodsVo.getId());
            return null;
    }

    public long getMiaoshaResult(Long id, long goodsId) {
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(id,goodsId);
        if(miaoshaOrder != null){
            return miaoshaOrder.getOrderId();
        }
        boolean flag = getGoodsOver(goodsId);
        if(flag){
            return -1;
        }
        return 0;
    }

    private boolean getGoodsOver(long goodsId) {
        return jedisAdapter.exists(MiaoshaKey.isGoodsOver,""+goodsId);
    }

    public void setGoodsOver(Long goodsOver) {
        jedisAdapter.set(MiaoshaKey.isGoodsOver,""+goodsOver,true);
    }

    public String createMiaoshaPath(MiaoshaUser miaoshaUser,long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        jedisAdapter.set(MiaoshaKey.getMiaoshaPath, "" + miaoshaUser.getId() + "_" + goodsId, str);
        return str;
    }

    public boolean checkPath(MiaoshaUser miaoshaUser, long goodsId, String path) {
        if(miaoshaUser==null || path == null){
            return false;
        }
        String pathOld = jedisAdapter.get(MiaoshaKey.getMiaoshaPath, "" + miaoshaUser.getId() + "_" + goodsId,String.class);
        return path.equals(pathOld);
    }

    public BufferedImage createVerifyCode(MiaoshaUser miaoshaUser, long goodsId) {
        if(miaoshaUser==null){
            return null;
        }
        int width = 80;
        int height = 32;
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        graphics.setColor(new Color(0xDCDCDC));
        graphics.fillRect(0,0,width,height);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(0,0,width-1,height-1);
        Random random = new Random();
        for(int i=0;i<50;i++){
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            graphics.drawOval(x,y,0,0);
        }
        String verifyCode = generateVerifyCode(random);
        graphics.setColor(new Color(0, 100, 0));
        graphics.setFont(new Font("Candara", Font.BOLD, 24));
        int x=(int)(width/2-graphics.getFontMetrics().stringWidth(verifyCode)/2);
        int y=height/2+graphics.getFontMetrics().getHeight()/3;
        graphics.drawString(verifyCode, x, y);
        graphics.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        jedisAdapter.set(MiaoshaKey.getMiaoshaVerifyCode, miaoshaUser.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <=0) {
            return false;
        }
        Integer codeOld = jedisAdapter.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, Integer.class);
        if(codeOld == null || codeOld - verifyCode != 0 ) {
            return false;
        }
        jedisAdapter.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
        return true;
    }

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static char[] ops = new char[] {'+', '-', '*'};
    /**
     * + - *
     * */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }
}
