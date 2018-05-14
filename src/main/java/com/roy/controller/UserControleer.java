package com.roy.controller;

import com.roy.domain.MiaoshaUser;
import com.roy.result.Result;
import com.roy.service.GoodsService;
import com.roy.service.MiaoshaUserService;
import com.roy.util.JedisAdapter;
import com.roy.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
@RequestMapping("/user")
public class UserControleer {
    @Autowired
    MiaoshaUserService miaoshaUserService;

    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(MiaoshaUser miaoshaUser){
        return Result.success(miaoshaUser);
    }
}
