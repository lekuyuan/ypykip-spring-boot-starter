package com.ypy.flexiplug.plugin.secret.impl;

import com.ypy.flexiplug.plugin.secret.ISecretPlugin;
import com.ypy.flexiplug.utils.SpringUtils;
import com.ypy.flexiplug.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

@Slf4j
public class DefaultSecretPlugin implements ISecretPlugin {
    private String KEY;

    private String IV;

    @Resource
    private SpringUtils springUtils;

    @PostConstruct
    public void init() {
        Environment env = springUtils.getBean(Environment.class);
        String key = env.getProperty("application.secret.key");
        String iv = env.getProperty("application.secret.iv");
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(iv)) {
            log.error(">>>>>> 项目没有配置加密私钥与随机iv值");
            log.info(">>>>>> 配置方法: 在application.yml或application.property中配置${application.secret.key}与${application.secret.iv}");
            throw new RuntimeException("缺少key iv 配置");
        }

        this.KEY = key;
        this.IV = iv;
        log.info(">>>>>> 配置KEY,IV成功");
    }

    public DefaultSecretPlugin() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public String encrypt(String data) {
        return this.encrypt(data, KEY, IV);
    }

    public String decrypt(String data) {
        return this.decrypt(data, KEY, IV);
    }

    private String encrypt(String data, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            byte[] dataBytes = data.getBytes();
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(dataBytes);

            return new Base64().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String decrypt(String data, String key, String iv) {
        try {
            byte[] encrypted1 = new Base64().decode(data);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
