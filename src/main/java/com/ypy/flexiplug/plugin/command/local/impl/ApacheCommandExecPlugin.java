package com.ypy.flexiplug.plugin.command.local.impl;

import com.ypy.flexiplug.plugin.command.local.ILocalCommandPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

@Slf4j
public class ApacheCommandExecPlugin implements ILocalCommandPlugin {
    public void asyncExecute(String command) {
        this.doAsyncExecute(command, new DefaultExecutor(), this.defaultExecuteStreamHandler(), this.defaultExecuteResultHandler(onComplete(), onFailed()));
    }

    public void asyncExecute(String command, Consumer<Integer> onComplete) {
        this.doAsyncExecute(command, new DefaultExecutor(), this.defaultExecuteStreamHandler(), this.defaultExecuteResultHandler(onComplete, onFailed()));
    }

    public void asyncExecute(String command, Consumer<Integer> onComplete, Consumer<ExecuteException> onFailed) {
        this.doAsyncExecute(command, new DefaultExecutor(), this.defaultExecuteStreamHandler(), this.defaultExecuteResultHandler(onComplete, onFailed));
    }

    public void asyncExecute(String command, ExecuteResultHandler executeResultHandler) {
        this.doAsyncExecute(command, new DefaultExecutor(), this.defaultExecuteStreamHandler(), executeResultHandler);
    }

    public void asyncExecute(String command, Executor executor, GBasePumpStreamHandler executeStreamHandler, ExecuteResultHandler executeResultHandler) {
        this.doAsyncExecute(command, executor, executeStreamHandler, executeResultHandler);
    }

    private void doAsyncExecute(String command, Executor executor, GBasePumpStreamHandler executeStreamHandler, ExecuteResultHandler executeResultHandler) {
        try {
            log.info("执行命令【{}】开始", command);
            CommandLine commandLine = CommandLine.parse(command);
            executeStreamHandler = (executeStreamHandler == null ? defaultExecuteStreamHandler() : executeStreamHandler);
            executor.setStreamHandler(executeStreamHandler);
            log.info("准备命令【{}】", command);
            executor.execute(commandLine, executeResultHandler == null ? defaultExecuteResultHandler(onComplete(), onFailed()) : executeResultHandler);
            log.info("命令【{}】执行结束", command);
            Thread.currentThread().join();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            OutputStream out;
            OutputStream err;
            try {
                if (null != (out = executeStreamHandler.out())) {
                    out.close();
                }
                if (null != (err = executeStreamHandler.err())) {
                    err.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ExecuteResultHandler defaultExecuteResultHandler(Consumer<Integer> onComplete, Consumer<ExecuteException> onFailed) {
        return new ExecuteResultHandler() {
            @Override
            public void onProcessComplete(int i) {
                if (null != onComplete) {
                    onComplete.accept(i);
                }
            }

            @Override
            public void onProcessFailed(ExecuteException e) {
                if (null != onFailed) {
                    onFailed.accept(e);
                }
            }
        };
    }

    private GBasePumpStreamHandler defaultExecuteStreamHandler() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        return new GBasePumpStreamHandler(stream, errStream);
    }

    private Consumer<Integer> onComplete() {
        return i -> log.info("命令执行成功！返回结果为【{}】", i);
    }

    private Consumer<ExecuteException> onFailed() {
        return e -> log.error("命令执行失败！错误信息为【{}】", e.getMessage());
    }
}
