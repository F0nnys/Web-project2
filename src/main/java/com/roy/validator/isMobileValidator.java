package com.roy.validator;

import com.alibaba.druid.util.StringUtils;
import com.roy.util.ValidatorUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class isMobileValidator implements ConstraintValidator<isMobile, String> {

    private boolean required = false;
    @Override
    public void initialize(isMobile isMobile) {
        required = isMobile.required();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(required) {
            return ValidatorUtil.isMobile(s);
        }else {
            if(StringUtils.isEmpty(s)) {
                return true;
            }else {
                return ValidatorUtil.isMobile(s);
            }
        }
    }
}
