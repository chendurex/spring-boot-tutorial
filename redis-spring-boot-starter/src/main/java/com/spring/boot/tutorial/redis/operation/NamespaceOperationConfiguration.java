package com.spring.boot.tutorial.redis.operation;

import com.spring.boot.tutorial.redis.scan.ClusterScanOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 提供不同的方式存储key
 * @author cheny.huang
 * @date 2019-01-05 15:05.
 */
public class NamespaceOperationConfiguration<V> {
    @Primary
    @Bean(name = "redisTemplate")
    RedisTemplate<String, V> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, V> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean(name = "prefixOperation")
    AbstractPrefixOperation<V> prefixOperation(RedisTemplate<String, V> redisTemplate, ClusterScanOperation clusterScanOperation,
                                               @Value("${spring.application.redis.key.prefix:${spring.application.name}}")String prefix) {
        return new AbstractPrefixOperation<V>(redisTemplate,clusterScanOperation, prefix) {};
    }

    @Bean(name = "hashTagOperation")
    AbstractHashTagOperation<V> hashTagOperation(RedisTemplate<String, V> redisTemplate, ClusterScanOperation clusterScanOperation,
                                                 @Value("${spring.application.redis.key.prefix:${spring.application.name}}")String prefix) {
        return new AbstractHashTagOperation<V>(redisTemplate, clusterScanOperation, prefix) {};
    }

    @Bean(name = "defaultOperation")
    @Primary
    AbstractDefaultOperation<V> defaultOperation(RedisTemplate<String, V> redisTemplate, ClusterScanOperation clusterScanOperation) {
        return new AbstractDefaultOperation<V>(redisTemplate, clusterScanOperation) {};
    }
}
