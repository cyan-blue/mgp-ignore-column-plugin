package com.nn.mybatis.plugins;

/**
 * @author huangyan
 * @version 1.0 2018/10/30.
 */
public enum StatementTypeReplace {
    /**
     * 字段加逗号
     */
    FIELD_PLUS_COMMA("^%s,"),

    INSERT_SELECTIVE("\\#\\{%s,jdbcType.*"),

    UPDATE_SET_PLUS_EQUAL("set %s = \\#\\{\\S+\\},"),

    FIELD_PLUS_EQUAL("^%s =.*");

    private String regex;

    StatementTypeReplace(String regex) {
        this.regex = regex;
    }


    public String getRegex() {
        return regex;
    }

}
