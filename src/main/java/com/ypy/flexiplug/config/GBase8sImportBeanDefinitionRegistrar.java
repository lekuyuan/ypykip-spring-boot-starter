package com.ypy.flexiplug.config;

import com.ypy.flexiplug.annotation.EnableGBase8s;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class GBase8sImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableGBase8s.class.getName())
        );

        // 在这里可以拿到所有注解的信息，可以根据不同注解的和注解的属性来返回不同的class,
        Object object = annotationAttributes.get("value");
        boolean isTrue = object == null ? false : Boolean.parseBoolean(object.toString());

        // if (isTrue) {
        //     Class beanClass = SplitServiceImpl.class;
        //     RootBeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
        //     String beanName = StringUtils.uncapitalize(beanClass.getSimpleName());
        //     // 在这里可以拿到所有注解的信息，可以根据不同注解来返回不同的class,从而达到开启不同功能的目的
        //
        //     // 通过这种方式可以自定义beanName
        //     registry.registerBeanDefinition(beanName, beanDefinition);
        // }

    }
}
