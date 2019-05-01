package com.spring.boot.tutorial.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.lang.reflect.Field;

/**
 * 兼容旧版本的{@link JedisCommands}底层接口
 * @author cheny.huang
 * @date 2018-08-14 09:35.
 */
public class RedisConfiguration {
    private static final String PASSWORD = "spring.redis.cluster.password";
    @Bean
    @ConditionalOnProperty(name = "spring.redis.reactive.enable", havingValue = "true")
    public LettuceConnectionFactory reactiveRedisConnectionFactory(RedisPropertiesConfiguration propertiesConfiguration) {
        return new LettuceConnectionFactory(new RedisClusterConfiguration(propertiesConfiguration));
    }

    @Bean
    @ConditionalOnBean(LettuceConnectionFactory.class)
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(LettuceConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
    }


    @Bean
    @ConditionalOnProperty(name = "spring.redis.reactive.enable", havingValue = "false", matchIfMissing = true)
    public JedisConnectionFactory redisConnectionFactory(RedisPropertiesConfiguration propertiesConfiguration) {
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration(propertiesConfiguration);
        if (propertiesConfiguration.containsProperty(PASSWORD)) {
            String password = String.valueOf(propertiesConfiguration.getProperty(PASSWORD));
            clusterConfiguration.setPassword(RedisPassword.of(password));
        }
        return new JedisConnectionFactory(new FaultyClusterDiscovery().discoveryAvailableNodes(clusterConfiguration));
    }


    @Bean(name = {"jedisCommands", "jedisCluster"})
    @ConditionalOnBean(JedisConnectionFactory.class)
    public JedisCluster jedisCommands(JedisConnectionFactory factory) {
        try {
            Field field = JedisConnectionFactory.class.getDeclaredField("cluster");
            field.setAccessible(true);
            return (JedisCluster) field.get(factory);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("获取RedisCluster失败,", e);
        }
    }
}
