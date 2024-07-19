package com.ypy.flexiplug.plugin.locale.impl;

import com.ypy.flexiplug.plugin.locale.ILocalePlugin;
import com.ypy.flexiplug.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Slf4j
public class LocalePlugin implements ILocalePlugin {

    @Resource
    private SpringUtils springUtils;

    public final String getMessage(String key, String... args) {
        MessageSource messageSource = null;
        try {
            messageSource = springUtils.getBean("InnerMessageSource");
        } catch (BeansException e) {
            log.error("Not find a bean of type for LocalePlugin, so you must enable LocalePlugin!");
            throw new RuntimeException(e);
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String acceptLanguage = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (acceptLanguage.toLowerCase().contains("en")) {
            return messageSource.getMessage(key, args, Locale.US);
        }
        return messageSource.getMessage(key, args, Locale.SIMPLIFIED_CHINESE);
    }

}
