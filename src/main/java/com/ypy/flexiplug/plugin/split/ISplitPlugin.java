package com.ypy.flexiplug.plugin.split;

import com.ypy.flexiplug.mark.Plugin;

import java.util.List;

public interface ISplitPlugin extends Plugin {
    List<String> split(String value);
}