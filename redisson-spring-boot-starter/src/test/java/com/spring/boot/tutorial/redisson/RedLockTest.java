package com.spring.boot.tutorial.redisson;

import org.junit.Test;
import org.redisson.RedissonRedLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static org.junit.Assert.*;

/**
 * @author cheny.huang
 * @date 2018-12-22 17:42.
 */
public class RedLockTest extends BaseApplication {
    @Autowired
    private RedissonClient redissonClient;
    private static final String PREFIX = "redlock:test:";
    @Test
    public void testRedLock() throws Exception {
        RedissonRedLock lock = new RedissonRedLock(redissonClient.getLock(PREFIX +"1"),
                redissonClient.getLock(PREFIX +"2"), redissonClient.getLock(PREFIX +"3"));
        assertTrue(lock.tryLock());
        RedissonRedLock lock2 = new RedissonRedLock(redissonClient.getLock(PREFIX +"1"),
                redissonClient.getLock(PREFIX +"2"), redissonClient.getLock(PREFIX +"3"));
        new Thread(()->assertFalse(lock2.tryLock()));
        lock.unlock();
        new Thread(()->assertTrue(lock2.tryLock()));
    }

    @Test
    public void testLock() throws Exception {
        new Thread(()-> assertTrue(tryLock(1))).start();
        TimeUnit.MILLISECONDS.sleep(100);
        new Thread(()-> assertFalse(tryLock(1))).start();
        TimeUnit.SECONDS.sleep(3);
        new Thread(()-> assertTrue(tryLock(1))).start();
        TimeUnit.MILLISECONDS.sleep(100);
        new Thread(()-> assertTrue(tryLock(4))).start();
        TimeUnit.SECONDS.sleep(5);
    }


    private boolean tryLock(int wait, int lease) {
        String lockKey = "redis-spring-dislock-test-1";
        try {
            return redissonClient.getLock(lockKey).tryLock(wait, lease, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        fail();
        return false;
    }

    private boolean tryLock(int wait) {
        String lockKey = "redis-spring-dislock-test-2";
        try {
            Lock lock = redissonClient.getLock(lockKey);
            return lock.tryLock(wait, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        fail();
        return false;
    }

}
