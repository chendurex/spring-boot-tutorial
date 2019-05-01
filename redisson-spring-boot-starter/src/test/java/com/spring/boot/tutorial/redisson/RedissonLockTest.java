package com.spring.boot.tutorial.redisson;

import org.junit.Assert;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author cheny.huang
 * @date 2018-12-22 17:42.
 */
public class RedissonLockTest extends BaseApplication {
    @Autowired
    private RedissonClient redissonClient;
    @Test
    public void testRLock() throws Exception {
        new Thread(()-> Assert.assertTrue(tryLock(1, 3))).start();
        TimeUnit.MILLISECONDS.sleep(100);
        new Thread(()-> Assert.assertFalse(tryLock(1, 3))).start();
        TimeUnit.SECONDS.sleep(3);
        new Thread(()-> Assert.assertTrue(tryLock(1, 3))).start();
        TimeUnit.MILLISECONDS.sleep(100);
        new Thread(()-> Assert.assertTrue(tryLock(4, 3))).start();
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void testLock() throws Exception {
        new Thread(()-> Assert.assertTrue(tryLock(1))).start();
        TimeUnit.MILLISECONDS.sleep(100);
        new Thread(()-> Assert.assertFalse(tryLock(1))).start();
        TimeUnit.SECONDS.sleep(3);
        new Thread(()-> Assert.assertTrue(tryLock(1))).start();
        TimeUnit.MILLISECONDS.sleep(100);
        new Thread(()-> Assert.assertTrue(tryLock(4))).start();
        TimeUnit.SECONDS.sleep(5);
    }


    private boolean tryLock(int wait, int lease) {
        String lockKey = "redis-spring-dislock-test-1";
        try {
            return redissonClient.getLock(lockKey).tryLock(wait, lease, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.fail();
        return false;
    }

    private boolean tryLock(int wait) {
        String lockKey = "redis-spring-dislock-test-2";
        try {
            Lock lock = redissonClient.getLock(lockKey);
            return lock.tryLock(wait, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.fail();
        return false;
    }

}
