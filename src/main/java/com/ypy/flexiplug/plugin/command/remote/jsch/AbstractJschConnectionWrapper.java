package com.ypy.flexiplug.plugin.command.remote.jsch;


import com.ypy.flexiplug.plugin.command.remote.ConnectInfo;
import com.ypy.flexiplug.plugin.command.remote.ConnectionWrapper;
import com.ypy.flexiplug.plugin.command.remote.jsch.channel.ChannelHolder;
import cn.hutool.extra.ssh.ChannelType;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJschConnectionWrapper<T extends Session, C extends Channel, CH extends ChannelHolder> extends ConnectionWrapper<T> {

    protected String userName;

    protected String ip;


    protected int port;

    protected String password;

    protected CH channelHolder;

    private final static int MAX_RETRY_COUNT = 25;

    private long ttl;

    private long time;

    private final static long HALF_HOURS = 30 * 60 * 1000L;

    public AbstractJschConnectionWrapper(T connection) {
        super(connection);
    }

    public AbstractJschConnectionWrapper(ConnectInfo connectInfo, String password) {
        super(connectInfo, password);
    }

    @Override
    protected void doConnect(int overTime) {
        try {
            connection.connect(overTime);
            this.time = System.currentTimeMillis();
            ttl = HALF_HOURS;
        } catch (JSchException e) {
            log.error("Jsch 创建连接失败！");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doDisconnect() {
        if(null != channelHolder){
            synchronized (channelHolder){
                if(null != channelHolder){
                    if(null != channelHolder.getChannel() && channelHolder.getChannel().isConnected()) {
                        channelHolder.getChannel().disconnect();
                    }
                    channelHolder = null;
                }
            }
        }
        connection.disconnect();
    }

    @Override
    protected T genericConnection(ConnectInfo connectInfo, String password) {
        JSch jSch = new JSch();
        try {
            this.userName = connectInfo.getUsername();
            this.ip = connectInfo.getIp();
            this.port = connectInfo.getPort();
            this.password = connectInfo.getPassword();
            connection = (T) jSch.getSession(userName, ip, port);
            connection.setPassword(password);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            connection.setConfig(config);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    protected C channel(ChannelType channelType) {
        C channel = null;
        int retry = 0;
        try {
            synchronized (channelHolder){
                while(null != channelHolder.getChannel() && channelHolder.getChannel().isConnected()){
                    if(retry > MAX_RETRY_COUNT){
                        log.error("获取连接重试次数过多！");
                        return null;
                    }
                    Thread.sleep(500);
                    retry ++;
                }
                channel = (C) connection.openChannel(channelType.getValue());
                channelHolder.setChannel(channel);
            }
            channel = (C) channelHolder.getChannel();
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return channel;
    }

    protected void renewal(){
       time = System.currentTimeMillis();
    }

    protected boolean isExpire(){
        return System.currentTimeMillis() - (time + ttl) >= 0;
    }
}
