package com.chen.spring.boot;

import com.chen.spring.boot.thrid.Constants;
import com.chen.spring.boot.util.Util;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

/**
 * @author chen
 * @date 2017/8/15 17:14
 */
@Configuration
@ConditionalOnClass(name = Constants.PROPERTY_SOURCES_PLACEHOLDER_CONFIGURER_EXT)
// PropertyPlaceholderAutoConfiguration 优先级非常高，如果让它先初始化，则会存在多份资源属性文件，导致出现获取属性失败
@AutoConfigureBefore(PropertyPlaceholderAutoConfiguration.class)
public class PropertiesConfig {
    @Bean
    public PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurerExt =
                (PropertySourcesPlaceholderConfigurer) Util.createInstance(Constants.PROPERTY_SOURCES_PLACEHOLDER_CONFIGURER_EXT);
        propertyPlaceholderConfigurerExt.setLocation(new ClassPathResource("application.properties"));
        return propertyPlaceholderConfigurerExt;
    }
}
