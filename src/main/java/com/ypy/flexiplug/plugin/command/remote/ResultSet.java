package com.ypy.flexiplug.plugin.command.remote;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class ResultSet {

    private int exitCode = 0;

    private List<String> results;

    public void setResults(String result){
         results = Arrays.asList(result.trim().split("\n"));
    }

}
