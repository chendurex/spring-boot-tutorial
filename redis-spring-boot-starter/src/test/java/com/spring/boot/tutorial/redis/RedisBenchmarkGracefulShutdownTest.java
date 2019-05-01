package com.spring.boot.tutorial.redis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Client;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.concurrent.TimeUnit;

/**
 * @author cheny.huang
 * @date 2018-09-12 09:57.
 */
public class RedisBenchmarkGracefulShutdownTest {
    private JedisCommands jedisCommands;

    @Before
    public void before() {
        jedisCommands = CreateJedisCommands.generator();
        new ShutdownServer().flushdb();
    }

    @Test
    public void testGracefulShutdown() {
        String key = "benchmark:test";
        for (int i=0;i<10000;i++) {
            String k = key + ":" + i;
            jedisCommands.setex(k, 600, k);
        }
        new ShutdownServer().shutdown();
        boolean renew = false;
        for (int i=0;i<10000;i++) {
            String k = key + ":" + i;
            try {
                Assert.assertEquals(k, jedisCommands.get(k));
            } catch (JedisConnectionException jce) {
                if (renew) {
                    throw jce;
                }
                renew = true;
            }

        }
    }

    class ShutdownServer {
        void flushdb() {
            for (int i=7000;i<7007;i++) {
                Client client = new Client("192.168.1.117", i);
                client.flushAll();
            }
        }
        void shutdown() {
            // 3台master的端口
            int []port = {7000,7001,7003};
            for (int i : port) {
                Client client = new Client("192.168.1.117", i);
                client.shutdown();
                try {
                    // 奇奇怪怪的，如果不调用这个方法则不会关闭服务器，但是调用了，则直接报错，还未仔细研究为何
                    System.out.println("-------------[" + client.getBulkReply() + "]-----------------");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 一台一台的关闭，直到所有的master都确认关闭，master选举时间为15s，所以等待20s
                // 不能一下子把所有的master都关闭，否则slave无法完成晋升
                try {
                    TimeUnit.SECONDS.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
