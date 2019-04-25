package com.chen.spring.boot.advice;

import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 在返回结果中增加头部信息
 * @author cheny.huang
 * @date 2018/8/22
 */
public class ReturnTypePredicate implements HandlerInterceptor {

    private final static String RETURN_TYPE_HTTP_HEADER = "return-Type";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Class<?> returnType = handlerMethod.getReturnType().getParameterType();
            String type = determineSpecificType(returnType);
            if(!StringUtils.isEmpty(type)){
                response.setHeader(RETURN_TYPE_HTTP_HEADER, type);
            }
        }

        return true;
    }

    private String determineSpecificType(Class<?> type) {

        if (type == String.class) {
            return ReturnType.string.literal();
        } else if (type == Short.class || type == short.class) {
            return ReturnType.short0.literal();
        } else if (type == Integer.class || type == int.class) {
            return ReturnType.int0.literal();
        } else if (type == Long.class || type == long.class) {
            return ReturnType.long0.literal();
        } else if (type == Double.class || type == double.class) {
            return ReturnType.double0.literal();
        } else if (type == Float.class || type == float.class) {
            return ReturnType.float0.literal();
        } else if (type == Byte.class || type == byte.class) {
            return ReturnType.byte0.literal();
        } else if (type == Boolean.class || type == boolean.class) {
            return ReturnType.boolean0.literal();
        } else if (type == char.class || type == Character.class) {
            return ReturnType.char0.literal();
        } else {
            return null;
        }

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }


    enum ReturnType {
        /**
         * 基础类型或者装箱类型
         */
        byte0("byte"),

        int0("int"),

        char0("char"),

        short0("short"),

        long0("long"),

        float0("float"),

        double0("double"),

        boolean0("boolean"),

        /**
         * 字符类型
         */
        string("string");

        private String literal;

        ReturnType(String literal) {
            this.literal = literal;
        }

        public String literal() {
            return literal;
        }
    }
}
