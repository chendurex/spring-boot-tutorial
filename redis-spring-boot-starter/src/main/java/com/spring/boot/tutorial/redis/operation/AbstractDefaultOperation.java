package com.spring.boot.tutorial.redis.operation;

import com.spring.boot.tutorial.redis.scan.ClusterScanOperation;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 默认的redis存储数据方式，全部按照常规的方式存储数据
 * 客户端也可以基于默认的方式存储前缀+hashtag，这完全取决于客户端本身
 * 客户端最好是使用带前缀的命名空间，方便对数据进行隔离
 * @see AbstractHashTagOperation
 * @see AbstractPrefixOperation
 * @author cheny.huang
 * @date 2019-01-05 15:04.
 */
public abstract class AbstractDefaultOperation<V> extends AbstractNamespaceOperation<V> {
    AbstractDefaultOperation(RedisTemplate<String, V> redisTemplate, ClusterScanOperation clusterScanOperation) {
        super(redisTemplate, clusterScanOperation, null);
    }
}
