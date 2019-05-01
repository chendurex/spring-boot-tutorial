package com.spring.boot.tutorial.util;

import org.springframework.util.ClassUtils;

import javax.servlet.Filter;

/**
 * @author chen
 * @date 2017/8/17 20:37
 */
public class Util {
    public static Object createInstance(String clz) {
        try {
            return getClass(clz).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getClass(String clz) {
        try {
            return ClassUtils.forName(clz, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Filter createFilter(String clz) {
        return (Filter) createInstance(clz);
    }

    public static String getSimpleNameFromClassName(String clz) {
        if (clz == null || clz.isEmpty()) {
            throw new IllegalArgumentException("传入的参数不能为空");
        }
        int index  = clz.lastIndexOf(".");
        if (index == -1) {
            throw new IllegalStateException("请传入正确的方法全限定名称");
        }
        return "springboot$"+clz.substring(index+1, index+2).toLowerCase()+clz.substring(index+2);
    }
}
