package com.spring.boot.tutorial.redis;

import org.junit.Assert;
import org.openjdk.jmh.annotations.*;
import redis.clients.jedis.JedisCommands;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author cheny.huang
 * @date 2018-09-12 09:57.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 4, time = 20)
@Threads(100)
@Fork(1)
public class RedisBenchmarkMultiThreadTest {
    private JedisCommands jedisCommands;
    private AtomicInteger atomicInteger = new AtomicInteger(10000);
    @Setup
    public void setup() {
        jedisCommands = CreateJedisCommands.generator();
    }

    @TearDown(Level.Iteration)
    public void teardown() {
        atomicInteger.set(10000);
    }


    @Benchmark
    public void multiThreadSetAndGetQps() {
        String key = "benchmark:test";
        String k = key + ":" + atomicInteger.getAndIncrement();
        jedisCommands.setex(k, 200, k);
        Assert.assertEquals(k, jedisCommands.get(k));
    }
}
