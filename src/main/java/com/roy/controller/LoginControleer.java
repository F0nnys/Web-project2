package com.roy.controller;

import com.roy.domain.User;
import com.roy.result.CodeMsg;
import com.roy.result.Result;
import com.roy.service.MiaoshaUserService;
import com.roy.service.UserService;
import com.roy.util.JedisAdapter;
import com.roy.util.ValidatorUtil;
import com.roy.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@Controller
@RequestMapping("/login")
public class LoginControleer {
    private static Logger logger = LoggerFactory.getLogger(LoginControleer.class);

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    JedisAdapter jedisAdapter;

    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(@Valid LoginVo loginVo, HttpServletResponse httpServletResponse){
        logger.info(loginVo.toString());
//        String pass = loginVo.getPassword();
//        String mobile = loginVo.getMobile();
//        if(StringUtils.isEmpty(pass)){
//            return Result.error(CodeMsg.PASSWORD_EMPTY);
//        }
//        if(StringUtils.isEmpty(mobile)){
//            return Result.error(CodeMsg.MOBILE_EMPTY);
//        }
//        if(!ValidatorUtil.isMobile(mobile)){
//            return Result.error(CodeMsg.MOBILE_ERROR);
//        }
        String token = miaoshaUserService.login(loginVo,httpServletResponse);
        return Result.success(token);
    }


}
