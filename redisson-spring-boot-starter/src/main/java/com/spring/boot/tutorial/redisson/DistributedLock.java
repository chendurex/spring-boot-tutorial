package com.spring.boot.tutorial.redisson;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * redis锁，包括普通的{@link Lock}所有语义的锁和带超时机制的锁，建议使用带超时机制的锁
 * 如果使用的锁未带超时功能，在服务突然宕机时，默认30s也会释放锁，防止锁被无限锁住
 * @author cheny.huang
 * @date 2018-12-24 15:31.
 */
public interface DistributedLock extends Lock {

    /**
     * 阻塞式获取锁，如果获取到锁则会在lease时间后自动释放锁
     * @see #lockInterruptibly(int, TimeUnit)
     * @see #tryLock(int, int, TimeUnit)
     */
    void lock(int lease, TimeUnit unit);

    /**
     * <p>可中断式获取锁</p>
     * 如果长时间未获取锁，可以中断当前线程
     * 如果获取到锁则会在lease时间后自动释放锁
     * @see #tryLock(int, int, TimeUnit)
     * @throws InterruptedException 如果在获取锁的过程中线程被中断则返回此异常
     */
    void lockInterruptibly(int lease, TimeUnit unit) throws InterruptedException;

    /**
     * <p>尝试获取锁，如果等待wait时间后还未获取到锁则返回false</p>
     * 如果在wait时间内获取到锁，则会在lease时间后自动释放锁
     * @param wait 等待获取锁时间
     * @param lease 续约时间
     * @param unit 时间单位
     * @throws InterruptedException 如果在获取锁的过程中线程被中断则返回此异常
     */
    boolean tryLock(int wait, int lease, TimeUnit unit) throws InterruptedException;


}