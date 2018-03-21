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
 * @description 从配置中心获取数据，然后追加到propertySources中。
 *              但是这个局限性有局限性，比如有些条件是判断某个属性是否存在，而且初始化bean比当前bean还早，则无法获取属性，导致判断失败。
 *              正确的应该是在spring初始化时候，就将属性追加到环境变量中，通过org.springframework.context.ApplicationContextInitializer实现，
 *              这样就保证了所有的属性都在使用之前注入到环境变量中去了
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
