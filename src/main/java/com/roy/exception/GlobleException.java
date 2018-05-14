package com.roy.exception;

import com.roy.result.CodeMsg;

public class GlobleException extends RuntimeException {
    private static final long serialVersionUID = 1l;
    private CodeMsg cm;
    public GlobleException(CodeMsg cm){
        super(cm.toString());
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }
}
