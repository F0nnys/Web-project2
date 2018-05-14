package com.roy.controller;

import com.roy.access.AccessLimit;
import com.roy.domain.MiaoshaOrder;
import com.roy.domain.MiaoshaUser;
import com.roy.domain.OrderInfo;
import com.roy.rabbitmq.MQSender;
import com.roy.rabbitmq.MiaoshaMessage;
import com.roy.redis.AccessKey;
import com.roy.redis.GoodsKey;
import com.roy.redis.MiaoshaKey;
import com.roy.result.CodeMsg;
import com.roy.result.Result;
import com.roy.service.GoodsService;
import com.roy.service.MiaoshaService;
import com.roy.service.MiaoshaUserService;
import com.roy.service.OrderService;
import com.roy.util.JedisAdapter;
import com.roy.util.MD5Util;
import com.roy.util.UUIDUtil;
import com.roy.vo.GoodsVo;
import com.sun.tools.javac.jvm.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean{
    private static Logger logger = LoggerFactory.getLogger(MiaoshaController.class);

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    MQSender mqSender;

    private static Map<Long,Boolean> localMap = new HashMap<Long, Boolean>();

    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodlist = goodsService.listGoodsVo();
        if(goodlist == null){
            return;
        }
        for(GoodsVo goodsVo:goodlist){
            jedisAdapter.set(GoodsKey.getMiaoshaGoodsStock,""+goodsVo.getId(),goodsVo.getStockCount());
            localMap.put(goodsVo.getId(),false);
        }
    }

    @RequestMapping(value = "/{path}/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser miaoshaUser,
                                   @RequestParam("goodsId")long goodsId,
                                   @PathVariable("path")String path){
        if(miaoshaUser==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user",miaoshaUser);

        boolean check = miaoshaService.checkPath(miaoshaUser,goodsId,path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        boolean flag = localMap.get(goodsId);
        if(flag){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        long stock = jedisAdapter.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);
        if(stock<0){
            localMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断是否秒杀
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(),goodsId);
        if(miaoshaOrder!=null){
            return Result.error(CodeMsg.REPEAT_MIAOSHA);
        }
        //减库存 下订单 写入秒杀订单
        MiaoshaMessage miaoshaMessage = new MiaoshaMessage();
        miaoshaMessage.setGoodId(goodsId);
        miaoshaMessage.setMiaoshaUser(miaoshaUser);
        mqSender.sendMiaoshaMessage(miaoshaMessage);
        return Result.success(0);
    }

    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser miaoshaUser, @RequestParam("goodsId")long goodsId) {
        if (miaoshaUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", miaoshaUser);
        long result = miaoshaService.getMiaoshaResult(miaoshaUser.getId(), goodsId);
        return Result.success(result);
    }

//    @RequestMapping("/do_miaosha")
//    public String doMiaosha(Model model, MiaoshaUser miaoshaUser, @RequestParam("goodsId")long goodsId){
//        if(miaoshaUser==null){
//            return "login";
//        }
//        model.addAttribute("user",miaoshaUser);
//        //判断库存
//        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
//        int stock = goodsVo.getStockCount();
//        if(stock<=0){
//            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER);
//            return "miaosha_fail";
//        }
//        //判断是否秒杀
//        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(),goodsId);
//        if(miaoshaOrder!=null){
//            model.addAttribute("errmsg",CodeMsg.REPEAT_MIAOSHA);
//            return "miaosha_fail";
//        }
//        //减库存 下订单 写入秒杀订单
//        OrderInfo orderInfo = miaoshaService.miaosha(miaoshaUser,goodsVo);
//        model.addAttribute("orderInfo",orderInfo);
//        model.addAttribute("goods",goodsVo);
//        return "order_detail";
//    }

    @AccessLimit(seconds=5,maxCount=5,needLogin=true)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest httpServletRequest,Model model, MiaoshaUser miaoshaUser,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value = "verifyCode",defaultValue = "0")int verifyCode) {
        if (miaoshaUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", miaoshaUser);
        boolean check = miaoshaService.checkVerifyCode(miaoshaUser,goodsId,verifyCode);
        if(!check){
            return Result.error(CodeMsg.YANZHENGMA_WRONG);
        }
        String path = miaoshaService.createMiaoshaPath(miaoshaUser,goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaverifyCode(HttpServletResponse response, MiaoshaUser miaoshaUser, @RequestParam("goodsId")long goodsId) {
        if (miaoshaUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image = miaoshaService.createVerifyCode(miaoshaUser, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
