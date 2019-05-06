package com.spring.boot.tutorial.rabbitmq.configuration;

import com.spring.boot.tutorial.rabbitmq.interceptor.RabbitListenerAroundAdvice;
import com.spring.boot.tutorial.rabbitmq.interceptor.ReturnCallBackListener;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Objects;
import java.util.Optional;

import static org.springframework.amqp.core.AcknowledgeMode.AUTO;
import static org.springframework.amqp.core.AcknowledgeMode.NONE;

/**
 * @author cheny.huang
 * @date 2018-07-30 16:40.
 */
public class MqConfiguration {

    @Bean
    public MqConfigProperties configProperties() {
        return new MqConfigProperties();
    }

    @Bean(name="connectionFactory")
    @ConditionalOnBean(MqConfigProperties.class)
    ConnectionFactory getConnectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        MqConfigProperties configProperties = configProperties();
        Objects.requireNonNull(configProperties.getHost(), "mq host未设置");
        Objects.requireNonNull(configProperties.getUsername(), "mq username 未设置");
        Objects.requireNonNull(configProperties.getPassword(), "mq password 未设置");
        Objects.requireNonNull(configProperties.getPort(), "mq port 未设置");
        connectionFactory.setHost(configProperties.getHost());
        connectionFactory.setUsername(configProperties.getUsername());
        connectionFactory.setPassword(configProperties.getPassword());
        connectionFactory.setPort(configProperties.getPort());
        return connectionFactory;
    }

    @Bean
    RabbitmqInterceptor rabbitListenerAroundAdvice() {
        return new RabbitListenerAroundAdvice();
    }

    @Bean
    RabbitmqInterceptor retryOperationsInterceptor() {
        // 注意，这个重试操作会间接的影响到拦截器，也就是说说如果拦截器抛出异常会执行重试操作
        // 还包括用户定义的监听器中，增加了返回值，但是又没有对应的消费者也会导致重试
        MethodInterceptor interceptor = RetryInterceptorBuilder.stateless()
                .backOffOptions(1000, 2, Optional.ofNullable(configProperties().getMaxInterval()).orElse(10000))
                .maxAttempts(Optional.ofNullable(configProperties().getMaxAttempts()).orElse(10)).build();
        return interceptor::invoke;
    }

    @Bean(name = "rabbitListenerContainerFactory")
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(RabbitmqInterceptor[] rabbitmqInterceptors) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(getConnectionFactory());
        factory.setAdviceChain(rabbitmqInterceptors);
        // 默认遵循以前的规则，暂时不开启事务，不管业务是否消费成功，都返回成功
        factory.setAcknowledgeMode(mode(configProperties().getMode()));
        factory.setMessageConverter(jackson2JsonMessageConverter());
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setMaxElapsedTime(120000);
        backOff.setMaxInterval(10000);
        factory.setMaxConcurrentConsumers(5);
        return factory;
    }

    /**
     * 支持自动确认功能，但是不支持手动确认，防止出现业务没有手动确认导致连接泄漏
     * 并且自动确认功能完全可以模拟手动确认功能
     */
    private AcknowledgeMode mode(String mode) {
        return Optional.ofNullable(mode)
                .filter(s->AUTO.name().equalsIgnoreCase(s))
                .map(s-> AcknowledgeMode.valueOf(s.toUpperCase())).orElse(NONE);
    }

    @Bean
    @ConditionalOnBean(RabbitTemplate.class)
    public RabbitTemplate getRabbitTemplate(RabbitTemplate rabbitTemplate) {
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        rabbitTemplate.setReturnCallback(new ReturnCallBackListener());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new CustomizedJackson2JsonMessageConverter();
    }
}
