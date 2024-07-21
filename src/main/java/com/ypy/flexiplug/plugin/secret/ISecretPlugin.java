package com.ypy.flexiplug.plugin.secret;


import com.ypy.flexiplug.mark.Plugin;

public interface ISecretPlugin extends Plugin {

    String encrypt(String data);

    String decrypt(String data);
}
