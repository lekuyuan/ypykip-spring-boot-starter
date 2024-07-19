package com.ypy.flexiplug.config;

import com.ypy.flexiplug.plugin.locale.impl.LocalePlugin;
import com.ypy.flexiplug.utils.SpringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class FlexPlugEnableAutoConfiguration {

    @Bean("InnerMessageSource")
    @ConditionalOnBean(LocalePlugin.class)
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean("springUtils")
    public SpringUtils springUtils() {
        return new SpringUtils();
    }
}
