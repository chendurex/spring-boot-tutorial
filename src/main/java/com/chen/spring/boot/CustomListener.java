package com.chen.spring.boot;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.util.IntrospectorCleanupListener;

/**
 * @author chen
 * @date 2017/8/17 13:50
 */
@Configuration
@AutoConfigureAfter(CustomContextParam.class)
public class CustomListener {

    @Bean
    public ServletListenerRegistrationBean<IntrospectorCleanupListener> introspectorCleanupListener() {
        ServletListenerRegistrationBean<IntrospectorCleanupListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new IntrospectorCleanupListener());
        return bean;
    }

    @Bean
    public ServletListenerRegistrationBean<RequestContextListener> requestContextListener() {
        ServletListenerRegistrationBean<RequestContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new RequestContextListener());
        return bean;
    }
}
