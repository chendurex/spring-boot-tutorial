package com.spring.boot.tutorial.redis;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * 获取redis环境配置
 * spring-redis-data提供了{@link PropertySource}的方式获取配置信息
 * 而所有的系统的配置都是来自配置中心，所以可以直接适配到容器中获取配置信息
 * @author cheny.huang
 * @date 2018-08-14 09:28.
 */
@Configuration
public class RedisPropertiesConfiguration extends PropertySource implements EnvironmentAware {
    private Environment env;
    @Override
    public void setEnvironment(Environment environment) {
        env = environment;
    }

    public RedisPropertiesConfiguration() {
        super(RedisPropertiesConfiguration.class.getSimpleName());
    }

    @Override
    public Object getProperty(String s) {
        return env.getProperty(s);
    }
}
