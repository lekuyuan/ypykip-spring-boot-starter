package com.ypy.flexiplug.plugin.command.remote.jsch;

import com.ypy.flexiplug.plugin.command.remote.ConnectInfo;
import com.ypy.flexiplug.plugin.command.remote.jsch.channel.SftpChannelHolder;
import com.ypy.flexiplug.plugin.command.remote.SftpConnection;
import cn.hutool.extra.ssh.ChannelType;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class JschSftpConnectionWrapper<T extends Session, C extends ChannelSftp, CH extends SftpChannelHolder> extends AbstractJschConnectionWrapper<T, C, CH> implements SftpConnection {


    public JschSftpConnectionWrapper(T connection) {
        super(connection);
        channelHolder = (CH) new SftpChannelHolder();
    }

    public JschSftpConnectionWrapper(ConnectInfo connectInfo, String password) {
        super(connectInfo, password);
        channelHolder = (CH) new SftpChannelHolder();
    }

    @Override
    public void upload(InputStream inputStream, String remoteDirPath) {
        this.upload(inputStream, remoteDirPath, 0);
    }

    @Override
    public void upload(InputStream inputStream, String remoteDirPath, int mode) {
        this.renewal();
        ChannelSftp channel = null;
        try {
            channel = super.channel(ChannelType.SFTP);
            if(!channel.isConnected()){
                channel.connect();
            }
            channel.put(inputStream, remoteDirPath, mode);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } finally {
            if (null != channel && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }
    @Override
    public void download(String directory, OutputStream os) {
        this.renewal();
        ChannelSftp channel = null;
        try {
            channel = super.channel(ChannelType.SFTP);
            if (!channel.isConnected()) {
                channel.connect();
            }
            channel.get(directory, os);
        } catch (Exception e) {
            log.error("下载文件异常 {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (null != channel && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JschSftpConnectionWrapper) {
            return (this.userName.equals(((JschSftpConnectionWrapper) obj).userName)
                    && this.port == ((JschSftpConnectionWrapper) obj).port
                    && this.ip.equals(((JschSftpConnectionWrapper) obj).ip))
                    && this.password.equals(((JschSftpConnectionWrapper)obj).password);
        }
        return false;
    }
}
