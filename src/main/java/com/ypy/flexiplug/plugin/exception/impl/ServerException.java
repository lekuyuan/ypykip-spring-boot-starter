package com.ypy.flexiplug.plugin.exception.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ServerException extends RuntimeException{
    // micro-server unique id
    protected String serverId;

    protected String message;

    public ServerException(String message) {
        super(message);
        this.message = message;
    }
}
