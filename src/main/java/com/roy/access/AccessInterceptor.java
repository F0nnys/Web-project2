package com.roy.access;

import com.alibaba.fastjson.JSON;
import com.roy.domain.MiaoshaUser;
import com.roy.redis.AccessKey;
import com.roy.result.CodeMsg;
import com.roy.result.Result;
import com.roy.service.MiaoshaUserService;
import com.roy.util.JedisAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Component
public class AccessInterceptor implements HandlerInterceptor {

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse,
                             Object o) throws Exception {
        if(o instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod)o;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if(accessLimit == null){
                return true;
            }
            MiaoshaUser miaoshaUser = getUser(httpServletRequest,httpServletResponse);
            UserContext.setUser(miaoshaUser);

            int sec = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean flag = accessLimit.needLogin();
            String key = httpServletRequest.getRequestURI();
            if(flag){
                if(miaoshaUser==null){
                    render(httpServletResponse, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_"+miaoshaUser.getId();
            }
            AccessKey accessKey = AccessKey.setExpire(sec);
            Integer countdb = jedisAdapter.get(accessKey,key,Integer.class);
            if(countdb == null){
                jedisAdapter.set(accessKey,key,1);
            }else if(countdb<maxCount){
                jedisAdapter.incr(accessKey,key);
            }else {
                render(httpServletResponse,CodeMsg.ACCESS_LIMIT);
                return false;
            }
        }
        return true;
    }

    private void render(HttpServletResponse httpServletResponse, CodeMsg sessionError) throws Exception {
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        OutputStream outputStream = httpServletResponse.getOutputStream();
        String str = JSON.toJSONString(Result.error(sessionError));
        outputStream.write(str.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }

    private MiaoshaUser getUser(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse){
        String paramToken = httpServletRequest.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(httpServletRequest,MiaoshaUserService.COOKIE_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        MiaoshaUser miaoshaUser = miaoshaUserService.getByToken(token,httpServletResponse);
        return miaoshaUser;
    }

    private String getCookieValue(HttpServletRequest httpServletRequest, String cookieNameToken) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if(cookies==null || cookies.length<=0){
            return null;
        }
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(cookieNameToken)){
                return cookie.getValue();
            }
        }
        return null;
    }
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
