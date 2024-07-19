package com.ypy.flexiplug.plugin.command.remote.jsch.channel;

import com.jcraft.jsch.Channel;

public interface ChannelHolder<T extends Channel> {

    T getChannel();

    void setChannel(T channel);

}
