package com.spring.boot.tutorial.advice.interceptor;

import com.spring.boot.tutorial.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author cheny.huang
 * @date 2018-09-07 15:44.
 */
@Slf4j
public class CustomizePathLogInterceptor extends HandlerInterceptorAdapter {
    private static final int SLOW_TIME = 3000;
    private static final String CONTENT_LENGTH = "content-length";
    private static final int LARGER_DATA_1M = 1<<20;
    private NamedThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<>("StopWatch-StartTime");

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) {
        startTimeThreadLocal.set(System.currentTimeMillis());
        Map<String, String[]> params = request.getParameterMap();
        if (log.isInfoEnabled()) {
            log.info("Request Start And RequestPath Is: {},header:{}, Params:{}",
                    getRealPath(request), getHeader(request), params.isEmpty() ? "{}" : BeanUtils.deepPrint(params));
        }
        return true;
    }

    private String getHeader(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaderNames();
        StringBuilder sb = new StringBuilder();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            sb.append(",").append(header).append("=").append(request.getHeader(header));
            if (CONTENT_LENGTH.equalsIgnoreCase(header)) {
                int length = Integer.valueOf(request.getHeader(header));
                if (LARGER_DATA_1M < length) {
                    log.error("调用方传入的数据量超过1M，请求接口为:{}", getRealPath(request));
                }
            }
        }
        return sb.length() > 0 ? sb.substring(1) : "";
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) {
        long consumeTime = getConsumeTime();
        if (consumeTime > SLOW_TIME) {
            log.warn("slow api ,{} executeTime {} ms", getRealPath(request), consumeTime);
        } else {
            if (log.isInfoEnabled()) {
                log.info("Request End: {}, executeTime: {} ms.", getRealPath(request), consumeTime);
            }
        }
    }

    private long getConsumeTime() {
        long endTime = System.currentTimeMillis();
        long beginTime = startTimeThreadLocal.get();
        startTimeThreadLocal.remove();
        return endTime - beginTime;
    }

    private String getRealPath(HttpServletRequest request) {
        String path = request.getServletPath();
        return (path == null || path.trim().length() == 0) ? request.getRequestURI() : path;
    }
}
