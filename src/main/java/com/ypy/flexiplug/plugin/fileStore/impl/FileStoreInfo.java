package com.ypy.flexiplug.plugin.fileStore.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class FileStoreInfo {
    private String storePath;
    private boolean isStore;
    private String fileName;
}
