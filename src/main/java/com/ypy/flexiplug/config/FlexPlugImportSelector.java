package com.ypy.flexiplug.config;

import com.ypy.flexiplug.annotation.EnableFlexPlug;
import com.ypy.flexiplug.mark.Plugin;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FlexPlugImportSelector implements ImportSelector, BeanFactoryAware {
    private BeanFactory beanFactory;

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        importingClassMetadata.getAnnotationTypes().forEach(System.out::println);

        System.out.println(beanFactory);

        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableFlexPlug.class.getName())
        );
        // 在这里可以拿到所有注解的信息，可以根据不同注解的和注解的属性来返回不同的class,
        Class<Plugin>[] plugins = (Class<Plugin>[]) annotationAttributes.get("plugins");
        System.out.println("plugins: ==> " + plugins);
        List<String> pluginNames = Arrays.stream(plugins).map(plugin -> plugin.getName()).collect(Collectors.toList());
        System.out.println("pluginNames: ++>" + pluginNames);
        return pluginNames.toArray(new String[pluginNames.size()]);
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
