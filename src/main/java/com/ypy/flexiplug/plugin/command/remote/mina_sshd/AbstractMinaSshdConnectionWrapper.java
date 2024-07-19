package com.ypy.flexiplug.plugin.command.remote.mina_sshd;

import com.ypy.flexiplug.plugin.command.remote.ConnectInfo;
import com.ypy.flexiplug.plugin.command.remote.ConnectionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.ClientBuilder;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.kex.BuiltinDHFactories;
import org.apache.sshd.common.signature.BuiltinSignatures;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
public abstract class AbstractMinaSshdConnectionWrapper<T extends ClientSession> extends ConnectionWrapper<ClientSession> {

    protected String userName;

    protected String ip;

    protected int port;

    protected String password;

    private String realPassword;

//    private ClientSession clientSession;

    private SshClient sshClient;

    private long ttl;

    private long time;

    private final static int MAX_RETRY_COUNT = 25;

    public AbstractMinaSshdConnectionWrapper(ClientSession connection) {
        super(connection);
    }

    public AbstractMinaSshdConnectionWrapper(ConnectInfo connectInfo, String password) {
        super(connectInfo, password);
    }

    @Override
    protected void doConnect(int overTime) {
        boolean clientNeedClose = true;
        try {
            long currentTime = System.currentTimeMillis();
            sshClient = SshClient.setUpDefaultClient();
            log.info("setUpDefaultClient 执行时间：" +  (System.currentTimeMillis() - currentTime));

            currentTime = System.currentTimeMillis();
            sshClient.setKeyExchangeFactories(NamedFactory.setUpTransformedFactories(
                    false,
                    BuiltinDHFactories.VALUES,
                    ClientBuilder.DH2KEX
            ));
            log.info("setKeyExchangeFactories 执行时间：" +  (System.currentTimeMillis() - currentTime));
            sshClient.setSignatureFactories(new ArrayList<>(BuiltinSignatures.VALUES));

            currentTime = System.currentTimeMillis();
            sshClient.start();
            log.info("sshClient start 执行时间：" +  (System.currentTimeMillis() - currentTime));

            currentTime = System.currentTimeMillis();
            if (!sshClient.isStarted()) {
                log.error("SshClient 启动失败！");
                throw new RuntimeException("SshClient 启动失败");
            }
            log.info("sshClient isStart 执行时间：" +  (System.currentTimeMillis() - currentTime));

            currentTime = System.currentTimeMillis();
            connection = sshClient.connect(userName, ip, port).verify().getSession();
            log.info("sshClient connect 执行时间：" +  (System.currentTimeMillis() - currentTime));

            currentTime = System.currentTimeMillis();
            if (null != connection) {
                clientNeedClose = false;
            }
            log.info("sshClient isConnect 执行时间：" +  (System.currentTimeMillis() - currentTime));
            connection.addPasswordIdentity(realPassword);
            realPassword = "";
            if (!connection.auth().verify(15000L).isSuccess()) {
                throw new RuntimeException("鉴权失败！");
            }
            this.time = System.currentTimeMillis();
            ttl = 30 * 1000;
        } catch (IOException e) {
            log.error("初始化ssh连接失败！");
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("ssh连接失败！原因【{}】", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if (clientNeedClose) {
                this.doDisconnect();
            }
        }
    }

    @Override
    protected void doDisconnect() {
        try {
            if (null != connection && connection.isOpen()) {
                connection.close();
            }
            if (null != sshClient && sshClient.isOpen()) {
                sshClient.close();
            }
        } catch (IOException e) {
            log.error("连接关闭失败！");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ClientSession genericConnection(ConnectInfo connectInfo, String password) {
//        try {
            this.userName = connectInfo.getUsername();
            this.ip = connectInfo.getIp();
            this.port = connectInfo.getPort();
            this.realPassword = password;
            this.password = connectInfo.getPassword();
//            sshClient = SshClient.setUpDefaultClient();
//            sshClient.setKeyExchangeFactories(NamedFactory.setUpTransformedFactories(
//                    false,
//                    BuiltinDHFactories.VALUES,
//                    ClientBuilder.DH2KEX
//            ));
//            sshClient.setSignatureFactories(new ArrayList<>(BuiltinSignatures.VALUES));
//            sshClient.start();
//            if (!sshClient.isStarted()) {
//                log.error("SshClient 启动失败！");
//                return null;
//            }
//            clientSession = sshClient.connect(userName, ip, port).verify().getSession();
//            if (null != clientSession) {
//                clientNeedClose = false;
//            }
            return connection;
//        } catch (IOException e) {
//            log.error("初始化ssh连接失败！");
//            throw new RuntimeException(e);
//        } finally {
//            if (clientNeedClose) {
//                this.doDisconnect();
//            }
//        }
    }

//    protected Object channel(ChannelType channelType) {
//        Object channel = null;
//        int retry = 0;
//        try {
//            synchronized (channelHolder){
//                while(null != channelHolder.getChannel() && channelHolder.isConnected()){
//                    if(retry > MAX_RETRY_COUNT){
//                        log.error("获取连接重试次数过多！");
//                        return null;
//                    }
//                    Thread.sleep(500);
//                    retry ++;
//                }
//                channel = openChannel();
//                channelHolder.setChannel(channel);
//            }
//            channel = channelHolder.getChannel();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return channel;
//    }

    @Override
    protected boolean isExpire() {
        return System.currentTimeMillis() - (time + ttl) >= 0;
    }

    protected void renewal() {
        time = System.currentTimeMillis();
    }
}
