package com.chen.spring.boot.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * @author chen
 * @date 2017/3/15 17:03
 */
public class CustomLog4jMDCFilter  implements Filter {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            MDC.put("uuid", UUID.randomUUID().toString().replace("-", ""));
            if (httpReq.getRequestedSessionId() != null) {
                MDC.put("sessionid", httpReq.getRequestedSessionId());
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            LOGGER.warn("MDC put error", e);
        } finally {
            MDC.remove("uuid");
            MDC.remove("sessionid");
        }
    }

    @Override
    public void destroy() {

    }
}
