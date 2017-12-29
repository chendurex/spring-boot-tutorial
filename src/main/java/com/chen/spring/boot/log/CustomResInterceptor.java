package com.chen.spring.boot.log;

import com.chen.spring.boot.log.print.ObjectMapperUtils;
import com.chen.spring.boot.log.print.TransactionResourcesMonitor;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author chen
 * @date 2017/3/2 20:39
 */
@ControllerAdvice
public class CustomResInterceptor implements ResponseBodyAdvice {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            TransactionResourcesMonitor.cleanResourceIfHas();
            LOGGER.info("Response Body Params: {}", ObjectMapperUtils.get().writeValueAsString(body));
        } catch (JsonProcessingException e) {
            LOGGER.warn("format value to jackson error, error stack is :", e);
        }
        return body;
    }
}
