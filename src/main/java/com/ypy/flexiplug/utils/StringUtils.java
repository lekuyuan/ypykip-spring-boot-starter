package com.ypy.flexiplug.utils;

public abstract class StringUtils {
    public static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

    public static boolean isNotEmpty(Object str) {
        return !isEmpty(str);
    }

    public static boolean isNotBlank(Object str) {
        return !isEmpty(str);
    }
}