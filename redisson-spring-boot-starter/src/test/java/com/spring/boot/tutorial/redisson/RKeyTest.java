package com.spring.boot.tutorial.redisson;

import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author cheny.huang
 * @date 2019-01-17 10:48.
 */
public class RKeyTest extends BaseApplication {
    @Autowired
    private RedissonClient redissonClient;
    private static final String PREFIX = "redis-scan-test-";
    @Test
    public void testScan() {
        for (int i=0;i<100;i++) {
            redissonClient.getBucket(PREFIX+i).set(PREFIX, 1, TimeUnit.HOURS);
        }
        redissonClient.getKeys().getKeys(1000).forEach(System.out::println);
    }
}
