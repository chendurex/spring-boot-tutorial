package com.spring.boot.tutorial.redis;

import org.junit.Assert;
import org.openjdk.jmh.annotations.*;
import redis.clients.jedis.JedisCommands;

import java.util.concurrent.TimeUnit;

/**
 * @author cheny.huang
 * @date 2018-09-12 09:57.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 4)
@Fork(1)
public class RedisBenchmarkSingleThreadTest {
    private JedisCommands jedisCommands;
    @Setup
    public void setup() {
        jedisCommands = CreateJedisCommands.generator();
    }

    @Benchmark
    public void setAndGetQps() {
        String key = "benchmark:test";
        for (int i=0;i<10000;i++) {
            String k = key + ":" + i;
            jedisCommands.setex(k, 600, k);
            Assert.assertEquals(k, jedisCommands.get(k));
        }
    }
}
