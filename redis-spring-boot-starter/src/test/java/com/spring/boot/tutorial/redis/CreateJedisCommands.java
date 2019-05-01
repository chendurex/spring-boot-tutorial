package com.spring.boot.tutorial.redis;

import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cheny.huang
 * @date 2018-09-12 11:42.
 */
class CreateJedisCommands {
    static JedisCommands generator() {
        Collection<String> nodes = Collections.singletonList("192.168.1.117:7000,192.168.1.117:7001,192.168.1.117:7002," +
                "192.168.1.117:7003,192.168.1.117:7004,192.168.1.117:7005,192.168.1.117:7006");
        Map<String, Object> map = new HashMap<>();
        map.put("spring.redis.cluster.nodes", StringUtils.collectionToCommaDelimitedString(nodes));
        map.put("spring.redis.cluster.max-redirects", 3);
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(
                new RedisClusterConfiguration(new MapPropertySource("RedisClusterConfiguration", map)));
        connectionFactory.afterPropertiesSet();
        try {
            Field field = JedisConnectionFactory.class.getDeclaredField("cluster");
            field.setAccessible(true);
            return  (JedisCluster) field.get(connectionFactory);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("获取RedisCluster失败,", e);
        }
    }
}
