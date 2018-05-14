package com.roy.controller;

import com.roy.domain.MiaoshaUser;
import com.roy.redis.GoodsKey;
import com.roy.result.Result;
import com.roy.service.GoodsService;
import com.roy.service.MiaoshaUserService;
import com.roy.util.JedisAdapter;
import com.roy.vo.GoodsDetailVo;
import com.roy.vo.GoodsVo;
import com.roy.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;


@Controller
@RequestMapping("/goods")
public class GoodsControleer {
    private static Logger logger = LoggerFactory.getLogger(GoodsControleer.class);

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    GoodsService goodsService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String list(HttpServletResponse httpServletResponse,
                       HttpServletRequest httpServletRequest,
                       Model model, MiaoshaUser miaoshaUser){
        model.addAttribute("user",miaoshaUser);
        List<GoodsVo> goodslist = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodslist);
        String html = jedisAdapter.get(GoodsKey.getGoodsList,"",String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        SpringWebContext ctx = new SpringWebContext(httpServletRequest,httpServletResponse,httpServletRequest.getServletContext(),
                httpServletRequest.getLocale(),model.asMap(),applicationContext);
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
        if(!StringUtils.isEmpty(html)) {
            jedisAdapter.set(GoodsKey.getGoodsList,"",html);
            return  html;
        }
        return null;
    }

    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletResponse httpServletResponse,
                                        HttpServletRequest httpServletRequest, Model model, MiaoshaUser miaoshaUser, @PathVariable("goodsId") long goodsId){
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goodsVo.getStartDate().getTime();
        long endAt = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSec = 0;
        if(startAt>now){
            miaoshaStatus = 0;
            remainSec = (int)((startAt-now)/1000);
        }else if(now > endAt) {
            miaoshaStatus = 2;
            remainSec = -1;
        }else {
            miaoshaStatus = 1;
            remainSec = 0;
        }
        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoodsVo(goodsVo);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        goodsDetailVo.setRemainSec(remainSec);
        goodsDetailVo.setMiaoshaUser(miaoshaUser);
        return Result.success(goodsDetailVo);
    }

    @RequestMapping(value = "/to_detail2/{goodsId}",produces="text/html")
    @ResponseBody
    public String detail2(HttpServletResponse httpServletResponse,
                         HttpServletRequest httpServletRequest,Model model, MiaoshaUser miaoshaUser, @PathVariable("goodsId") long goodsId){
        model.addAttribute("user",miaoshaUser);

        String html = jedisAdapter.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goodsVo);
        long startAt = goodsVo.getStartDate().getTime();
        long endAt = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSec = 0;
        if(startAt>now){
            miaoshaStatus = 0;
            remainSec = (int)((startAt-now)/1000);
        }else if(now > endAt) {
            miaoshaStatus = 2;
            remainSec = -1;
        }else {
            miaoshaStatus = 1;
            remainSec = 0;
        }
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSec);
        SpringWebContext ctx = new SpringWebContext(httpServletRequest,httpServletResponse,httpServletRequest.getServletContext(),
                httpServletRequest.getLocale(),model.asMap(),applicationContext);
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",ctx);
        if(!StringUtils.isEmpty(html)) {
            jedisAdapter.set(GoodsKey.getGoodsDetail,""+goodsId,html);
            return  html;
        }
        return null;
    }
}
