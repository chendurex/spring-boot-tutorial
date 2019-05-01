package com.spring.boot.tutorial.redisson.configuration;

import com.spring.boot.tutorial.redisson.DisLockAllocator;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import redis.clients.jedis.JedisCluster;

import java.util.Set;

/**
 * @author cheny.huang
 * @date 2018-12-22 17:45.
 */

@ConditionalOnClass(Config.class)
public class EnableRedissonAutoConfiguration {
    private static final String PASSWORD = "spring.redis.cluster.password";
    /**
     * 集群模式自动装配
     *
     * @return
     */
    @Bean
    @Primary
    @ConditionalOnBean(JedisCluster.class)
    RedissonClient redissonCluster(JedisCluster jedisCluster, Environment environment) {
        Set<String> nodes = jedisCluster.getClusterNodes().keySet();
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("不存在可用节点，无法启动分布式锁");
        }
        Config config = new Config();
        ClusterServersConfig serverConfig = config.useClusterServers().setScanInterval(2000);
        if (environment.containsProperty(PASSWORD)) {
            serverConfig.setPassword(String.valueOf(environment.getProperty(PASSWORD)));
        }
        nodes.forEach(s -> serverConfig.addNodeAddress("redis://"+s));
        return Redisson.create(config);
    }

    /**
     *  todo redlock在锁过期后还能解锁成功，这个地方有问题，暂时不提供
     */
    @Bean
    DisLockAllocator disLockAllocator(RedissonClient redissonClient, @Value("${spring.redis.cluster.redlock:false}")String redlock) {
        return new DisLockAllocator(redissonClient, false);
    }
}
