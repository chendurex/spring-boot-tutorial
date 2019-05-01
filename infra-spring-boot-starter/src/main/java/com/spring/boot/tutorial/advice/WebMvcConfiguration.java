package com.spring.boot.tutorial.advice;

import com.spring.boot.tutorial.advice.interceptor.CustomizePathLogInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.IntrospectorCleanupListener;

import java.util.List;

/**
 * @author cheny.huang
 * @date 2018-08-23 17:23.
 */
public class WebMvcConfiguration {
    /**
     * 动态注册拦截器，但是pattern只能固定，否则需要自定义一个对象进行改造
     * @param interceptors
     * @return
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer(List<HandlerInterceptor> interceptors) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                interceptors.forEach(i->registry.addInterceptor(i).addPathPatterns("/**"));
            }

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("PUT", "DELETE", "POST", "GET")
                        .allowCredentials(false);
            }
        };
    }

    @Bean
    HandlerInterceptor returnTypePredicate() {
        return new ReturnTypePredicate();
    }

    @Bean
    HandlerInterceptor pathLogInterceptor() {
        return new CustomizePathLogInterceptor();
    }

    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> encode() {
        FilterRegistrationBean<CharacterEncodingFilter> bean =  new FilterRegistrationBean<>();
        bean.setFilter(new CharacterEncodingFilter());
        bean.setName("encode");
        bean.addUrlPatterns("/*");
        bean.addInitParameter("encoding", "UTF-8");
        bean.addInitParameter("forceEncoding", "true");
        bean.setOrder(900);
        return bean;
    }

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
