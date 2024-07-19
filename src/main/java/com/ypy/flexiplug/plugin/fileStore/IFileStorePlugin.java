package com.ypy.flexiplug.plugin.fileStore;

import com.ypy.flexiplug.mark.Plugin;
import com.ypy.flexiplug.plugin.fileStore.impl.FileStoreInfo;
import org.springframework.web.multipart.MultipartFile;

public interface IFileStorePlugin extends Plugin {
    /**
     * 上传文件到本地指定路径下
     * @param file
     * @param localPath
     * @return
     */
    public FileStoreInfo localStore(MultipartFile file, String localPath);
}
