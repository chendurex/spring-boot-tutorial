package com.spring.boot.toturial.advice.interceptor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.to8to.common.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * <p>监控每次请求请求的数据量大小</p>
 * <p>因为数据是序列化工具是采用jackson,而且数据是以流的形式发送出去，所以无法获取到数据的大小</p>
 * <p>这里采用这个取巧的办法，首先获取到原始需要发送出去的对象，然后序列化成字符串获取大小，再将当前数据
 * 记录到缓存中，下次就不用再次序列化对象了，减少性能损耗</p>
 * @author cheny.huang
 * @date 2019-04-16 18:59.
 */
@Slf4j
@Component
class CustomizeRequestMonitor {
    private final int errorDataSize;
    private final int warnDataSize;
    private static final ThreadLocal<Integer> REQUEST_PARAMS = ThreadLocal.withInitial(()->0);
    private Cache<Integer, Integer> requestMonitor;

    CustomizeRequestMonitor(@Value("${request.monitor.threshold.error:8388608}")Integer errorDataSize,
                            @Value("${request.monitor.threshold.warn:1048576}")Integer warnDataSize,
                            @Value("${request.monitor.threshold.cache:10000}")Integer cache) {
        this.errorDataSize = errorDataSize;
        this.warnDataSize = warnDataSize;
        init(cache);
    }

    private void init(int cache) {
        requestMonitor = CacheBuilder.newBuilder().maximumSize(cache).expireAfterWrite(1, TimeUnit.DAYS).build();
    }

    Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        String v = BeanUtils.deepPrint(body);
        if (v.length() > errorDataSize) {
            log.error("请求数据超过错误位设置大小:{},当前大小:{}，请减小数据量", errorDataSize, v.length());
        } else if (v.length() > warnDataSize) {
            log.warn("请求数据量超过错误位设置大小:{},当前大小:{}，请减小数据量", warnDataSize, v.length());
        }
        int hash = parameter.getExecutable().hashCode() ^ v.hashCode();
        REQUEST_PARAMS.set(hash);
        if (log.isInfoEnabled()) {
            log.info("Request Body Params: {}", v);
        }
        return body;
    }

    Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // lambda表达式无法赋值变量，为了性能，多了4行代码...
        Integer cached = requestMonitor.getIfPresent(REQUEST_PARAMS.get());
        String v = null;
        if (cached == null) {
            v = BeanUtils.deepPrint(body);
            cached = v.length();
            requestMonitor.put(REQUEST_PARAMS.get(), cached);
        }
        REQUEST_PARAMS.remove();
        if (cached > errorDataSize) {
            log.error("响应数据超过错误位设置大小:{},当前大小:{}，请减小数据量", errorDataSize, cached);
        } else if (cached > warnDataSize) {
            log.warn("响应数据量超过警告位设置大小:{},当前大小:{}，请减小数据量", warnDataSize, cached);
        }
        if (log.isDebugEnabled()) {
            log.debug("Response Body Params: {}", Optional.ofNullable(v).orElseGet(()->BeanUtils.deepPrint(body)));
        }
        return body;
    }
}
