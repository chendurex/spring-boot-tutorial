package com.spring.boot.tutorial.redis.scan;

import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisCluster;

/**
 * 提供不同的方式存储key
 * @author cheny.huang
 * @date 2019-01-05 15:05.
 */
public class ScanOperationConfiguration {
    @Bean
    ClusterScanOperation clusterScanOperation(JedisCluster jedisCluster) {
        return new DefaultClusterScanOperation(jedisCluster);
    }
}
