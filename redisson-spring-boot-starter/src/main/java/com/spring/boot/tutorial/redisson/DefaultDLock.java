package com.spring.boot.tutorial.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * @author cheny.huang
 * @date 2018-12-24 15:32.
 */
@Slf4j
public class DefaultDLock implements DistributedLock {
    private static final long DEFAULT_LOG_TIME = 1000;
    private final RLock rLock;
    public DefaultDLock(RLock rLock) {
        this.rLock = rLock;
    }

    @Override
    public void lock() {
        log.debug("开始获取分布式锁");
        long s = System.currentTimeMillis();
        rLock.lock();
        endLog(s, true);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        log.debug("开始获取可中断分布式锁");
        long s = System.currentTimeMillis();
        rLock.lockInterruptibly();
        endLog(s, true);
    }

    @Override
    public boolean tryLock() {
        log.debug("获取分布式锁");
        long s = System.currentTimeMillis();
        boolean b = rLock.tryLock();
        endLog(s, b);
        return b;
    }

    @Override
    public Condition newCondition() {
        return rLock.newCondition();
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        log.debug("开始获取分布式锁,timeout:{},unit:{}", timeout, unit);
        long s = System.currentTimeMillis();
        boolean b = rLock.tryLock(timeout, unit);
        endLog(s, b);
        return b;
    }

    @Override
    public void unlock() {
        log.debug("释放分布式锁");
        rLock.unlock();
        log.debug("成功获取分布式锁");
    }

    @Override
    public void lock(int lease, TimeUnit unit) {
        log.debug("开始获取分布式锁,lease:{}, unit:{}", lease, unit);
        long s = System.currentTimeMillis();
        rLock.lock(lease, unit);
        endLog(s, true);
    }

    @Override
    public void lockInterruptibly(int lease, TimeUnit unit) throws InterruptedException {
        log.debug("开始获取分布式可中断锁,lease{}, unit:{}", lease, unit);
        long s = System.currentTimeMillis();
        rLock.lockInterruptibly(lease, unit);
        endLog(s, true);
    }

    @Override
    public boolean tryLock(int wait, int lease, TimeUnit unit) throws InterruptedException {
        log.debug("开始获取分布式锁,wait:{}, lease:{}, unit:{}", wait, lease, unit);
        long s = System.currentTimeMillis();
        boolean b = rLock.tryLock(wait, lease, unit);
        endLog(s, b);
        return b;
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
