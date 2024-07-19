package com.ypy.flexiplug.plugin.software;

public interface SoftwareManage {

    boolean remoteUpload(String user, String pwd, String uploadPath);

    boolean localUpload(String uploadPath);

    boolean localUnZip(String targetPath);

    boolean remoteUnZip(String targetPath);

    boolean localStart(String cmd);

    boolean remoteStart(String cmd);

    boolean localStop(String cmd);

    boolean remoteStop(String cmd);


    boolean rename(String oldName, String newName);

    boolean delSoftware(String name);

    String cd(String path);


}
