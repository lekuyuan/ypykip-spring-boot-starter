package com.ypy.flexiplug.plugin.command.remote;

public abstract class ConnectionWrapper<T> {

    public ConnectionWrapper(T connection) {
        this.connection = connection;
    }

    public ConnectionWrapper(ConnectInfo connectInfo, String password){
        this.connection = genericConnection(connectInfo, password);
    }

    /**
     * 连接
     */
    protected T connection;

    public void connect(){
        this.doConnect(1500);
    }

    public void connect(int overTime){
        this.doConnect(overTime);
    }

    public void disconnect(){
        this.doDisconnect();
    }

    /**
     * 连接方法
     */
    protected abstract void doConnect(int overTime);

    /**
     * 断开连接方法
     */
    protected abstract void doDisconnect();

    /**
     * 生成Connection
     * @param connectInfo
     * @return
     */
    protected abstract T genericConnection(ConnectInfo connectInfo, String password);

    public  void initialization(){

    }

    protected abstract boolean isExpire();

}
