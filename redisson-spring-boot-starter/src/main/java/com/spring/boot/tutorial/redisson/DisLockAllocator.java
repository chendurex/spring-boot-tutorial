package com.spring.boot.tutorial.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonRedLock;
import org.redisson.api.RedissonClient;

/**
 * redis执行器代理
 * @author cheny.huang
 * @date 2018-12-24 16:40.
 */
@Slf4j
public class DisLockAllocator {
    private final RedissonClient redissonClient;
    private final boolean redlock;
    private static final String RED_LOCK_PREFIX = "red:lock:prefix:";
    private static final String COMMON_LOCK_PREFIX = "common:lock:prefix:";
    public DisLockAllocator(RedissonClient redissonClient, boolean redlock) {
        this.redissonClient = redissonClient;
        this.redlock = redlock;
        log.info("构建分布式锁服务成功，enabled redlock:{}", redlock);
    }
    public DistributedLock generator(String key) {
        log.debug("开始执行分布式锁操作,key:{}", key);
        if (redlock) {
            return new RedLockImpl(new RedissonRedLock(redissonClient.getLock(RED_LOCK_PREFIX + key)));
        } else {
            return new DefaultDLock(this.redissonClient.getLock(COMMON_LOCK_PREFIX + key));
        }
    }
}
