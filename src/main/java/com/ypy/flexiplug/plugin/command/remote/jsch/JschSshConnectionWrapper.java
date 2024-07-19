package com.ypy.flexiplug.plugin.command.remote.jsch;


import cn.hutool.extra.ssh.ChannelType;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ypy.flexiplug.plugin.command.remote.*;
import com.ypy.flexiplug.plugin.command.remote.jsch.channel.ExecuteChannelHolder;
import com.ypy.flexiplug.plugin.command.remote.jsch.model.SourceInfo;
import com.ypy.flexiplug.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JschSshConnectionWrapper<T extends Session, C extends ChannelExec, CH extends ExecuteChannelHolder> extends AbstractJschConnectionWrapper<T, C, CH> implements SshConnection {

    private Map<String, SourceInfo> sourceCache = new HashMap<>();


    public JschSshConnectionWrapper(T connection) {
        super(connection);
        channelHolder = (CH) new ExecuteChannelHolder();
    }

    public JschSshConnectionWrapper(ConnectInfo connectInfo, String password) {
        super(connectInfo, password);
        channelHolder = (CH) new ExecuteChannelHolder();
    }



    @Override
    public ResultSet execute(String command) {
        this.renewal();
        ChannelExec channel = null;
        boolean source_flag = channelHolder.getSource_flag();
        String source = channelHolder.getSourceContent();
        ResultSet resultSet = null;
        channelHolder.setSource_flag(false);
        channelHolder.setSourceContent("");
        try {
            channel = super.channel(ChannelType.EXEC);
            channel.setPtyType("dump");
            channel.setPty(!command.contains("nohup"));
            if (StringUtils.isNotEmpty(channelHolder.getPath())) {
                command = "cd " + channelHolder.getPath() + " && " + command;
            }
            if (source_flag) {
                command = source + " && " + command;
            }
            channel.setCommand(command);
            log.info("execute command: [ {} ]", command);
            channel.connect(1500);
            resultSet = toResultSet(channel);
            if (resultSet.getExitCode() != 0) {
                log.error("execute command fail, command: [ {} ], result: [ {} ]", command, resultSet);
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }finally {
            if(null != channel && channel.isConnected()) {
                channel.disconnect();
            }
        }
        return resultSet;
    }

    @Override
    public void cd(String path) {
        this.cd(path, false);
    }

    @Override
    public void cd(String path, boolean absolute) {
        this.renewal();
        synchronized (channelHolder) {
            channelHolder.setPath(path, absolute);
        }
    }

    @Override
    public JschSshConnectionWrapper source() {
        this.renewal();
        synchronized (channelHolder){
            channelHolder.source();
        }
        return this;
    }


    @Override
    public JschSshConnectionWrapper source(String instanceName) {
        this.renewal();
        synchronized (channelHolder){
            setSourceContent(instanceName);
        }
        return this;
    }
    private ResultSet toResultSet(ChannelExec channel) {
        ResultSet resultSet = new ResultSet();
        StringBuilder resultStrBuilder = new StringBuilder();
        try (InputStream in = channel.getInputStream(); OutputStream out = channel.getOutputStream()) {
            byte[] tmp = new byte[1024];
            while (true) {
                while (true) {
                    int exitStatus;
                    if (in.available() > 0) {
                        exitStatus = in.read(tmp, 0, 1024);
                        if (exitStatus >= 0) {
                            String msg = new String(tmp, 0, exitStatus, StandardCharsets.UTF_8);
                            resultStrBuilder.append(msg);
                            continue;
                        }
                    }

                    if (channel.isClosed()) {
                        if (in.available() <= 0) {
                            in.close();
                            out.close();
                            exitStatus = channel.getExitStatus();
                            resultSet.setExitCode(exitStatus);
                            channel.disconnect();
                            resultSet.setResults(resultStrBuilder.toString().trim());
                            return resultSet;
                        }
                    } else {
                        Thread.sleep(2000L);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    public String getUserName() {
        return userName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void initialization() {
        synchronized (channelHolder){
            renewal();
            channelHolder.init();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JschSshConnectionWrapper) {
            return (this.userName.equals(((JschSshConnectionWrapper) obj).getUserName())
                    && this.port == ((JschSshConnectionWrapper) obj).port
                    && this.ip.equals(((JschSshConnectionWrapper) obj).getIp()))
                    && this.password.equals(((JschSshConnectionWrapper)obj).password);
        }
        return false;
    }


    private void setSourceContent (String instanceName) {

        SourceInfo sourceInfo = sourceCache.get(instanceName);
        String source ="source " + ExecuteChannelHolder.SOURCE_PATH;
        ByteArrayOutputStream os = null;
        InputStream is = null;
        if (null == sourceInfo || sourceInfo.run()) {
            try {
                if (sourceInfo == null) {
                    sourceInfo = new SourceInfo();
                }
                ConnectInfo connectInfo = new ConnectInfo();
                connectInfo.setPassword(this.password);
                connectInfo.setPort(this.port);
                connectInfo.setUsername(this.userName);
                connectInfo.setIp(this.ip);
                String instanceNameCommand = SourceContentUtils.findInstallDirOfInstance(instanceName);
                ResultSet resultSet = this.execute(instanceNameCommand);
                log.info("获取安装路径结果: [ {} ]", resultSet);
                if (resultSet.getExitCode() == 0) {
                    String installDir = resultSet.getResults().get(0);
                    ConnectionManager connectionManager = SpringApplicationUtils.getApplicationContext().getBean(ConnectionManager.class);
                    int connectId = connectionManager.sftp(connectInfo);
                    SftpConnection sftpConnection = (SftpConnection) connectionManager.get(connectId);
                    os = new ByteArrayOutputStream();
                    sftpConnection.download(installDir + "/etc/.conf." + instanceName, os);
                    is = new ByteArrayInputStream(os.toByteArray());
                    List<String> profileInfo = SourceContentUtils.getProfileInfo(is, installDir);
                    source = SourceContentUtils.getSourceContent(instanceName, installDir, profileInfo.get(1));
                    sourceInfo.setRetryCount(0);
                    sourceInfo.setContent(source);
                } else {
                    log.info("获取安装路径异常");
                    sourceInfo.setRetryCount(sourceInfo.getRetryCount() + 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("获取config配置文件异常: [ {} ]", e.getMessage(), e);
                sourceInfo.setRetryCount(sourceInfo.getRetryCount() + 1);
            } finally {
                closeOs(os);
                closeIs(is);
            }
        }
        if (StringUtils.isNotBlank(sourceInfo.getContent())) {
            source = sourceInfo.getContent();
        }
        channelHolder.source(source);
        sourceCache.put(instanceName, sourceInfo);
    }

    private void closeOs (OutputStream os) {

        if (null != os) {
            try {
                os.close();
            } catch (IOException e) {
                log.error("close OutputStream error: [ {} ]", e.getMessage(), e);
            }
        }
    }

    private void closeIs (InputStream is) {
        if (null != is) {
            try {
                is.close();
            } catch (IOException e) {
                log.error("close InputStream error: [ {} ]", e.getMessage(), e);
            }
        }
    }

}
