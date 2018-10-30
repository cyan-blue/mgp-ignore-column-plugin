package com.nn.mybatis.plugins.util;

/**
 * @author huangyan
 * @version 1.0 2018-10-30.
 */

public class StringUtils {
    private StringUtils() {
    }

    /**
     * 首字母大写
     */
    public static String captureName(String name) {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }
}
