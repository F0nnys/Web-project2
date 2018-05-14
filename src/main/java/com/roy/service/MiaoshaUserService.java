package com.roy.service;

import com.roy.dao.MiaoshaUserDao;
import com.roy.domain.MiaoshaUser;
import com.roy.exception.GlobleException;
import com.roy.redis.MiaoshaUserKey;
import com.roy.result.CodeMsg;
import com.roy.util.JedisAdapter;
import com.roy.util.MD5Util;
import com.roy.util.UUIDUtil;
import com.roy.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    JedisAdapter jedisAdapter;

    public String login(LoginVo loginVo, HttpServletResponse httpServletResponse) {
        if(loginVo==null){
            throw new GlobleException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String pass = loginVo.getPassword();
        MiaoshaUser miaoshaUser = getById(Long.parseLong(mobile));
        if(miaoshaUser==null){
            throw new GlobleException(CodeMsg.MOBILE_NOT_EXIST);
        }
        String dbPass = miaoshaUser.getPassword();
        String dbsalt = miaoshaUser.getSalt();
        String calcPass = MD5Util.formPassToDBPass(pass,dbsalt);
        if(!calcPass.equals(dbPass)){
            throw new GlobleException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(token,miaoshaUser,httpServletResponse);
        return token;
    }

    public MiaoshaUser getById(long id){
        MiaoshaUser miaoshaUser = jedisAdapter.get(MiaoshaUserKey.getById,""+id,MiaoshaUser.class);
        if(miaoshaUser != null){
            return miaoshaUser;
        }
        miaoshaUser = miaoshaUserDao.getById(id);
        if(miaoshaUser != null){
            jedisAdapter.set(MiaoshaUserKey.getById,""+id,miaoshaUser);
        }
        return miaoshaUser;

    }
    public MiaoshaUser getByToken(String token,HttpServletResponse httpServletResponse) {
        if(StringUtils.isEmpty(token)){
            return null;
        }
        MiaoshaUser miaoshaUser = jedisAdapter.get(MiaoshaUserKey.token,token,MiaoshaUser.class);
        if(miaoshaUser!=null){
        addCookie(token,miaoshaUser,httpServletResponse);
        }
        return miaoshaUser;
    }
    private void addCookie(String token,MiaoshaUser miaoshaUser,HttpServletResponse httpServletResponse){
        jedisAdapter.set(MiaoshaUserKey.token,token,miaoshaUser);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
    }

    public boolean updatePassword(String token,long id,String passwordNew){
        MiaoshaUser miaoshaUser = getById(id);
        if(miaoshaUser == null){
            throw new GlobleException(CodeMsg.MOBILE_NOT_EXIST);
        }
        MiaoshaUser toBeUpdate = new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(passwordNew,miaoshaUser.getSalt()));
        miaoshaUserDao.update(toBeUpdate);
        jedisAdapter.delete(MiaoshaUserKey.getById,""+id);
        miaoshaUser.setPassword(toBeUpdate.getPassword());
        jedisAdapter.set(MiaoshaUserKey.token,token,miaoshaUser);
        return true;
    }
}
