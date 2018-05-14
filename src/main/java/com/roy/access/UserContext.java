package com.roy.access;

import com.roy.domain.MiaoshaUser;

public class UserContext {
    private static ThreadLocal<MiaoshaUser> userHodler = new ThreadLocal<MiaoshaUser>();
    public static void setUser(MiaoshaUser miaoshaUser){
        userHodler.set(miaoshaUser);
    }
    public static MiaoshaUser getUser(){
        return userHodler.get();
    }
}
