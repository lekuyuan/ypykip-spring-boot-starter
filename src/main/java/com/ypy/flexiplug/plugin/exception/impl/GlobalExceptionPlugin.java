package com.ypy.flexiplug.plugin.exception.impl;

import com.ypy.flexiplug.plugin.exception.IGlobalExceptionPlugin;
import com.ypy.flexiplug.plugin.locale.impl.LocalePlugin;
import com.ypy.flexiplug.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@ControllerAdvice
public class GlobalExceptionPlugin implements IGlobalExceptionPlugin {

    private LocalePlugin localePlugin;
    @Resource
    private SpringUtils springUtils;

    @PostConstruct
    public void init() {
        try {
            localePlugin = springUtils.getBean(LocalePlugin.class);
        } catch (BeansException e) {
            log.error("The language internationalization plugin(LocalePlugin) has not been enabled!");
        }
    }

    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public String exceptionHandler(BusinessException be) {
        if (localePlugin == null) {
            log.warn(">>>>>>>>>> warn: 没有配置国际化插件(LocalePlugin)**************");
        } else {
            log.info(">>>>>>>>>> 启用了国际化插件(LocalePlugin)**************");
        }

        log.error("全局异常捕获(Business Exception) : {}", be.getMessage(), be);
        if (localePlugin == null) {
            return "全局异常捕获: >>> " + be.getMessage();
        }
        return localePlugin.getMessage(be.getBusCode(), null);
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public String exceptionHandler(Exception be) {
        if (localePlugin == null) {
            log.warn(">>>>>>>>>> warn: 没有配置国际化插件(LocalePlugin)**************");
        } else {
            log.info(">>>>>>>>>> 启用了国际化插件(LocalePlugin)**************");
        }
        log.error("全局异常捕获 :>>> {}", be.getMessage(), be);
        return "全局异常捕获>>>:" + be.getMessage();
    }


}
