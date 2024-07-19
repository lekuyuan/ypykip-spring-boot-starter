package com.ypy.flexiplug.plugin.locale;

import com.ypy.flexiplug.mark.Plugin;

public interface ILocalePlugin extends Plugin {
    String getMessage(String key, String... args);
}
