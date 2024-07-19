package com.ypy.flexiplug.plugin.secret;


public interface SecretProcessor {

    String encrypt(String data);

    String decrypt(String data);
}
