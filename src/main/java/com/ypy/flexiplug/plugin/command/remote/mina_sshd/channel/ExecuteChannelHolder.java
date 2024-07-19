package com.ypy.flexiplug.plugin.command.remote.mina_sshd.channel;

public class ExecuteChannelHolder implements ChannelHolder{

    /**
     * 当前命令在的绝对路径
     */
    private String path = "";

    public final static String SOURCE_PATH = "/home/gbasedbt/.bash_profile";

    private boolean source_flag = false;


    private String sourceContent = "";
    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public Object getChannel() {
        return null;
    }

    @Override
    public void setChannel(Object channel) {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path, boolean absolute) {
        if (absolute) {
            this.path = "";
        }
        if ("..".equals(path)) {
            int lastIndex = this.path.lastIndexOf("/");
            if (-1 == lastIndex) {
                this.path = "";
                return;
            }
            this.path = this.path.substring(0, lastIndex - 1);
            return;
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (this.path.endsWith("/")) {
            this.path = this.path.substring(0, this.path.length() - 2);
        }
        this.path = this.path + path;
    }

    public boolean getSource_flag() {
        return source_flag;
    }

    public void setSource_flag(boolean source_flag) {
        this.source_flag = source_flag;
    }

    public void source(){
        this.source_flag = true;
        this.sourceContent = SOURCE_PATH;
    }

    public void source(String sourceContent ){
        this.sourceContent = sourceContent;
        this.source_flag = true;
    }

    public String getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
    }

    public void init(){
        this.path = "";
        this.source_flag = false;
        this.sourceContent = "";
    }
}
