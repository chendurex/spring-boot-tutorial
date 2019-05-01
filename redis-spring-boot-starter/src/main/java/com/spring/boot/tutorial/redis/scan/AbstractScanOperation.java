package com.spring.boot.tutorial.redis.scan;

import redis.clients.jedis.JedisCluster;

/**
 * @author cheny.huang
 * @date 2019-01-18 10:55.
 */
abstract class AbstractScanOperation implements ClusterScanOperation {
    private static final int MAX_LIMIT_THRESHOLD = 10000;
    private final JedisCluster jedisCluster;

    AbstractScanOperation(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    JedisCluster jedisCluster() {
        return jedisCluster;
    }

    @Override
    public AggressionResult scan(String pattern, int limit) {
        return scan(pattern, limit, new AggressionResult());
    }

    @Override
    public AggressionResult scan(String pattern, int limit, AggressionResult aggressionResult) {
        if (pattern == null || limit > MAX_LIMIT_THRESHOLD) {
            throw new IllegalArgumentException("limit is exceed 10000 or pattern is null");
        }
        return internalScan(pattern, limit, aggressionResult);
    }

    abstract AggressionResult internalScan(String pattern, int limit,AggressionResult aggressionResult);
}
