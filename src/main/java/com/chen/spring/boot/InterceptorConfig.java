package com.chen.spring.boot;


import com.chen.spring.boot.thrid.Constants;
import com.chen.spring.boot.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author chen
 * @date 2017/8/16 9:37
 */
@Configuration
@ConditionalOnWebApplication
public class InterceptorConfig extends WebMvcConfigurerAdapter {

    @Bean
    @ConditionalOnClass(name = Constants.CUSTOM_SYS_LOG_INTERCEPTOR)
    @Autowired
    public HandlerInterceptor customSysLogInterceptor() {
        return (HandlerInterceptor) Util.createInstance(Constants.CUSTOM_SYS_LOG_INTERCEPTOR);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (hasCustomSysLogInterceptor()) {
            registry.addInterceptor(customSysLogInterceptor());
        }
    }


    private boolean hasCustomSysLogInterceptor() {
        try {
            Class.forName(Constants.CUSTOM_SYS_LOG_INTERCEPTOR);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}