package com.ypy.flexiplug.plugin.fileStore.impl;

import com.ypy.flexiplug.plugin.fileStore.IFileStorePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
public class LocalFileStorePlugin implements IFileStorePlugin {
    @Override
    public FileStoreInfo localStore(MultipartFile file, String storePath) {
        log.info("file name is :" + file.getOriginalFilename() + ", storePath: " + storePath);
        FileStoreInfo storeInfo = new FileStoreInfo();
        try {
            File path = new File(storePath);
            String lastName = path.getName();
            if (lastName.contains(".")) {
                path.getParentFile().mkdirs();
                File dest = new File(path.getParentFile(), file.getOriginalFilename());
                file.transferTo(dest);
                storeInfo.setStorePath(dest.getAbsolutePath()).setStore(true).setFileName(file.getOriginalFilename());
            } else {
                if (!path.exists()) {
                    path.mkdirs();
                }
                File dest = new File(path,file.getOriginalFilename());
                file.transferTo(dest);
                storeInfo.setStorePath(dest.getAbsolutePath()).setStore(true).setFileName(file.getOriginalFilename());
            }

            log.info("file is store in the specified path successfully");
            return storeInfo;

        } catch (IOException e) {
            log.error("error when store file: {}", e.getMessage(), e);
            storeInfo.setStore(false).setStorePath("").setFileName(file.getOriginalFilename());
            return storeInfo;
        }
    }

}
