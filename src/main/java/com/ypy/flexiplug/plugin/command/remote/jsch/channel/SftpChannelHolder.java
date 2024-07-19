package com.ypy.flexiplug.plugin.command.remote.jsch.channel;

import com.jcraft.jsch.ChannelSftp;

public class SftpChannelHolder implements ChannelHolder<ChannelSftp>{

    private ChannelSftp channelSftp;

    @Override
    public ChannelSftp getChannel() {
        return channelSftp;
    }

    @Override
    public void setChannel(ChannelSftp channel) {
        this.channelSftp = channel;
    }
}
