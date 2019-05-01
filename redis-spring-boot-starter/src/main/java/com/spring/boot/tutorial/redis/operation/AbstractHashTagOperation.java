package com.spring.boot.tutorial.redis.operation;

import com.spring.boot.tutorial.redis.scan.ClusterScanOperation;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 按照命名空间前缀key+hashtag存储key，默认为spring.application.name+key
 * 可以通过配置spring.application.redis.key.prefix修改默认前缀值
 * @author cheny.huang
 * @date 2019-01-05 15:04.
 */
public abstract class AbstractHashTagOperation<V> extends AbstractNamespaceOperation<V> {
    AbstractHashTagOperation(RedisTemplate<String, V> redisTemplate, ClusterScanOperation clusterScanOperation, String prefix) {
        super(redisTemplate,clusterScanOperation, prefix, true);
    }
}
