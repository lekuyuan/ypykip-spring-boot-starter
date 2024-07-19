package com.ypy.flexiplug.annotation;

import com.ypy.flexiplug.config.GBase8sImportSelector;
import com.ypy.flexiplug.mark.Plugin;
import com.ypy.flexiplug.plugin.command.local.impl.ApacheCommandExecPlugin;
import com.ypy.flexiplug.plugin.exception.impl.GlobalExceptionPlugin;
import com.ypy.flexiplug.plugin.fileStore.impl.LocalFileStorePlugin;
import com.ypy.flexiplug.plugin.split.impl.SplitService;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({GBase8sImportSelector.class})
public @interface EnableGBase8s {

    Class<? extends Plugin>[] plugins() default {};

    Class<? extends Plugin>[] allPlugins() default
            {
                    SplitService.class,
                    ApacheCommandExecPlugin.class,
                    LocalFileStorePlugin.class,
                    GlobalExceptionPlugin.class,
            };
}
