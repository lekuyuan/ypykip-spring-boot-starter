package com.ypy.flexiplug.plugin.command.remote.mina_sshd;

import com.ypy.flexiplug.plugin.command.remote.ConnectInfo;
import com.ypy.flexiplug.plugin.command.remote.SftpConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class MinaSftpConnectionWrapper <T extends ClientSession> extends AbstractMinaSshdConnectionWrapper<T> implements SftpConnection {
    public MinaSftpConnectionWrapper(ClientSession connection) {
        super(connection);
    }

    public MinaSftpConnectionWrapper(ConnectInfo connectInfo, String password) {
        super(connectInfo, password);
    }

    @Override
    public void upload(InputStream inputStream, String remoteDirPath) {
        this.upload(inputStream, remoteDirPath, 0);
    }

    @Override
    public void upload(InputStream inputStream, String remoteDirPath, int mode) {
        log.info("准备上传文件【{}】，上传模式【{}】...", remoteDirPath, mode);
        try(SftpClient sftpClient = SftpClientFactory.instance().createSftpClient(connection)) {
            SftpClient.CloseableHandle handle = null;
            if(0 == mode) {
                handle = sftpClient.open(remoteDirPath, SftpClient.OpenMode.Write, SftpClient.OpenMode.Create);
            }
            if(2 == mode){
                handle = sftpClient.open(remoteDirPath, SftpClient.OpenMode.Append, SftpClient.OpenMode.Create);
            }
            if(null == handle){
                log.error("不支持的上传模式，mode：【{}】", mode);
                throw new RuntimeException("不支持的上传模式，mode：【" + mode + "】");
            }
            int buff_size = 1024*1024;
            byte[] src = new byte[buff_size];
            int len;
            long fileOffset = 0L;
            while ((len = inputStream.read(src)) != -1) {
                sftpClient.write(handle, fileOffset, src, 0, len);
                fileOffset += len;
            }
            log.info("上传文件【{}】成功！", remoteDirPath);
        } catch (IOException e) {
            log.info("上传文件【{}】失败！原因：【{}】", remoteDirPath, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void download(String directory, OutputStream os) {
        log.info("准备下载文件【{}】...", directory);
        try(SftpClient sftpClient = SftpClientFactory.instance().createSftpClient(connection);
            InputStream inputStream = sftpClient.read(directory))
        {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //try(SftpFileSystem sftpFileSystem = (SftpFileSystem) SftpClientFactory.instance().createSftpClient(connection)) {
        //    Path remoteFile = sftpFileSystem.getDefaultDir().resolve(directory);
        //    Files.copy(remoteFile, os);
        //    log.info("下载文件【{}】成功！", directory);
        //} catch (IOException e) {
        //    log.info("下载文件【{}】失败！原因：【{}】", directory, e.getMessage());
        //    throw new RuntimeException(e);
        //}
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MinaSftpConnectionWrapper) {
            return (this.userName.equals(((MinaSftpConnectionWrapper) obj).userName)
                    && this.port == ((MinaSftpConnectionWrapper) obj).port
                    && this.ip.equals(((MinaSftpConnectionWrapper) obj).ip))
                    && this.password.equals(((MinaSftpConnectionWrapper)obj).password);
        }
        return false;
    }
}
