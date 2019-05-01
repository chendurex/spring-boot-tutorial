package com.spring.boot.tutorial.advice.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author cheny.huang
 * @date 2018-12-07 15:24.
 */
@Slf4j
@ControllerAdvice
public class CustomizeResInterceptor implements ResponseBodyAdvice {
    private final CustomizeRequestMonitor monitor;

    CustomizeResInterceptor(CustomizeRequestMonitor monitor) {
        this.monitor = monitor;
    }
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        return monitor.beforeBodyWrite(body, returnType, selectedContentType, selectedConverterType, request, response);
    }
}
