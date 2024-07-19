package com.ypy.flexiplug.plugin.exception.impl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BusinessException extends RuntimeException{
    private String busCode;
    private String message;

    public BusinessException() {
        super();
    }


    public BusinessException(String busCode, String message) {
        super(message);
        this.busCode = busCode;
        this.message = message;
    }

}
