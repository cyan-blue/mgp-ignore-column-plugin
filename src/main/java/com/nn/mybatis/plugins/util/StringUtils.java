package com.nn.mybatis.plugins.util;

import com.nn.mybatis.plugins.StatementTypeReplace;

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

    public static boolean isTextMatch(String targetText, String matchFieldName, StatementTypeReplace statementTypeReplace) {
        return (targetText.matches(String.format(statementTypeReplace.getRegex(), matchFieldName)));
    }

    public static void main(String[] args) {
        String targetText = "set app_count = #{appCount,jdbcType=INTEGER},";
        String fieldName = "app_count";
        System.out.println(isTextMatch(targetText, fieldName, StatementTypeReplace.UPDATE_SET_PLUS_EQUAL));
    }
}
