package com.roy.util;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtil {
    private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");
    public static boolean isMobile(String num){
        String mobile = num.trim();
        if(StringUtils.isEmpty(mobile)){
            return false;
        }
        Matcher matcher = mobile_pattern.matcher(mobile);
        return matcher.matches();
    }
    public static void main(String[] args){
        System.out.println(isMobile("12131231"));
        System.out.println(isMobile("18780009301"));
        System.out.println(isMobile(" 18780009301 "));
    }
}
