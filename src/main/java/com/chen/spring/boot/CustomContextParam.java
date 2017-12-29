package com.chen.spring.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Map;

/**
 * @author chen
 * @date 2017/8/17 14:22
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(ContextParamProp.class)
public class CustomContextParam {
    @Autowired
    private ContextParamProp contextParam;
    @Bean
    public ServletContextInitializer initializer() {
        return new ServletContextInitializer() {

            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                Map<String, String> customParam = contextParam.getContextParam();
                for (Map.Entry<String, String> value : customParam.entrySet()) {
                    servletContext.setInitParameter(value.getKey(), value.getValue());
                }
            }
        };
    }
}
