package com.spring.boot.tutorial.redis.scan;

import redis.clients.jedis.JedisCluster;

/**
 * @author cheny.huang
 * @date 2019-01-17 17:00.
 */
public class DefaultClusterScanOperation implements ClusterScanOperation {
    private final ClusterScanOperation aggressionScanOperation;
    private final ClusterScanOperation hashTagScanOperation;
    DefaultClusterScanOperation(JedisCluster jedisCluster) {
        aggressionScanOperation = new AggressionScanOperation(jedisCluster);
        hashTagScanOperation = new HashTagScanOperation(jedisCluster);
    }


    @Override
    public AggressionResult scan(String pattern, int limit) {
        return pattern.contains("}") && pattern.contains("}") ?
                hashTagScanOperation.scan(pattern, limit) : aggressionScanOperation.scan(pattern, limit);
    }

    @Override
    public AggressionResult scan(String pattern, int limit, AggressionResult aggressionResult) {
        return pattern.contains("}") && pattern.contains("}") ?
                hashTagScanOperation.scan(pattern, limit, aggressionResult) : aggressionScanOperation.scan(pattern, limit, aggressionResult);
    }
}
