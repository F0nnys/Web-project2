package com.roy.redis;

public class MiaoshaKey extends BasePrefix {

    public MiaoshaKey(int time,String prefix) {
        super(time,prefix);
    }

    public static MiaoshaKey isGoodsOver = new MiaoshaKey(0,"go");
    public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60,"mp");
    public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(60,"mvc");
}
