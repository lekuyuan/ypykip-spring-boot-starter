package com.ypy.flexiplug.plugin.exception.impl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BusinessException extends ServerException{

    private String busCode;


    public BusinessException() {
        super();
    }

    public BusinessException(String busCode, String message) {
        super(message);
        this.busCode = busCode;
        this.message = message;
    }

    public BusinessException(String busCode, String serverId, String message) {
        super(message);
        this.serverId = serverId;
        this.busCode = busCode;
        this.message = message;
    }

}
