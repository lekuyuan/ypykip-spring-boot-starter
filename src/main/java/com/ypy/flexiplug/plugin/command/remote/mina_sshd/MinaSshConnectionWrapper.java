package com.ypy.flexiplug.plugin.command.remote.mina_sshd;

import com.ypy.flexiplug.plugin.command.remote.jsch.model.SourceInfo;
import com.ypy.flexiplug.plugin.command.remote.mina_sshd.channel.ExecuteChannelHolder;
import com.ypy.flexiplug.utils.StringUtils;
import com.ypy.flexiplug.plugin.command.remote.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MinaSshConnectionWrapper<T extends ClientSession> extends AbstractMinaSshdConnectionWrapper<T> implements SshConnection {

    private Map<String, SourceInfo> sourceCache = new HashMap<>();

    private ExecuteChannelHolder channelHolder;

    public MinaSshConnectionWrapper(ClientSession connection) {
        super(connection);
        channelHolder = new ExecuteChannelHolder();
    }

    public MinaSshConnectionWrapper(ConnectInfo connectInfo, String password) {
        super(connectInfo, password);
        channelHolder = new ExecuteChannelHolder();
    }

    @Override
    public ResultSet execute(String command) {
        this.renewal();
        boolean source_flag = channelHolder.getSource_flag();
        String source = channelHolder.getSourceContent();
        channelHolder.setSource_flag(false);
        channelHolder.setSourceContent("");
        if (StringUtils.isNotEmpty(channelHolder.getPath())) {
            command = "cd " + channelHolder.getPath() + " && " + command;
        }
        if (source_flag) {
            command = source + " && " + command;
        }
        log.info("execute command: [ {} ]", command);
        ResultSet resultSet = doExecute(command);
        if (resultSet.getExitCode() != 0) {
            log.error("execute command fail, command: [ {} ], result: [ {} ]", command, resultSet);
        }
        return resultSet;
    }


    private ResultSet doExecute(String command) {
        log.info("准备执行命令：【{}】...", command);
        try (ChannelExec channelExec = super.connection.createExecChannel(command);
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             ByteArrayOutputStream err = new ByteArrayOutputStream()) {
            channelExec.setOut(out);
            channelExec.setErr(err);
            channelExec.open();
            channelExec.waitFor(Collections.singleton(ClientChannelEvent.CLOSED), 0);
            int exitStatus = channelExec.getExitStatus();
            log.info("执行命令【{}】结束，状态码为【{}】", command, exitStatus);
            channelExec.close(true);
            log.info("准备处理命令【{}】的返回结果...", command);
            return this.toResultSet(out, err, exitStatus);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultSet toResultSet(ByteArrayOutputStream out, ByteArrayOutputStream err, int exitStatus) {
        ResultSet resultSet = new ResultSet();
        resultSet.setExitCode(exitStatus);
        byte[] outData = out.toByteArray();
        byte[] errData = err.toByteArray();
        /*if(exitStatus >= 0){
            resultSet.setResults(new String(outData).trim());
        }else{
            resultSet.setResults(new String(errData).trim());
        }*/
        String outStr = new String(outData);
        String errStr = new String(errData);
        resultSet.setResults((errStr+ outStr).trim());
        log.info("处理结果为【{}】", null == resultSet.getResults() ? "null" : resultSet.getResults().stream().collect(Collectors.joining(",")));
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
    public MinaSshConnectionWrapper<T> source() {
        this.renewal();
        synchronized (channelHolder) {
            channelHolder.source();
        }
        return this;
    }


    @Override
    public MinaSshConnectionWrapper source(String instanceName) {
        this.renewal();
        synchronized (channelHolder) {
            setSourceContent(instanceName);
        }
        return this;
    }

    @Override
    public void initialization() {
        synchronized (channelHolder) {
            renewal();
            channelHolder.init();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MinaSshConnectionWrapper) {
            return (this.userName.equals(((MinaSshConnectionWrapper) obj).userName)
                    && this.port == ((MinaSshConnectionWrapper) obj).port
                    && this.ip.equals(((MinaSshConnectionWrapper) obj).ip))
                    && this.password.equals(((MinaSshConnectionWrapper) obj).password);
        }
        return false;
    }

    private void setSourceContent(String instanceName) {
        SourceInfo sourceInfo = sourceCache.get(instanceName);
        String source = "source " + ExecuteChannelHolder.SOURCE_PATH;
        if (null == sourceInfo || sourceInfo.run()) {
            InputStream is = null;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
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
            }finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(sourceInfo.getContent())) {
            source = sourceInfo.getContent();
        }
        channelHolder.source(source);
        sourceCache.put(instanceName, sourceInfo);
    }
}
