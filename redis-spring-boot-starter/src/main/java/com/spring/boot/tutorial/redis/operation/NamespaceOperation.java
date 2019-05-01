package com.spring.boot.tutorial.redis.operation;

import com.spring.boot.tutorial.redis.scan.ClusterScanOperation;
import org.springframework.data.redis.core.ValueOperations;

/**
 * 封装redis操作命令，按照命名空间形式存储数据，包括但不限于数据只落在同一个redis服务端、按照一定的规则存放数据key
 * @author cheny.huang
 * @date 2019-01-02 16:11.
 */
public interface NamespaceOperation<V> extends ValueOperations<String,V>, OptimizerRedisOperation {
    /**
     * @see ClusterScanOperation
     */
    ClusterScanOperation.AggressionResult scan(String pattern, int limit);

    ClusterScanOperation.AggressionResult scan(String pattern, int limit, ClusterScanOperation.AggressionResult aggressionResult);
}
