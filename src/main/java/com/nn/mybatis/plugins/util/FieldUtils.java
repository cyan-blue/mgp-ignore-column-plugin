package com.nn.mybatis.plugins.util;

/**
 * @author huangyan
 * @version 1.0 2018-10-30.
 */

public class FieldUtils {
    private FieldUtils() {
    }

    public static String getModelNameByField(String fieldName) {
        String[] splitFields = fieldName.split("_");
        if (splitFields.length == 1) {
            return splitFields[0];
        }
        StringBuilder ret = new StringBuilder();
        ret.append(splitFields[0]);

        for (int i = 0; i < splitFields.length; i++) {
            if (i != 0) {
                ret.append(StringUtils.captureName(splitFields[i]));
            }
        }
        return ret.toString();
    }


    public static void main(String[] args) {
        System.out.println(getModelNameByField("create"));
        System.out.println(getModelNameByField("create_by"));
        System.out.println(getModelNameByField("create_by_name"));
    }
}
