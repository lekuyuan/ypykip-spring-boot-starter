package com.ypy.flexiplug.plugin.split.impl;

import com.ypy.flexiplug.plugin.split.ISplitPlugin;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SplitService implements ISplitPlugin {
    @SuppressWarnings("all")
    @Override
    public List<String> split(String value) {
        return Stream.of(StringUtils.split(value, ",")).collect(Collectors.toList());
    }
}

