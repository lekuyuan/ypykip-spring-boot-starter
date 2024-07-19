package com.ypy.flexiplug.plugin.command.remote;

import com.ypy.flexiplug.plugin.command.remote.jsch.JschSftpConnectionWrapper;
import com.ypy.flexiplug.plugin.command.remote.jsch.JschSshConnectionWrapper;
import com.ypy.flexiplug.plugin.secret.SecretProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 连接管理器
 */
public class ConnectionManager {

    private volatile boolean checkUnusableConnectionFlag = true;

    private boolean checkUnusableConnectionRunning = false;

    private volatile boolean closeConnectionFlag = true;

    private boolean closeConnectionRunning = false;

    private final ConcurrentMap<String, LinkedList<ConnectionWrapper>> cache = new ConcurrentHashMap<>();

    private final LinkedBlockingDeque<Thread> threads = new LinkedBlockingDeque<>(200);

    private final LinkedBlockingDeque<ConnectionWrapper> readyCloseConnection = new LinkedBlockingDeque<>(400);

    @Autowired
    private SecretProcessor secretProcessor;

    public final int connect(ConnectInfo connectInfo) {
        return this.connect(connectInfo, JschSshConnectionWrapper.class);
    }

    public final int sftp(ConnectInfo connectInfo){
        return this.connect(connectInfo, JschSftpConnectionWrapper.class);
    }

    public final int connect(ConnectInfo connectInfo, Class wrapperClazz) {
        startTask();
        try {
            Constructor constructor = wrapperClazz.getConstructor(ConnectInfo.class, String.class);
            String password = secretProcessor.decrypt(connectInfo.getPassword());
            ConnectionWrapper connectionWrapper = (ConnectionWrapper) constructor.newInstance(connectInfo, password);
            Thread thread = Thread.currentThread();
            LinkedList<ConnectionWrapper> wrappers = cache.getOrDefault(thread.getId() + "", new LinkedList<>());
            if(wrappers.size() > 0){
                for(int i=0; i< wrappers.size(); i++){
                    if(wrappers.get(i).equals(connectionWrapper)){
                        wrappers.get(i).initialization();
                        return i;
                    }
                }
            }else {
                threads.addFirst(thread);
            }
            wrappers.add(connectionWrapper);
            connectionWrapper.connect();
            cache.put(thread.getId() + "", wrappers);
            return wrappers.size() - 1;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public final ConnectionWrapper get(int index) {
        Thread thread = Thread.currentThread();
        List<ConnectionWrapper> connectionWrappers = cache.get(thread.getId() + "");
        if (null == connectionWrappers || connectionWrappers.size() <= index) {
            return null;
        }
        return connectionWrappers.get(index);
    }

    private void checkUnusableConnection() {
        checkUnusableConnectionRunning = true;
        try {
            while (checkUnusableConnectionFlag) {
                Thread t;
                if (null != (t = threads.takeLast())) {
                    LinkedList<ConnectionWrapper> connectionWrappers = cache.get(t.getId() + "");
                    if(null != connectionWrappers && connectionWrappers.size() > 0){
                        boolean removeAll = !t.isAlive() || t.isInterrupted();
                        ListIterator<ConnectionWrapper> iterator = connectionWrappers.listIterator();
                        while (iterator.hasNext()){
                            ConnectionWrapper next = iterator.next();
                            if((removeAll || next.isExpire()) && readyCloseConnection.offerFirst(next)){
                                iterator.remove();
                            }
                        }
                        if(connectionWrappers.size() == 0){
                            cache.remove(t.getId() + "");
                            continue;
                        }
                    }
                    threads.offerFirst(t);
                    Thread.sleep(5000);
                }
            }
        } catch (InterruptedException e) {
            checkUnusableConnectionRunning = false;
            checkUnusableConnectionFlag = false;
            throw new RuntimeException(e);
        }
    }

    private void closeConnection() {
        closeConnectionRunning = true;
        try {
            while (closeConnectionFlag) {
                readyCloseConnection.takeLast().disconnect();
            }
        } catch (InterruptedException e) {
            closeConnectionRunning = false;
            closeConnectionFlag = false;
            throw new RuntimeException(e);
        }
    }

    private synchronized void startTask() {
        if (!closeConnectionRunning || !closeConnectionFlag) {
            closeConnectionFlag = true;
            Thread thread = new Thread(() -> this.closeConnection());
            thread.start();
        }

        if (!checkUnusableConnectionRunning || !checkUnusableConnectionFlag) {
            checkUnusableConnectionFlag = true;
            Thread thread = new Thread(() -> this.checkUnusableConnection());
            thread.start();
        }
    }

    public void stopTask(){
        closeConnectionFlag = false;
        checkUnusableConnectionFlag = false;
    }
}
