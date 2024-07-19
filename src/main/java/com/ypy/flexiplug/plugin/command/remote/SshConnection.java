package com.ypy.flexiplug.plugin.command.remote;

public interface SshConnection {


    /**
     * 执行命令
     * @param command
     * @return
     */
    ResultSet execute(String command);

    /**
     * source
     */
    SshConnection source();

    SshConnection source(String instanceName);

    /**
      * 切换目录
      * @param path
      */
    void cd(String path);

    /**
     * 切换目录
     * @param path
     * @param absolute
     */
    void cd(String path, boolean absolute);
}
