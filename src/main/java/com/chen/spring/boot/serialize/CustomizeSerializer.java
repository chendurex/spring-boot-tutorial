package com.chen.spring.boot.serialize;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * @author chen
 * @date 2017/8/15 17:18
 */
@Configuration
public class CustomizeSerializer {

    @Bean
    public Jackson2ObjectMapperFactoryBean jackson2ObjectMapperFactoryBean() {
        Jackson2ObjectMapperFactoryBean factoryBean = new Jackson2ObjectMapperFactoryBean();
        factoryBean.setSerializers(new LongTypeToStringTypeSerializer());
        return factoryBean;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(jackson2ObjectMapperFactoryBean().getObject());
        return converter;
    }
}
