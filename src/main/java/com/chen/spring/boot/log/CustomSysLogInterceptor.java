package com.chen.spring.boot.log;

import com.chen.spring.boot.log.print.ObjectMapperUtils;
import com.chen.spring.boot.log.print.TransactionResourcesMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chen
 * date 2017/3/2 20:39
 */
public class CustomSysLogInterceptor extends HandlerInterceptorAdapter {
    private static final int SLOW_TIME = 3000;
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private NamedThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<>("StopWatch-StartTime");

    private final String[] sensitiveFields = new String[]{"password", "pwd", "oldPassword", "oldPwd", "newPassword", "newPwd"};
    private final List<String> validFields = Arrays.asList(sensitiveFields);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) {
        startTimeThreadLocal.set(System.currentTimeMillis());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Request Start And RequestPath Is: {}", getRealPath(request));
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Map<String, String[]> params = request.getParameterMap();
        Map<String, String[]> newParams = this.filterSensitiveField(params);
        long consumeTime = getConsumeTime();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Params: {}", ObjectMapperUtils.get().writeValueAsString(newParams));
            LOGGER.info("End: {}. executeTime: {} ms.", getRealPath(request), consumeTime);
        }

        if (consumeTime > SLOW_TIME) {
            LOGGER.warn("slow api ,{} executeTime {} ms", getRealPath(request), consumeTime);
        }
        TransactionResourcesMonitor.cleanResourceIfHas();
    }

    private String getRealPath(HttpServletRequest request) {
        String path = request.getServletPath();
        return (path == null || path.trim().length() == 0) ? request.getRequestURI() : path;
    }

    private long getConsumeTime() {
        long endTime = System.currentTimeMillis();
        long beginTime = startTimeThreadLocal.get();
        return endTime - beginTime;
    }

    private Map<String, String[]> filterSensitiveField(Map<String, String[]> params) {
        Map<String, String[]> newParams = new HashMap<>(8);
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            final String key = entry.getKey();
            if (!validFields.contains(key)) {
                newParams.put(key, entry.getValue());
            }
        }
        return newParams;
    }
}
