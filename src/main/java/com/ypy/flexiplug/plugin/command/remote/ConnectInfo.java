package com.ypy.flexiplug.plugin.command.remote;

import lombok.Data;

@Data
public class ConnectInfo {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * ip
     */
    private String ip;

    /**
     * 端口号
     */
    private int port;
}
