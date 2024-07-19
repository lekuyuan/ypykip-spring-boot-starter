package com.ypy.flexiplug.plugin.command.remote.mina_sshd.channel;

public interface ChannelHolder<T> {

    boolean isConnected();

    void disconnect();

    T getChannel();

    void setChannel(T channel);
}
