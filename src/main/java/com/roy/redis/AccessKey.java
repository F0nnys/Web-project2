package com.roy.redis;

public class AccessKey extends BasePrefix {

    public AccessKey(int time, String prefix) {
        super(time,prefix);
    }

    public static AccessKey accessCount = new AccessKey(5,"access");
    public static AccessKey setExpire(int sec){
        return new AccessKey(sec,"access");
    }
}
