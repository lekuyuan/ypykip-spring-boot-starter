package com.ypy.flexiplug.plugin.command.remote.jsch.model;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class SourceInfo {

    private String content;
    private int retryCount;

    public boolean run () {
        return StringUtils.isEmpty(content) && retryCount < 3;
    }

}
