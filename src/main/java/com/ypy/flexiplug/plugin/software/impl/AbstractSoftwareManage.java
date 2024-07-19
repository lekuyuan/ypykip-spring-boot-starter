package com.ypy.flexiplug.plugin.software.impl;

import com.ypy.flexiplug.plugin.software.SoftwareManage;

public abstract class AbstractSoftwareManage implements SoftwareManage {
    @Override
    public boolean remoteUpload(String user, String pwd, String uploadPath) {
        return false;
    }

    @Override
    public boolean localUpload(String uploadPath) {
        return false;
    }

    @Override
    public boolean localUnZip(String targetPath) {
        return false;
    }

    @Override
    public boolean remoteUnZip(String targetPath) {
        return false;
    }

    @Override
    public boolean localStart(String cmd) {
        return false;
    }

    @Override
    public boolean remoteStart(String cmd) {
        return false;
    }

    @Override
    public boolean localStop(String cmd) {
        return false;
    }

    @Override
    public boolean remoteStop(String cmd) {
        return false;
    }

    @Override
    public boolean rename(String oldName, String newName) {
        return false;
    }

    @Override
    public boolean delSoftware(String name) {
        return false;
    }

    @Override
    public String cd(String path) {
        return null;
    }
}
