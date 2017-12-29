package com.chen.spring.boot;

import com.chen.spring.boot.thrid.Constants;
import com.chen.spring.boot.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * @author chen
 * @date 2017/8/17 13:59
 */
@Configuration
@AutoConfigureAfter(value = {CustomContextParam.class, BasicConfig.class})
@EnableConfigurationProperties(ContextParamProp.class)
public class CustomFilter {
    @Autowired
    private ContextParamProp customFilterProp;

    @Bean
    public FilterRegistrationBean encode() {
        FilterRegistrationBean bean =  new FilterRegistrationBean();
        bean.setFilter(new CharacterEncodingFilter());
        bean.setName("encode");
        bean.addUrlPatterns("/*");
        bean.addInitParameter("encoding", "UTF-8");
        bean.addInitParameter("forceEncoding", "true");
        bean.setOrder(900);
        return bean;
    }

    @Bean
    @ConditionalOnClass(name = Constants.CUSTOM_LOG4J_MDC_FILTER)
    public FilterRegistrationBean customLog4jMDCFilter() {
        return createFilter(Constants.CUSTOM_LOG4J_MDC_FILTER, 2000);
    }

    private FilterRegistrationBean createFilter(String name, int order) {
        return createFilter(name, "/*", order);
    }

    private FilterRegistrationBean createFilter(String name, String pattern, int order) {
        FilterRegistrationBean bean =  new FilterRegistrationBean();
        bean.setFilter(Util.createFilter(name));
        bean.setName(Util.getSimpleNameFromClassName(name));
        bean.addUrlPatterns(pattern);
        bean.setOrder(order);
        bean.setEnabled(customFilterProp.notContainsFilter(name));
        return bean;
    }
}
