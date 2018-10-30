package com.nn.mybatis.plugins.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author huangyan
 * @version 1.0 2018-10-30.
 */

public class ReflectionUtils {
    private ReflectionUtils() {
    }

    /**
     * 根据类实例对象获取对应声明的方法实例
     * @param target 类实例
     * @param methodName 方法名
     * @return
     */
    public static Method getDeclaredMethodByInstance(Object target, String methodName) {
        Method method = null;
        Class<?> clazz = target.getClass();

        try {
            method = clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return method;
    }


    /**
     * 根据类实例获取对应入参类型的声明方法实例
     * @param target 类实例
     * @param methodName 方法名
     * @param parameterTypes 入参类型
     * @return
     */
    public static Method getDeclaredMethodByInstance(Object target, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        Class<?> clazz = target.getClass();

        try {
            method = clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return method;
    }


    /**
     * 根据Class获取其声明的方法实例
     * @param clazz
     * @param methodName 方法名
     * @return
     */
    public static Method getDeclaredMethodByClazz(Class<?> clazz, String methodName) {
        Method method = null;
        if (clazz == null) {
            return method;
        }

        try {
            method = clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return method;
    }


    /**
     * 获取对应实例顶层父类的对应filed值
     * @param fieldName filed名
     * @param target 实例对象
     * @return
     */
    public static Object getSuperFieldValue(String fieldName, Object target) {
        if (target == null) {
            return null;
        }
        try {
            Class superClass = target.getClass();
            while (superClass.getSuperclass() != Object.class) {
                superClass = superClass.getSuperclass();
            }
            Field targetField = superClass.getDeclaredField(fieldName);
            makeAccessible(targetField);
            return getField(targetField, target);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取实例对象的对应filed值
     * @param fieldName field名
     * @param target 实例对象
     * @return
     */
    public static Object getFieldValue(String fieldName, Object target) {
        if (target == null) {
            return null;
        }
        try {
            Field targetField = target.getClass().getDeclaredField(fieldName);
            makeAccessible(targetField);
            return getField(targetField, target);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置实例对象的对应filed值
     * @param fieldName filed名
     * @param target 实例对象
     * @param fieldObject filed值
     */
    public static void setFieldValue(String fieldName, Object target, Object fieldObject) {
        if (target == null) {
            return;
        }
        try {
            Field targetField = target.getClass().getDeclaredField(fieldName);
            makeAccessible(targetField);
            targetField.set(target, fieldObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取实例对象的对应field值
     * @param field filed对象
     * @param target 实例对象
     * @return
     */
    private static Object getField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从filed对象数组总选出对应名称的filed值
     * @param fields
     * @param fieldName
     * @return
     */
    public static Field getField(Field[] fields, String fieldName) {
        for (int i = 0; i < fields.length; i++) {
            try {
                //得到属性
                Field field = fields[i];
                //打开私有访问
                field.setAccessible(true);
                //获取属性
                String name = field.getName();
                if (fieldName.equals(name)) {
                    return field;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 打开属性访问域
     * @param field
     */
    private static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers())
                || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
                || Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {

            field.setAccessible(true);
        }
    }
}
