package com.spring.boot.tutorial.redisson;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Repeat;
import redis.clients.jedis.JedisCluster;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author cheny.huang
 * @date 2018-12-25 10:45.
 */
public class FaultyLockTest extends BaseApplication {
    @Autowired
    private DisLockAllocator allocator;
    @Autowired
    private JedisCluster cluster;
    private static final String PREFIX = "redis:faulty:test:";

    @Test
    @Repeat(2)
    public void testOneSlaveBreakdown() throws Exception {
        String key = PREFIX + "1";
        DistributedLock lock = allocator.generator(key);
        assertTrue(lock.tryLock());
        // 随机关闭一个slave看看会不会影响到unlock操作以及后续的lock操作
        // slave挂了，不会影响到锁的，因为信息只写入master
        new RedisServerTest().shutdownRandomOneOfSlave(nodes());
        // 关闭一个slave，还是阻塞了其它的线程获取锁
        new Thread(()->assertFalse(allocator.generator(key).tryLock())).start();
        TimeUnit.MILLISECONDS.sleep(100);
        // 能正常解锁
        lock.unlock();
        // 还是能正常获取锁
        assertTrue(lock.tryLock(2, TimeUnit.SECONDS));
        lock.unlock();
    }

    @Test
    public void testOneMasterBreakdown() throws Exception {
        String key = PREFIX + "2";
        DistributedLock lock = allocator.generator(key);
        assertTrue(lock.tryLock());
        // 随机关闭一个master看看会不会影响到unlock操作以及后续的lock操作
        new RedisServerTest().shutdownRandomOneOfMaster(nodes());
        // todo 如果是使用redlock，那么在3主的情况下，只要有一个主挂了，无法获取到锁，必须等到slave晋升为master，redlock才能继续使用
        // 因为redlock获取的要求至少是>N/2+1个，所有有一个master挂了就无法获取到锁了
        new Thread(()->assertFalse(allocator.generator(key).tryLock())).start();
        TimeUnit.MILLISECONDS.sleep(100);
        // 能正常解锁
        lock.unlock();
        // 还是能正常获取锁
        assertTrue(lock.tryLock(2, TimeUnit.SECONDS));
        lock.unlock();
    }

    @Test
    public void testTwoSlaveBreakdown() throws Exception {
        String key = PREFIX + "3";
        DistributedLock lock = allocator.generator(key);
        assertTrue(lock.tryLock());
        // 随机关闭两个slave看看会不会影响到unlock操作以及后续的lock操作
        new RedisServerTest().shutdownTwoOfSlave(nodes());
        // 关闭两个slave，其它的线程可以还是不可以获取到锁
        new Thread(()->assertFalse(allocator.generator(key).tryLock())).start();
        TimeUnit.MILLISECONDS.sleep(100);
        // 能正常解锁
        lock.unlock();
        // 还是能正常获取锁
        assertTrue(lock.tryLock(2, TimeUnit.SECONDS));
        lock.unlock();
    }

    @Test
    public void testTwoMasterBreakdown() throws Exception {
        String key = PREFIX + "4";
        DistributedLock lock = allocator.generator(key);
        assertTrue(lock.tryLock());
        // 随机关闭两个master看看会不会影响到unlock操作以及后续的lock操作
        new RedisServerTest().shutdownTwoOfMaster(nodes());
        // 关闭两个master，其它的线程可以还是不可以获取到锁
        // todo 如果是使用redlock，那么在其它的slave没有晋升到master时，是无法获取到锁的
        new Thread(()->assertFalse(allocator.generator(key).tryLock())).start();
        TimeUnit.MILLISECONDS.sleep(100);
        // 能正常解锁
        lock.unlock();
        // 还是能正常获取锁
        assertTrue(lock.tryLock(2, TimeUnit.SECONDS));
        lock.unlock();
    }

    private String nodes() {
        return cluster.getClusterNodes().values().iterator().next().getResource().clusterNodes();
    }
}
