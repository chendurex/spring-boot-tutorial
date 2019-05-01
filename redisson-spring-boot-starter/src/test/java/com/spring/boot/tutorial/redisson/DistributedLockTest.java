package com.spring.boot.tutorial.redisson;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Repeat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

/**
 * @author cheny.huang
 * @date 2018-12-24 16:46.
 */
public class DistributedLockTest extends BaseApplication {
    @Autowired
    private DisLockAllocator allocator;
    private static final String PREFIX = "dis:lock:test:";

    /**
     * https://github.com/redisson/redisson/wiki/2.-Configuration#lockwatchdogtimeout
     */
    @Test
    public void testWatchdogLock() throws Exception {
        String key = PREFIX + "x";
        DistributedLock lock = allocator.generator(key);
        lock.lock();
        new Thread(()-> assertFalse(allocator.generator(key).tryLock())).start();
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    public void testUnlockRedLock() throws InterruptedException {
        String key = PREFIX + "unlock:red:lock";
        DistributedLock lock = allocator.generator(key);
        new Thread(()->assertTrue(allocator.generator(key).tryLock())).start();
        TimeUnit.MILLISECONDS.sleep(100);
        // 如果是redlock，就算是其它线程获取到锁，当前线程去解锁也不会抛出异常
        // 这里跟常规的锁语义不一样
        lock.unlock();
        assertFalse(lock.tryLock());
    }

    @Test
    public void testUnlockExpiredRedLock() throws InterruptedException {
        String key = PREFIX + "unlock:expired:red:lock";
        DistributedLock lock = allocator.generator(key);
        assertTrue(lock.tryLock(1,1, TimeUnit.SECONDS));
        // 等待1s直到锁过期
        TimeUnit.MILLISECONDS.sleep(1200);
        new Thread(()->assertTrue(allocator.generator(key).tryLock())).start();
        TimeUnit.MILLISECONDS.sleep(100);
        // todo 已经有其它的线程获取到锁了，而redlock居然还能解锁，不符合语义
        lock.unlock();
        assertFalse(lock.tryLock());
    }

    @Test
    @Repeat(10)
    public void testUnlock() throws InterruptedException {
        String key = PREFIX + "unlock";
        DistributedLock lock = allocator.generator(key);
        // 先设置一个时间为2s的锁
        assertTrue(lock.tryLock(2, 2, TimeUnit.SECONDS));
        // 另外一个线程去解锁
        new Thread(()-> {
            try {
                allocator.generator(key).unlock();
            } catch (IllegalMonitorStateException e) {
                // ignore
                return ;
            }
            fail();
        }).start();
        TimeUnit.MILLISECONDS.sleep(100);
        // 另外一个线程再次去加锁
        new Thread(()-> assertFalse(allocator.generator(key).tryLock())).start();
        // 等待2s，让第一个获取锁的线程自动释放锁
        TimeUnit.MILLISECONDS.sleep(2200);
        // 另外一个线程可以获取锁了
        new Thread(() -> {
            Lock l = allocator.generator(key);
            assertTrue(l.tryLock());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            l.unlock();
        }).start();
        TimeUnit.MILLISECONDS.sleep(100);
        // 尝试释放已经过期的锁，看看能否把其它线程获取的锁释放
        // 如果当前锁被其它的线程获取，那么不能解锁成功，因为当前操作已经失去防护
        // 如果当前锁没有被其它的线程获取，那么还是能释放成功，因为不影响结果
        try {
            lock.unlock();
        } catch (IllegalMonitorStateException e) {
            // ignore
        }
        // 不能再次获取当前锁
        assertFalse(lock.tryLock());
        // 等待其它线程获取的锁自动过期释放
        TimeUnit.MILLISECONDS.sleep(1100);
        // 解锁失败，因为当前线程并未有用锁
        lock.unlock();
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void testUnlockExpiredLock() throws InterruptedException {
        String key = PREFIX + "expired:unlock";
        DistributedLock lock = allocator.generator(key);
        // 如果并未获取过锁，那么直接解锁没问题
        //todo jdk中标准的服务是会抛出异常，redssion与jdk的语义不符
        lock.unlock();
        Lock lock2 = new ReentrantLock();
        try {
            lock2.unlock();
        } catch (IllegalMonitorStateException e) {
            // ignore
        }
        // 同样的道理，jdk只能存在锁的情况才能解锁，而redssion可以随意解锁
        assertTrue(lock.tryLock());
        lock.unlock();
        lock.unlock();
        assertTrue(lock2.tryLock());
        lock2.unlock();
        try {
            lock2.unlock();
        } catch (IllegalMonitorStateException e) {
            // ignore
        }
        // 另外一个线程获取锁
        new Thread(() -> {
            DistributedLock l = allocator.generator(key);
            try {
                l.tryLock(1, 1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        TimeUnit.MILLISECONDS.sleep(100);
        // 如果是其它的线程获取到锁，然后再调用解锁操作，是会抛出异常
        // 也就是说redssion认为大家都不存在锁，直接解锁不影响结果
        lock.unlock();
    }

    @Test
    public void testUnlockExpiredLockByOwner() throws InterruptedException {
        DistributedLock lock = allocator.generator(PREFIX+"owner:lock");
        assertTrue(lock.tryLock(1, 1, TimeUnit.SECONDS));
        TimeUnit.SECONDS.sleep(2);
        // 没有其它的线程获取锁，虽然锁已经过期了，但是并没有影响到结果，所以直接解锁过期的锁没问题
        lock.unlock();
    }

    @Test
    public void testUnlockExpiredLockByOther() throws InterruptedException {
        String key = PREFIX+"other:lock";
        DistributedLock lock = allocator.generator(key);
        assertTrue(lock.tryLock(1, 1, TimeUnit.SECONDS));
        TimeUnit.SECONDS.sleep(2);
        new Thread(()->assertTrue(allocator.generator(key).tryLock())).start();
        // 如果有其它的线程获取到锁了，那么直接释放锁会抛出异常，提示开发人员当前操作已经失去锁的保护
        TimeUnit.MILLISECONDS.sleep(100);
        lock.unlock();
    }

    @Test
    public void testLock() throws Exception {
        String key = PREFIX + "1";
        DistributedLock lock = allocator.generator(key);
        lock.lock();
        new Thread(()-> assertFalse(allocator.generator(key).tryLock())).start();
        TimeUnit.MILLISECONDS.sleep(10);
        lock.unlock();
        mustAllocated(key);
        TimeUnit.MILLISECONDS.sleep(20);
        assertFalse(lock.tryLock());
        TimeUnit.SECONDS.sleep(4);
        assertTrue(lock.tryLock());
        lock.unlock();
    }

    @Test
    public void testLockInterrupted() throws Exception {
        String key = PREFIX + "2";
        // 尝试获取可中断锁
        DistributedLock lock = allocator.generator(key);
        lock.lockInterruptibly();
        // 其它线程获取锁肯定失败
        new Thread(()-> assertFalse(allocator.generator(key).tryLock())).start();
        TimeUnit.MILLISECONDS.sleep(10);
        lock.unlock();
        // 再次获取锁
        mustAllocated(key);
        TimeUnit.MILLISECONDS.sleep(20);
        assertFalse(lock.tryLock());
        // 可中断获再次获取锁
        Thread t = new Thread(()->{
            try {
                lock.lockInterruptibly();
                fail();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(Thread.currentThread().isInterrupted());
        });
        t.start();
        // 中断获取锁的线程
        t.interrupt();
        TimeUnit.SECONDS.sleep(4);
        assertTrue(lock.tryLock());
        lock.unlock();
    }

    private void mustAllocated(String key) {
        new Thread(()-> {
            DistributedLock innerLock = allocator.generator(key);
            assertTrue(innerLock.tryLock());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            }
            innerLock.unlock();
        }).start();
    }

    @Test
    public void testLockWithLeaseTime() throws Exception {
        String key = PREFIX + "3";
        DistributedLock lock = allocator.generator(key);
        // 获取时间段为2s的锁
        lock.lock(2, TimeUnit.SECONDS);
        // 其它线程获取锁肯定失败
        new Thread(()-> assertFalse(allocator.generator(key).tryLock())).start();
        // 等待2s释放锁后重新获取锁
        TimeUnit.MILLISECONDS.sleep(2200);
        // 其它线程重新获取锁
        new Thread(()-> allocator.generator(key).lock(2, TimeUnit.SECONDS)).start();
        TimeUnit.MILLISECONDS.sleep(10);
        assertFalse(lock.tryLock());
        TimeUnit.SECONDS.sleep(3);
        // 等待3s又可以获取到锁
        assertTrue(lock.tryLock());
        lock.unlock();
    }

    @Test
    public void testLockInterruptedAndWithLeaseTime() throws Exception {
        String key = PREFIX + "4";
        // 尝试获取可中断锁
        DistributedLock lock = allocator.generator(key);
        lock.lockInterruptibly(2, TimeUnit.SECONDS);
        // 其它线程获取锁肯定失败
        new Thread(()-> assertFalse(allocator.generator(key).tryLock())).start();
        // 可中断获再次获取锁
        Thread t = new Thread(()->{
            try {
                lock.lockInterruptibly(2, TimeUnit.SECONDS);
                fail();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(Thread.currentThread().isInterrupted());
        });
        t.start();
        // 中断获取锁的线程
        t.interrupt();
        TimeUnit.MILLISECONDS.sleep(2200);
        // 锁已经释放了，可以由下一个线程获取锁
        mustAllocated(key);
        TimeUnit.MILLISECONDS.sleep(100);
        assertFalse(lock.tryLock());
    }

    @Test
    public void testTryLock() throws Exception {
        String key = PREFIX + "5";
        DistributedLock lock = allocator.generator(key);
        assertTrue(lock.tryLock());
        // 其它线程获取锁肯定失败
        new Thread(()-> assertFalse(allocator.generator(key).tryLock())).start();
        lock.unlock();
        // 释放锁了，其它线程可以获取锁
        new Thread(()-> assertTrue(allocator.generator(key).tryLock())).start();
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void testTryLockWithTimeout() throws Exception {
        String key = PREFIX + "6";
        DistributedLock lock = allocator.generator(key);
        assertTrue(lock.tryLock(2, TimeUnit.SECONDS));
        // 其它线程获取锁肯定失败
        new Thread(()-> assertFalse(allocator.generator(key).tryLock())).start();
        // 其它线程等待3s时候获取线程成功
        new Thread(() -> {
            try {
                DistributedLock innerLock = allocator.generator(key);
                assertTrue(innerLock.tryLock(3, TimeUnit.SECONDS));
                TimeUnit.SECONDS.sleep(2);
                innerLock.unlock();
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            }
        }).start();
        TimeUnit.MILLISECONDS.sleep(2000);
        // 释放锁后，其它的线程才能获取锁
        lock.unlock();
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    public void testTryLockWithLeaseTime() throws Exception {
        String key = PREFIX + "7";
        DistributedLock lock = allocator.generator(key);
        assertTrue(lock.tryLock(2, 1, TimeUnit.SECONDS));
        // 其它线程获取锁肯定失败
        new Thread(()-> assertFalse(allocator.generator(key).tryLock())).start();
        // 其它线程等待3s时候获取线程成功
        new Thread(() -> {
            try {
                DistributedLock innerLock = allocator.generator(key);
                assertTrue(innerLock.tryLock(2, 5, TimeUnit.SECONDS));
                TimeUnit.SECONDS.sleep(2);
                innerLock.unlock();
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            }
        }).start();
        TimeUnit.MILLISECONDS.sleep(2000);
        assertFalse(lock.tryLock(1, 2, TimeUnit.SECONDS));
        TimeUnit.MILLISECONDS.sleep(1200);
        // 释放锁了，其它线程可以获取锁
        assertTrue(lock.tryLock(0, 2, TimeUnit.SECONDS));

    }
}
