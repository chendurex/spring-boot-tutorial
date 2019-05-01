package com.spring.boot.tutorial.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonRedLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * redis redlock
 * @author cheny.huang
 * @date 2018-12-25 17:31.
 */
@Slf4j
public class RedLockImpl implements DistributedLock {
    private static final long DEFAULT_LOG_TIME = 1000;
    private final RedissonRedLock redLock;
    public RedLockImpl(RedissonRedLock redissonRedLock) {
        this.redLock = redissonRedLock;
    }

    @Override
    public void lock(int lease, TimeUnit unit) {
        log.debug("开始获取锁,lease:{}, unit:{}", lease, unit);
        long s = System.currentTimeMillis();
        redLock.lock(lease, unit);
        endLog(s,true);
    }

    @Override
    public void lockInterruptibly(int lease, TimeUnit unit) throws InterruptedException {
        log.debug("开始获取可中断锁,lease:{}, unit:{}", lease, unit);
        long s = System.currentTimeMillis();
        redLock.lockInterruptibly(lease, unit);
        endLog(s, true);
    }

    @Override
    public boolean tryLock(int wait, int lease, TimeUnit unit) throws InterruptedException {
        log.debug("开始获取锁,wait:{},lease:{}, unit:{}", wait, lease, unit);
        long s = System.currentTimeMillis();
        boolean b = redLock.tryLock(wait, lease, unit);
        endLog(s, b);
        return b;
    }

    @Override
    public void lock() {
        log.debug("开始获取锁");
        long s = System.currentTimeMillis();
        redLock.lock();
        endLog(s, true);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        log.debug("开始获取锁");
        long s = System.currentTimeMillis();
        redLock.lockInterruptibly();
        endLog(s, true);
    }

    @Override
    public boolean tryLock() {
        log.debug("开始获取锁");
        long s = System.currentTimeMillis();
        boolean b = redLock.tryLock();
        endLog(s, b);
        return b;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        log.debug("开始获取锁");
        long s = System.currentTimeMillis();
        boolean b = redLock.tryLock(time, unit);
        endLog(s, b);
        return b;
    }

    @Override
    public void unlock() {
        log.debug("执行分布式解锁");
        redLock.unlock();
    }

    @Override
    public Condition newCondition() {
        return redLock.newCondition();
    }

    private void endLog(long s, boolean suc) {
        long elapsed = System.currentTimeMillis() - s;
        if (elapsed > DEFAULT_LOG_TIME) {
            log.warn("获取锁:{}，一共耗时:{}ms", suc, elapsed);
        } else {
            log.debug("获取锁:{}，一共耗时:{}ms", suc, elapsed);
        }
    }
}
