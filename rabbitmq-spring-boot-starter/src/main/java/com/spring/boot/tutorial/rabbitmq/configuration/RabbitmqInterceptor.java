package com.spring.boot.tutorial.rabbitmq.configuration;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * 代理interceptor，可以让业务方自定义自己的拦截器
 * @author cheny.huang
 * @date 2018-12-15 18:45.
 */
public interface RabbitmqInterceptor extends MethodInterceptor {
}
