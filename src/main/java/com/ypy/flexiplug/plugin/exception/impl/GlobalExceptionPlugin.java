package com.ypy.flexiplug.plugin.exception.impl;

import com.ypy.flexiplug.plugin.exception.IGlobalExceptionPlugin;
import com.ypy.flexiplug.plugin.locale.impl.LocalePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Locale;

@Slf4j
@ControllerAdvice
public class GlobalExceptionPlugin implements IGlobalExceptionPlugin,BeanFactoryAware, MessageSourceAware {

    private LocalePlugin localePlugin;

    private MessageSource messageSource;

    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public String exceptionHandler(BusinessException be) {
        if (localePlugin == null) {
            System.out.println("没有配置国际化插件");
        }else {
            System.out.println("配置了国际化插件, 地址:" + localePlugin);
        }
        System.out.println("localePlugin: " + localePlugin);
        System.out.println("全局异常捕获>>>:" + be);
        log.error("全局异常捕获(Business Exception) : {}",be.getMessage(), be);
        return messageSource.getMessage(be.getBusCode(),null, Locale.CHINA);
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public String exceptionHandler(Exception be) {
        if (localePlugin == null) {
            System.out.println("没有配置国际化插件");
        }else {
            System.out.println("配置了国际化插件, 地址:" + localePlugin);
        }
        System.out.println("localePlugin: " + localePlugin);
        System.out.println("全局异常捕获>>>:" + be);
        log.error( "全局异常捕获>>>: {}", be.getMessage(), be);
        return "全局异常捕获>>>:" + be.getMessage();
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        LocalePlugin localePluginBean = beanFactory.getBean(LocalePlugin.class);
        localePlugin = localePluginBean;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}
