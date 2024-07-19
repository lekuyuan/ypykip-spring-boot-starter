package com.ypy.flexiplug.plugin.command.remote;


import java.io.InputStream;
import java.io.OutputStream;

public interface SftpConnection {

    void upload(InputStream inputStream, String remoteDirPath);

    void upload(InputStream inputStream, String remoteDirPath, int mode);

    void download(String directory, OutputStream os);
}
