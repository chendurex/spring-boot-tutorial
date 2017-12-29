package com.chen.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chen
 * @date 2017/8/24 11:33
 */
@ConfigurationProperties(prefix = "spring.chen")
public class ContextParamProp {
    private Map<String, String> contextParam = new HashMap<>();
    private String filter;
    private String servlet;
    public Map<String, String> getContextParam() {
        return contextParam;
    }
    public void setContextParam(Map<String, String> contextParam) {
            this.contextParam = contextParam;
        }
    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getServlet() {
        return servlet;
    }

    public void setServlet(String servlet) {
        this.servlet = servlet;
    }

    public boolean notContainsFilter(String value) {
            return filter == null || !filter.contains(value);
    }

    public boolean notContainsServlet(String value) {
        return servlet == null || !servlet.contains(value);
    }
}
